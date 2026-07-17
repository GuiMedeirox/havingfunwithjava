package com.havingfunwithjava.orders.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste de domínio (Seam 2): invariantes e cálculo de total de Order/OrderItem.
 * JUnit puro, sem Spring — prova que o domínio é independente.
 */
class OrderTest {

    private OrderItem item(String name, int qty, String price) {
        return new OrderItem(UUID.randomUUID(), name, qty, new BigDecimal(price), "BRL");
    }

    @Test
    void shouldCalculateTotalAsSumOfSubtotals() {
        Order order = Order.createNew(
                new CustomerId(UUID.randomUUID()),
                List.of(item("Mouse", 2, "50.00"), item("Teclado", 1, "150.00"))
        );
        // 2×50 + 1×150 = 250
        assertEquals(new BigDecimal("250.00"), order.total().amount());
        assertEquals("BRL", order.currency());
    }

    @Test
    void shouldBeCreatedInPendingPayment() {
        Order order = Order.createNew(new CustomerId(UUID.randomUUID()), List.of(item("X", 1, "10.00")));
        assertEquals(OrderStatus.PENDING_PAYMENT, order.status());
        assertNotNull(order.id());
        assertNotNull(order.createdAt());
    }

    @Test
    void shouldRejectEmptyItems() {
        assertThrows(IllegalArgumentException.class, () ->
                Order.createNew(new CustomerId(UUID.randomUUID()), List.of()));
    }

    @Test
    void shouldRejectMixedCurrencies() {
        OrderItem usd = new OrderItem(UUID.randomUUID(), "USD Item", 1, new BigDecimal("10.00"), "USD");
        OrderItem brl = new OrderItem(UUID.randomUUID(), "BRL Item", 1, new BigDecimal("10.00"), "BRL");
        assertThrows(IllegalArgumentException.class, () ->
                Order.createNew(new CustomerId(UUID.randomUUID()), List.of(usd, brl)));
    }

    @Test
    void shouldRejectItemWithNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(UUID.randomUUID(), "X", 0, new BigDecimal("10.00"), "BRL"));
    }

    @Test
    void shouldRejectItemWithNonPositivePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(UUID.randomUUID(), "X", 1, new BigDecimal("0"), "BRL"));
    }

    @Test
    void itemsShouldBeImmutable() {
        Order order = Order.createNew(
                new CustomerId(UUID.randomUUID()),
                List.of(item("X", 1, "10.00")));
        // Tentar modificar a lista retornada deve falhar (List.copyOf)
        assertThrows(UnsupportedOperationException.class, () -> order.items().add(item("Y", 1, "5.00")));
    }
}
