package com.havingfunwithjava.catalog.domain;

/**
 * Exceção de domínio: produto não encontrado.
 *
 * Lançada pelos casos de uso quando um produto solicitado não existe no
 * repositório. É uma exceção de negócio (não de infraestrutura) — representa
 * uma situação esperada do domínio. A camada de interfaces a traduz para HTTP
 * 404 via {@code GlobalExceptionHandler}.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(ProductId id) {
        super("Produto não encontrado: " + id.value());
    }
}
