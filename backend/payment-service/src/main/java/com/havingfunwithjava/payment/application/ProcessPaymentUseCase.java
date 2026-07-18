package com.havingfunwithjava.payment.application;

import com.havingfunwithjava.payment.domain.Money;
import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentGateway;
import com.havingfunwithjava.payment.domain.PaymentMethod;
import com.havingfunwithjava.payment.domain.PaymentRepository;
import com.havingfunwithjava.payment.domain.PaymentResultEvent;
import com.havingfunwithjava.payment.domain.PaymentResultPublisher;
import com.havingfunwithjava.payment.domain.PaymentSucceeded;
import com.havingfunwithjava.payment.domain.PaymentFailed;
import com.havingfunwithjava.payment.domain.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso: processar um pagamento a partir de um evento OrderCreated.
 *
 * <p>Orquestra o domínio, as strategies, e a publicação do resultado (issue #21):
 * <ol>
 *   <li>Cria o {@link Payment} em PENDING (a partir do orderId, método e valor).</li>
 *   <li>Seleciona a {@link PaymentStrategy} para o método (Strategy pattern).</li>
 *   <li>Delega o processamento à strategy, que chama o {@link PaymentGateway}.</li>
 *   <li>Atualiza o status conforme o resultado: AUTHORIZED, DECLINED, ou
 *       mantém PENDING p/ retentativa em caso de falha transitória (issue #20).</li>
 *   <li>Persiste o pagamento final.</li>
 *   <li>Publica o {@link PaymentResultEvent} (PaymentSucceeded/PaymentFailed) no
 *       broker, para o orders-service atualizar o status do pedido (issue #22).</li>
 * </ol>
 *
 * <p>Idempotência dupla (issue #21):
 * <ul>
 *   <li>Se o mesmo OrderCreated chega duas vezes (redelivery), o passo 1 já
 *       encontra o pagamento existente e retorna sem reprocessar — não publica
 *       resultado duplicado.</li>
 *   <li>A publicação do resultado acontece dentro da transação; falha no broker
 *       reverte o processamento.</li>
 * </ul>
 */
@Service
public class ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;
    private final PaymentResultPublisher resultPublisher;
    private final RetryProperties retryProperties;
    private final Map<PaymentMethod, PaymentStrategy> strategiesByMethod;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                 PaymentGateway gateway,
                                 PaymentResultPublisher resultPublisher,
                                 RetryProperties retryProperties,
                                 List<PaymentStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
        this.resultPublisher = resultPublisher;
        this.retryProperties = retryProperties;
        this.strategiesByMethod = strategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::supports, Function.identity()));
    }

    @Transactional
    public Payment execute(UUID orderId, PaymentMethod method, Money amount) {
        // 1. Idempotência: se já existe pagamento para este pedido, não reprocessa.
        var existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            log.info("Pagamento já existe para o pedido {} (status {}); ignorando",
                    orderId, existing.get().status());
            return existing.get();
        }

        // 2. Cria o pagamento PENDING
        Payment payment = Payment.createNew(orderId, method, amount);
        payment = paymentRepository.save(payment);

        // 3. Seleciona a strategy
        PaymentStrategy strategy = strategiesByMethod.get(method);
        if (strategy == null) {
            throw new IllegalStateException("Nenhuma strategy para o método: " + method);
        }

        // 4. Loop de retentativas com backoff exponencial (issue #20)
        PaymentGateway.GatewayResult result = null;
        int maxAttempts = retryProperties.getMaxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            result = strategy.process(payment, gateway);

            if (result.status() != PaymentGateway.GatewayStatus.FALHA_TRANSIENTE) {
                // Resultado definitivo (AUTHORIZED ou DECLINED) — para de tentar
                break;
            }

            // Falha transitória: decide se retenta ou esgota
            if (attempt < maxAttempts) {
                long delay = retryProperties.delayForAttempt(attempt + 1);
                log.warn("Falha transitória no pagamento do pedido {} (tentativa {}/{}): {}. "
                                + "Retentando em {}ms",
                        orderId, attempt, maxAttempts, result.reason(), delay);
                payment = payment.incrementAttempts();
                paymentRepository.save(payment);
                sleep(delay);
            } else {
                log.error("Pagamento do pedido {} esgotou {} tentativas; marcando FAILED",
                        orderId, maxAttempts);
            }
        }

        // 5. Atualiza status conforme resultado final
        Payment updated = switch (result.status()) {
            case AUTHORIZED -> payment.authorize();
            case DECLINED -> payment.decline();
            case FALHA_TRANSIENTE -> payment.fail(); // esgotou retentativas
        };

        // 6. Persiste o estado final
        Payment saved = paymentRepository.save(updated);

        // 7. Publica o resultado (issue #21) em estado terminal
        publishResultIfTerminal(saved, result.reason());

        return saved;
    }

    /**
     * Sleep tratando InterruptedException (best-effort; não interrompe o fluxo).
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep de backoff interrompido");
        }
    }

    /**
     * Publica o evento de resultado se o pagamento chegou a um estado terminal.
     * Agora também publica PaymentFailed quando o status é FAILED (esgotou retentativas — issue #20).
     */
    private void publishResultIfTerminal(Payment payment, String failureReason) {
        switch (payment.status()) {
            case AUTHORIZED -> resultPublisher.publish(
                    new PaymentSucceeded(payment.orderId(), payment.id().value()));
            case DECLINED -> resultPublisher.publish(
                    new PaymentFailed(payment.orderId(),
                            failureReason != null ? failureReason : "Pagamento recusado"));
            case FAILED -> resultPublisher.publish(
                    new PaymentFailed(payment.orderId(),
                            failureReason != null ? failureReason : "Falha após retentativas"));
            // PENDING não publica — aguarda retentativa
            default -> log.debug("Pagamento {} não terminal ({}); resultado não publicado",
                    payment.orderId(), payment.status());
        }
    }
}
