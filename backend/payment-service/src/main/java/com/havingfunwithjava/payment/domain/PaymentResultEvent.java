package com.havingfunwithjava.payment.domain;

import java.util.UUID;

/**
 * Marker interface: resultado de um pagamento a ser publicado no broker.
 *
 * <p>Implementado por {@link PaymentSucceeded} e {@link PaymentFailed}. O consumidor
 * (orders-service, issue #22) faz pattern matching para decidir qual transição de
 * status aplicar ao pedido.
 */
public interface PaymentResultEvent {

    /**
     * @return o id do pedido cujo pagamento foi processado.
     */
    UUID orderId();
}
