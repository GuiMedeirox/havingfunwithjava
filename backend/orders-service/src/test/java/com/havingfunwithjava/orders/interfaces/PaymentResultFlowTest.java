package com.havingfunwithjava.orders.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havingfunwithjava.orders.IntegrationTestBase;
import com.havingfunwithjava.orders.domain.CatalogClient;
import com.havingfunwithjava.orders.domain.CatalogItem;
import com.havingfunwithjava.orders.domain.Money;
import com.havingfunwithjava.orders.domain.OrderRepository;
import com.havingfunwithjava.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) do fluxo completo: cria pedido → publica
 * OrderCreated → (simula payment) publica PaymentSucceeded → consumer do orders
 * marca o pedido como PAID (issue #22).
 *
 * <p>Não sobe o payment-service — apenas simula a publicação do resultado no
 * broker (como o payment-service faria). O consumer do orders-service consome
 * e aplica a transição de status via {@code ApplyPaymentResultUseCase}.
 *
 * <p>Valida o fechamento do loop assíncrono completo.
 */
class PaymentResultFlowTest extends IntegrationTestBase {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${app.messaging.orders-exchange}")
    private String ordersExchange;

    @Value("${app.messaging.result-routing-key-succeeded}")
    private String routingKeySucceeded;

    @Value("${app.messaging.result-routing-key-failed}")
    private String routingKeyFailed;

    @MockBean
    private CatalogClient catalogClient;

    private final ObjectMapper mapper = new ObjectMapper();

    private String createOrder(UUID productId) throws Exception {
        when(catalogClient.findProductsByIds(anyList()))
                .thenReturn(List.of(new CatalogItem(productId, "Item",
                        new Money(new BigDecimal("100.00"), "BRL"), true)));
        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    { "productId": "%s", "quantity": 1, "expectedUnitPrice": "100.00", "currency": "BRL" }
                  ]
                }
                """.formatted(UUID.randomUUID(), productId);
        var result = mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void shouldMarkOrderAsPaidWhenPaymentSucceeded() throws Exception {
        UUID productId = UUID.randomUUID();
        String orderId = createOrder(productId);

        // Simula o payment-service publicando PaymentSucceeded
        String succeededJson = """
                { "orderId": "%s", "paymentId": "%s" }
                """.formatted(orderId, UUID.randomUUID());
        rabbitTemplate.convertAndSend(ordersExchange, routingKeySucceeded, succeededJson);

        // Aguarda o consumer marcar como PAID
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var order = orderRepository.findById(
                    new com.havingfunwithjava.orders.domain.OrderId(UUID.fromString(orderId)));
            assertThat(order).isPresent();
            assertThat(order.get().status()).isEqualTo(OrderStatus.PAID);
        });
    }

    @Test
    void shouldCancelOrderWhenPaymentFailed() throws Exception {
        UUID productId = UUID.randomUUID();
        String orderId = createOrder(productId);

        // Simula o payment-service publicando PaymentFailed
        String failedJson = """
                { "orderId": "%s", "reason": "Cartão recusado" }
                """.formatted(orderId);
        rabbitTemplate.convertAndSend(ordersExchange, routingKeyFailed, failedJson);

        // Aguarda o consumer cancelar o pedido
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var order = orderRepository.findById(
                    new com.havingfunwithjava.orders.domain.OrderId(UUID.fromString(orderId)));
            assertThat(order).isPresent();
            assertThat(order.get().status()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    @Test
    void shouldBeIdempotentOnDuplicatePaymentSucceeded() throws Exception {
        UUID productId = UUID.randomUUID();
        String orderId = createOrder(productId);

        // Publica PaymentSucceeded duas vezes (simula redelivery)
        String succeededJson = """
                { "orderId": "%s", "paymentId": "%s" }
                """.formatted(orderId, UUID.randomUUID());
        rabbitTemplate.convertAndSend(ordersExchange, routingKeySucceeded, succeededJson);
        rabbitTemplate.convertAndSend(ordersExchange, routingKeySucceeded, succeededJson);

        // Aguarda o pedido ser marcado como PAID (uma vez)
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var order = orderRepository.findById(
                    new com.havingfunwithjava.orders.domain.OrderId(UUID.fromString(orderId)));
            assertThat(order).isPresent();
            assertThat(order.get().status()).isEqualTo(OrderStatus.PAID);
        });
        // Idempotência: continua PAID (não quebra com o duplicado)
    }
}
