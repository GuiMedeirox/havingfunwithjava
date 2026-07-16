package com.havingfunwithjava.catalog.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object: valor monetário (quantia + moeda).
 *
 * Usa BigDecimal para evitar problemas de precisão de ponto flutuante — regra
 * de ouro para dinheiro. Imutável. Vive no domínio — puro, sem Spring.
 *
 * Invariante: valor não pode ser nulo; moeda não pode ser vazia. O valor pode
 * ser zero (para "frete grátis", por exemplo), mas a validação de preço > 0
 * fica na entidade {@link Product}, que conhece a regra de negócio.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "valor (amount) não pode ser nulo");
        Objects.requireNonNull(currency, "moeda (currency) não pode ser nula");
        if (currency.isBlank()) {
            throw new IllegalArgumentException("moeda (currency) não pode ser vazia");
        }
    }

    /**
     * Cria um Money a partir de um valor e código ISO de moeda (ex.: "BRL", "USD").
     */
    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }
}
