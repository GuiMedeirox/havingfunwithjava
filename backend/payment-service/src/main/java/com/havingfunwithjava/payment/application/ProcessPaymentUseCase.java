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
    private final Map<PaymentMethod, PaymentStrategy> strategiesByMethod;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                 PaymentGateway gateway,
                                 PaymentResultPublisher resultPublisher,
                                 List<PaymentStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
        this.resultPublisher = resultPublisher;
        this.strategiesByMethod = strategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::supports, Function.identity()));
    }

    @Transactional
    public Payment execute(UUID orderId, PaymentMethod method, Money amount) {
        // 1. Idempotência: se já existe pagamento para este pedido, não reprocessa.
        //    (Pode acontecer por redelivery do RabbitMQ.) Também evita publicar
        //    resultado duplicado — só publicamos na primeira vez.
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

        // 4. Processa via strategy → gateway
        PaymentGateway.GatewayResult result = strategy.process(payment, gateway);

        // 5. Atualiza status conforme resultado
        Payment updated = switch (result.status()) {
            case AUTHORIZED -> payment.authorize();
            case DECLINED -> payment.decline();
            case FALHA_TRANSIENTE -> {
                // Falha transitória: mantém PENDING p/ retentativa (issue #20).
                // NÃO publica resultado ainda — só publica em estado terminal.
                log.warn("Falha transitória no pagamento do pedido {}: {}", orderId, result.reason());
                yield payment;
            }
        };

        // 6. Persiste o estado final
        Payment saved = paymentRepository.save(updated);

        // 7. Publica o resultado (issue #21) apenas em estado terminal
        //    (AUTHORIZED ou DECLINED). Estados PENDING (falha transitória) não
        //    publicam — aguardam retentativa.
        publishResultIfTerminal(saved, result.reason());

        return saved;
    }

    /**
     * Publica o evento de resultado se o pagamento chegou a um estado terminal.
     */
    private void publishResultIfTerminal(Payment payment, String failureReason) {
        switch (payment.status()) {
            case AUTHORIZED -> resultPublisher.publish(
                    new PaymentSucceeded(payment.orderId(), payment.id().value()));
            case DECLINED -> resultPublisher.publish(
                    new PaymentFailed(payment.orderId(),
                            failureReason != null ? failureReason : "Pagamento recusado"));
            // PENDING (falha transitória) e FAILED não publicam aqui
            default -> log.debug("Pagamento {} não terminal ({}); resultado não publicado",
                    payment.orderId(), payment.status());
        }
    }
}
