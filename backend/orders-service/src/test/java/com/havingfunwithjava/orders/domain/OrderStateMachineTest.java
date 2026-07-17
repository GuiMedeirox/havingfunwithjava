package com.havingfunwithjava.orders.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste de domínio (Seam 2): máquina de estados do pedido (issue #17).
 *
 * <p>Cobre TODAS as transições válidas e as principais inválidas, garantindo
 * que a lógica de {@link Order} protege o agregado contra estados ilegais.
 */
class OrderStateMachineTest {

    private Order newPendingOrder() {
        return Order.createNew(
                new CustomerId(UUID.randomUUID()),
                List.of(new OrderItem(UUID.randomUUID(), "X", 1, new BigDecimal("10.00"), "BRL"))
        );
    }

    @Nested
    class ValidTransitions {
        @Test
        void pendingToPaid() {
            Order paid = newPendingOrder().markAsPaid();
            assertEquals(OrderStatus.PAID, paid.status());
        }

        @Test
        void pendingToPaymentFailed() {
            Order failed = newPendingOrder().markAsPaymentFailed();
            assertEquals(OrderStatus.PAYMENT_FAILED, failed.status());
        }

        @Test
        void paymentFailedToCancelled() {
            Order cancelled = newPendingOrder().markAsPaymentFailed().cancel();
            assertEquals(OrderStatus.CANCELLED, cancelled.status());
        }
    }

    @Nested
    class InvalidTransitions {
        @Test
        void cannotMarkPaidTwice() {
            Order paid = newPendingOrder().markAsPaid();
            assertThrows(IllegalOrderTransitionException.class, paid::markAsPaid);
        }

        @Test
        void cannotCancelFromPendingPayment() {
            // PENDING_PAYMENT → CANCELLED direto é inválido (deve passar por PAYMENT_FAILED)
            Order pending = newPendingOrder();
            assertThrows(IllegalOrderTransitionException.class, pending::cancel);
        }

        @Test
        void cannotCancelPaidOrder() {
            Order paid = newPendingOrder().markAsPaid();
            assertThrows(IllegalOrderTransitionException.class, paid::cancel);
        }

        @Test
        void cannotRecoverFromPaymentFailedToPaid() {
            // PAYMENT_FAILED → PAID direto é inválido (não há retry neste fluxo)
            Order failed = newPendingOrder().markAsPaymentFailed();
            assertThrows(IllegalOrderTransitionException.class, failed::markAsPaid);
        }

        @Test
        void cannotMarkAsPaymentFailedFromCancelled() {
            Order cancelled = newPendingOrder().markAsPaymentFailed().cancel();
            assertThrows(IllegalOrderTransitionException.class, cancelled::markAsPaymentFailed);
        }
    }

    @Test
    void transitionsPreserveIdentityAndItems() {
        Order pending = newPendingOrder();
        Order paid = pending.markAsPaid();

        // Mesmo id, customer, items — só o status muda (imutabilidade do record)
        assertEquals(pending.id(), paid.id());
        assertEquals(pending.customerId(), paid.customerId());
        assertEquals(pending.items(), paid.items());
        assertEquals(pending.createdAt(), paid.createdAt());
    }
}
