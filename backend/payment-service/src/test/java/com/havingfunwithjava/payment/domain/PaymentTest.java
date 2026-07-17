package com.havingfunwithjava.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste de domínio (Seam 2): transições de status e invariantes de Payment.
 * JUnit puro, sem Spring.
 */
class PaymentTest {

    private Payment newPending() {
        return Payment.createNew(UUID.randomUUID(), PaymentMethod.CREDIT_CARD,
                new Money(new BigDecimal("100.00"), "BRL"));
    }

    @Test
    void shouldBeCreatedPendingWithOneAttempt() {
        Payment p = newPending();
        assertEquals(PaymentStatus.PENDING, p.status());
        assertEquals(1, p.attempts());
        assertNotNull(p.id());
    }

    @Test
    void shouldAuthorizeFromPending() {
        assertEquals(PaymentStatus.AUTHORIZED, newPending().authorize().status());
    }

    @Test
    void shouldDeclineFromPending() {
        assertEquals(PaymentStatus.DECLINED, newPending().decline().status());
    }

    @Test
    void shouldFailFromPending() {
        assertEquals(PaymentStatus.FAILED, newPending().fail().status());
    }

    @Test
    void shouldRejectAuthorizeFromTerminal() {
        assertThrows(IllegalStateException.class, () -> newPending().authorize().authorize());
    }

    @Test
    void shouldRejectDeclineFromAuthorized() {
        assertThrows(IllegalStateException.class, () -> newPending().authorize().decline());
    }

    @Test
    void incrementAttemptsShouldPreserveOtherFields() {
        Payment p = newPending();
        Payment incremented = p.incrementAttempts();
        assertEquals(2, incremented.attempts());
        assertEquals(p.id(), incremented.id());
        assertEquals(p.orderId(), incremented.orderId());
        assertEquals(p.status(), incremented.status());
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                Payment.createNew(UUID.randomUUID(), PaymentMethod.PIX,
                        new Money(new BigDecimal("0"), "BRL")));
    }
}
