package com.havingfunwithjava.orders.domain;

/**
 * Exceção de domínio: pedido não encontrado.
 * Traduzida para HTTP 404 pela camada de interfaces.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId id) {
        super("Pedido não encontrado: " + id.value());
    }
}
