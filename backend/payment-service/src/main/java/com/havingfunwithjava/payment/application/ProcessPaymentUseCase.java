package com.havingfunwithjava.payment.application;

import com.havingfunwithjava.payment.domain.Money;
import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentGateway;
import com.havingfunwithjava.payment.domain.PaymentMethod;
import com.havingfunwithjava.payment.domain.PaymentRepository;
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
 * <p>Orquestra o domínio e as strategies:
 * <ol>
 *   <li>Cria o {@link Payment} em PENDING (a partir do orderId, método e valor).</li>
 *   <li>Seleciona a {@link PaymentStrategy} para o método (Strategy pattern).</li>
 *   <li>Delega o processamento à strategy, que chama o {@link PaymentGateway}.</li>
 *   <li>Atualiza o status conforme o resultado: AUTHORIZED, DECLINED, ou
 *       mantém PENDING p/ retentativa em caso de falha transitória (issue #20).</li>
 *   <li>Persiste o pagamento final.</li>
 * </ol>
 *
 * <p>NOTA sobre método: neste slice, o método é definido pelo consumer (default
 * CREDIT_CARD). Em produção, viria no evento OrderCreated ou seria escolhido pelo
 * cliente no checkout.
 */
@Service
public class ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;
    private final Map<PaymentMethod, PaymentStrategy> strategiesByMethod;

    /**
     * @param strategies Spring injeta todas as implementações de {@link PaymentStrategy};
     *                   indexamos por {@link PaymentStrategy#supports()} p/ lookup O(1).
     */
    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                 PaymentGateway gateway,
                                 List<PaymentStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
        this.strategiesByMethod = strategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::supports, Function.identity()));
    }

    @Transactional
    public Payment execute(UUID orderId, PaymentMethod method, Money amount) {
        // 1. Idempotência: se já existe pagamento para este pedido, não reprocessa.
        //    (Pode acontecer por redelivery do RabbitMQ.)
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
                // Neste slice, apenas logamos; o backoff exponencial vem na #20.
                log.warn("Falha transitória no pagamento do pedido {}: {}", orderId, result.reason());
                yield payment;
            }
        };

        // 6. Persiste o estado final
        return paymentRepository.save(updated);
    }
}
