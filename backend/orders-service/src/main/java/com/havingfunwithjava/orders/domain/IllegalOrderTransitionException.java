package com.havingfunwithjava.orders.domain;

/**
 * Exceção de domínio: tentativa de transição inválida na máquina de estados do pedido.
 *
 * <p>Lançada pelos métodos de transição de {@link Order} (markAsPaid, markAsPaymentFailed,
 * cancel) quando o estado atual não permite a transição solicitada. Traduzida para
 * HTTP 409 Conflict pela camada de interfaces (o pedido está num estado incompatível
 * com a operação desejada).
 */
public class IllegalOrderTransitionException extends RuntimeException {

    public IllegalOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("Transição inválida de pedido: " + from + " → " + to);
    }
}
