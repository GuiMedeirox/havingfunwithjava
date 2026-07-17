package com.havingfunwithjava.orders.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object: identificador de cliente (quem fez o pedido).
 */
public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "CustomerId não pode ser nulo");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
