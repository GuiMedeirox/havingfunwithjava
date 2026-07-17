package com.havingfunwithjava.orders.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object: identificador de pedido.
 */
public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "OrderId não pode ser nulo");
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
