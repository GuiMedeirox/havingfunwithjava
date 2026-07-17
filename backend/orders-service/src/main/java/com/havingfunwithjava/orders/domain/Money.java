package com.havingfunwithjava.orders.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object: valor monetário (quantia + moeda).
 * Reaproveita o mesmo conceito do catalog-service (BigDecimal, sem double).
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

    /**
     * Soma dois valores monetários (mesma moeda).
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Não é possível somar moedas diferentes: " + currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), currency);
    }

    /**
     * Multiplica o valor por uma quantidade (para calcular subtotal de item).
     */
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }
}
