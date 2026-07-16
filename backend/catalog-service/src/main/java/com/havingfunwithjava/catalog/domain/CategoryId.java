package com.havingfunwithjava.catalog.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object: identificador de categoria.
 *
 * Wraps um UUID garantindo tipagem forte no domínio (não confundir com outros
 * UUIDs). Imutável. Vive no domínio — puro, sem Spring.
 */
public record CategoryId(UUID value) {

    public CategoryId {
        Objects.requireNonNull(value, "CategoryId não pode ser nulo");
    }

    /**
     * Gera um novo CategoryId aleatório.
     */
    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
