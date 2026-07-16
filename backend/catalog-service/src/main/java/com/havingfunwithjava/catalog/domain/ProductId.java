package com.havingfunwithjava.catalog.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object: identificador de produto.
 *
 * Tipagem forte para não confundir com outros UUIDs no domínio. Imutável.
 */
public record ProductId(UUID value) {

    public ProductId {
        Objects.requireNonNull(value, "ProductId não pode ser nulo");
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
