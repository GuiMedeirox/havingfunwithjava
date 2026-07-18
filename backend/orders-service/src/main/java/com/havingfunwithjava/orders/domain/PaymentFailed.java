package com.havingfunwithjava.orders.domain;

import java.util.UUID;

/**
 * Evento de entrada: pagamento falhou (publicado pelo payment-service).
 *
 * <p>Espelha o schema JSON do {@code PaymentFailed} do payment-service.
 * O consumer faz o pedido transitar PENDING_PAYMENT → PAYMENT_FAILED → CANCELLED.
 */
public record PaymentFailed(UUID orderId, String reason) implements PaymentResultEvent {
}
