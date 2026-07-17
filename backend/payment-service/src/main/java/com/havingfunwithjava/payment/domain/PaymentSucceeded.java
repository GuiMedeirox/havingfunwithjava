package com.havingfunwithjava.payment.domain;

import java.util.UUID;

/**
 * Evento: pagamento autorizado com sucesso.
 *
 * <p>Publicado quando o gateway autoriza o pagamento. O orders-service (issue #22)
 * consome e marca o pedido como PAID.
 *
 * @param orderId   id do pedido
 * @param paymentId id do pagamento (para auditoria/rastreio)
 */
public record PaymentSucceeded(UUID orderId, UUID paymentId) implements PaymentResultEvent {
}
