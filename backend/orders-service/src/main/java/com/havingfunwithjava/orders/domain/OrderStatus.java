package com.havingfunwithjava.orders.domain;

/**
 * Estado de um pedido no ciclo de vida de pagamento.
 *
 * <p>Transições válidas (máquina de estados — formalizada na issue #17):
 * <pre>
 *   PENDING_PAYMENT ──→ PAID            (pagamento confirmado)
 *   PENDING_PAYMENT ──→ PAYMENT_FAILED (pagamento falhou definitivamente)
 *   PAYMENT_FAILED  ──→ CANCELLED      (cancelamento automático)
 * </pre>
 *
 * <p>Estados terminais: PAID, CANCELLED.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
