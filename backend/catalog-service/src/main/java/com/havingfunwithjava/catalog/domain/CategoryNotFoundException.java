package com.havingfunwithjava.catalog.domain;

/**
 * Exceção de domínio: categoria não encontrada.
 * Traduzida para HTTP 404 pela camada de interfaces.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(CategoryId id) {
        super("Categoria não encontrada: " + id.value());
    }
}
