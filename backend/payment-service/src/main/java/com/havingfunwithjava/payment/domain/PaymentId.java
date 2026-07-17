package com.havingfunwithjava.payment.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object: identificador de pagamento.
 */
public record PaymentId(UUID value) {

    public PaymentId {
        Objects.requireNonNull(value, "PaymentId não pode ser nulo");
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
