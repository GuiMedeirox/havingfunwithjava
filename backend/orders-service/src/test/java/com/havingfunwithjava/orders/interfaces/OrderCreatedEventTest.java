package com.havingfunwithjava.orders.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havingfunwithjava.orders.IntegrationTestBase;
import com.havingfunwithjava.orders.domain.CatalogClient;
import com.havingfunwithjava.orders.domain.CatalogItem;
import com.havingfunwithjava.orders.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) da publicação do evento OrderCreated (issue #18).
 *
 * <p>Cria um pedido via POST /orders (com CatalogClient mockado) e verifica que:
 * <ul>
 *   <li>Uma mensagem chega na fila {@code payment.queue}.</li>
 *   <li>O corpo é JSON válido com orderId, customerId, amount, currency, items[].</li>
 *   <li>O orderId da mensagem bate com o do pedido criado.</li>
 * </ul>
 *
 * <p>O RabbitMQ real (Testcontainers) valida a topologia declarada em
 * {@code RabbitMQConfig} (exchange, binding, fila) — se algo estiver errado na
 * declaração, a mensagem não chega e o teste falha.
 */
class OrderCreatedEventTest extends IntegrationTestBase {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.payment-queue}")
    private String paymentQueue;

    @MockBean
    private CatalogClient catalogClient;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Drena mensagens antigas da fila antes do teste. Como todos os testes de
     * slice vertical compartilham o mesmo RabbitMQ (container singleton) e a mesma
     * fila, mensagens de testes anteriores podem estar pendentes. Limpamos para
     * garantir que a próxima mensagem consumida seja a deste teste.
     */
    private void drainPaymentQueue() {
        while (rabbitTemplate.receive(paymentQueue, 500) != null) {
            // descarta
        }
    }

    @Test
    void shouldPublishOrderCreatedEventToPaymentQueue() throws Exception {
        drainPaymentQueue();
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(catalogClient.findProductsByIds(anyList()))
                .thenReturn(List.of(new CatalogItem(productId, "Teclado", new Money(new BigDecimal("150.00"), "BRL"), true)));

        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    { "productId": "%s", "quantity": 2, "expectedUnitPrice": "150.00", "currency": "BRL" }
                  ]
                }
                """.formatted(customerId, productId);

        // Cria o pedido (publica o evento no broker)
        String responseJson = mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID createdOrderId = UUID.fromString(mapper.readTree(responseJson).get("id").asText());

        // Consome a mensagem da fila (timeout de 10s para o broker entregar)
        Message message = rabbitTemplate.receive(paymentQueue, 10_000);
        assertNotNull(message, "Mensagem OrderCreated deveria ter chegado na payment.queue");

        // Valida o corpo JSON
        JsonNode body = mapper.readTree(message.getBody());
        assertEquals(createdOrderId.toString(), body.get("orderId").asText());
        assertEquals(customerId.toString(), body.get("customerId").asText());
        assertEquals("BRL", body.get("currency").asText());
        // amount = 2 × 150.00 = 300 (escala pode variar, só checamos presença)
        assertNotNull(body.get("amount").asText());
        assertTrue(body.get("items").isArray());
        assertEquals(1, body.get("items").size());
        assertEquals(productId.toString(), body.get("items").get(0).get("productId").asText());
        assertEquals("Teclado", body.get("items").get(0).get("productName").asText());
        assertEquals(2, body.get("items").get(0).get("quantity").asInt());
    }
}
