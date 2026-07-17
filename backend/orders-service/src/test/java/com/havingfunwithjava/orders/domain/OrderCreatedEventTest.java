package com.havingfunwithjava.orders.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de domínio (Seam 2): serialização JSON do {@link OrderCreatedEvent}.
 *
 * <p>Garante que o evento pode ser serializado/desserializado como JSON sem perda
 * de informação (formato estável entre serviços). Usa Jackson (mesmo serializer
 * do broker).
 */
class OrderCreatedEventTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldBeSerializableAsJson() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Order order = Order.createNew(
                new CustomerId(customerId),
                List.of(new OrderItem(productId, "Mouse", 3, new BigDecimal("50.00"), "BRL"))
        );
        // Reconstrói o evento com orderId conhecido (factory usa OrderId.generate)
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, customerId, "150.00", "BRL",
                List.of(new OrderCreatedEvent.Item(productId, "Mouse", 3, "50.00", "BRL"))
        );

        String json = mapper.writeValueAsString(event);
        assertTrue(json.contains("\"orderId\":\"" + orderId + "\""));
        assertTrue(json.contains("\"amount\":\"150.00\""));
        assertTrue(json.contains("\"productName\":\"Mouse\""));

        // Desserializa e valida ida e volta
        OrderCreatedEvent deserialized = mapper.readValue(json, OrderCreatedEvent.class);
        assertEquals(event.orderId(), deserialized.orderId());
        assertEquals(event.amount(), deserialized.amount());
        assertEquals(event.items().size(), deserialized.items().size());
    }

    @Test
    void shouldBuildFromOrderWithCalculatedTotal() {
        Order order = Order.createNew(
                new CustomerId(UUID.randomUUID()),
                List.of(new OrderItem(UUID.randomUUID(), "A", 2, new BigDecimal("10.00"), "BRL"),
                        new OrderItem(UUID.randomUUID(), "B", 1, new BigDecimal("25.00"), "BRL"))
        );
        OrderCreatedEvent event = OrderCreatedEvent.from(order);

        // Total = 2×10 + 1×25 = 45
        assertEquals("45.00", event.amount());
        assertEquals(2, event.items().size());
    }
}
