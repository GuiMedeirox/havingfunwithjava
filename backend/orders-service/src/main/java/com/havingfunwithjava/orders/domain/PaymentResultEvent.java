package com.havingfunwithjava.orders.domain;

import java.util.UUID;

/**
 * Marker interface para eventos de resultado de pagamento consumidos do payment-service.
 *
 * <p>Implementado por {@link PaymentSucceeded} e {@link PaymentFailed}. O consumer
 * faz pattern matching para decidir qual transição de status aplicar ao pedido.
 */
public interface PaymentResultEvent {

    UUID orderId();
}
