package com.havingfunwithjava.orders.domain;

import java.util.UUID;

/**
 * Evento de entrada: pagamento autorizado (publicado pelo payment-service).
 *
 * <p>Espelha o schema JSON do {@code PaymentSucceeded} do payment-service.
 * O consumer faz o pedido transitar PENDING_PAYMENT → PAID.
 */
public record PaymentSucceeded(UUID orderId, UUID paymentId) implements PaymentResultEvent {
}
