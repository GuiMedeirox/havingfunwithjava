package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.domain.Product;

import java.util.UUID;

/**
 * DTO de response: representa um produto no payload de resposta HTTP.
 *
 * Plano e estável — desacoplado do modelo de domínio. Conversão explícita via
 * {@link #from(Product)} para não vazar detalhes do domínio para o cliente.
 */
public record ProductResponse(
        UUID id,
        String name,
        String description,
        String amount,
        String currency,
        UUID categoryId,
        boolean active
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id().value(),
                product.name(),
                product.description(),
                product.price().amount().toPlainString(),
                product.price().currency(),
                product.categoryId().value(),
                product.active()
        );
    }
}
