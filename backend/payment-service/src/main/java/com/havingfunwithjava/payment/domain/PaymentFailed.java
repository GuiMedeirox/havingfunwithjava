package com.havingfunwithjava.payment.domain;

import java.util.UUID;

/**
 * Evento: pagamento falhou (recusado pelo gateway ou falha técnica após retentativas).
 *
 * <p>Publicado quando o gateway recusa o pagamento definitivamente. O orders-service
 * (issue #22) consome e marca o pedido como PAYMENT_FAILED → CANCELLED.
 *
 * @param orderId id do pedido
 * @param reason  motivo da falha (ex.: "cartão recusado", "valor excede limite")
 */
public record PaymentFailed(UUID orderId, String reason) implements PaymentResultEvent {
}
