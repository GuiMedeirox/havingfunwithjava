package com.havingfunwithjava.payment.application;

import com.havingfunwithjava.payment.IntegrationTestBase;
import com.havingfunwithjava.payment.domain.OrderCreatedEvent;
import com.havingfunwithjava.payment.domain.PaymentRepository;
import com.havingfunwithjava.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Teste de slice vertical (Seam 1) das retentativas com backoff (issue #20).
 *
 * <p>Cenário: valor com último decimal ímpar (ex.: 100.01) → gateway mock retorna
 * FALHA_TRANSIENTE em todas as tentativas. Após esgotar maxAttempts (configurado
 * baixo para o teste), o pagamento vai para FAILED.
 *
 * <p>Configuramos delays baixos (10ms) via {@link DynamicPropertySource} para o
 * teste não ficar lento.
 */
class PaymentRetryTest extends IntegrationTestBase {

    @DynamicPropertySource
    static void fastRetries(DynamicPropertyRegistry registry) {
        // Retentativas rápidas para o teste (não dormir 1s+ por tentativa)
        registry.add("app.payment.retry.max-attempts", () -> "3");
        registry.add("app.payment.retry.initial-delay-ms", () -> "10");
        registry.add("app.payment.retry.multiplier", () -> "1.0");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${app.messaging.orders-exchange}")
    private String ordersExchange;

    @Value("${app.messaging.payment-routing-key}")
    private String paymentRoutingKey;

    @Test
    void shouldFailAfterExhaustingRetriesOnTransientError() {
        UUID orderId = UUID.randomUUID();
        // Valor 100.01: último decimal ímpar → FALHA_TRANSIENTE (gateway mock)
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, UUID.randomUUID(), "100.01", "BRL",
                List.of(new OrderCreatedEvent.Item(UUID.randomUUID(), "Item", 1, "100.01", "BRL"))
        );

        rabbitTemplate.convertAndSend(ordersExchange, paymentRoutingKey, event);

        // Aguarda o pagamento esgotar retentativas e ir para FAILED
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            var payment = paymentRepository.findByOrderId(orderId);
            assertThat(payment).isPresent();
            assertThat(payment.get().status()).isEqualTo(PaymentStatus.FAILED);
            // Deve ter tentado 3 vezes (maxAttempts)
            assertThat(payment.get().attempts()).isGreaterThanOrEqualTo(3);
        });
    }
}
