package com.havingfunwithjava.payment.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object: valor monetário (quantia + moeda).
 * Mesma semântica do Money do orders-service (BigDecimal, sem double).
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "valor (amount) não pode ser nulo");
        Objects.requireNonNull(currency, "moeda (currency) não pode ser nula");
        if (currency.isBlank()) {
            throw new IllegalArgumentException("moeda (currency) não pode ser vazia");
        }
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }
}
