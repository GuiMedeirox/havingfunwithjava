package com.havingfunwithjava.payment.application;

import com.havingfunwithjava.payment.IntegrationTestBase;
import com.havingfunwithjava.payment.domain.OrderCreatedEvent;
import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentRepository;
import com.havingfunwithjava.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Teste de slice vertical (Seam 1) do consumer end-to-end (issue #19).
 *
 * <p>Publica um {@link OrderCreatedEvent} no RabbitMQ (orders.exchange) e verifica
 * que o {@code PaymentConsumer} o processa: cria um {@link Payment} no banco com
 * status AUTHORIZED (valor "100.00" → gateway mock autoriza, último decimal 0 = par).
 *
 * <p>Usa Awaitility para aguardar o processamento assíncrono (o consumer roda em
 * thread separada). O RabbitMQ e o Postgres são reais (Testcontainers).
 */
class PaymentProcessingTest extends IntegrationTestBase {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${app.messaging.orders-exchange}")
    private String ordersExchange;

    @Value("${app.messaging.payment-routing-key}")
    private String paymentRoutingKey;

    @Test
    void shouldConsumeOrderCreatedAndAuthorizePayment() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        // Valor 100.00: último decimal é 0 (par) → gateway mock AUTORIZA.
        // Não excede 10000 → não declina.
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, customerId, "100.00", "BRL",
                List.of(new OrderCreatedEvent.Item(productId, "Produto", 1, "100.00", "BRL"))
        );

        rabbitTemplate.convertAndSend(ordersExchange, paymentRoutingKey, event);

        // Aguarda o consumer processar assincronamente (timeout 15s)
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var payment = paymentRepository.findByOrderId(orderId);
            assertThat(payment).isPresent();
            assertThat(payment.get().status()).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(payment.get().amount().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        });
    }

    @Test
    void shouldDeclinePaymentWhenAmountExceedsLimit() {
        UUID orderId = UUID.randomUUID();
        // Valor 15000.00 > 10000 (limite do mock) → DECLINED.
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, UUID.randomUUID(), "15000.00", "BRL",
                List.of(new OrderCreatedEvent.Item(UUID.randomUUID(), "Carro", 1, "15000.00", "BRL"))
        );

        rabbitTemplate.convertAndSend(ordersExchange, paymentRoutingKey, event);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var payment = paymentRepository.findByOrderId(orderId);
            assertThat(payment).isPresent();
            assertThat(payment.get().status()).isEqualTo(PaymentStatus.DECLINED);
        });
    }
}
