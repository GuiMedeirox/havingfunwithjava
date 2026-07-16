package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.domain.Page;
import com.havingfunwithjava.catalog.domain.Product;

import java.util.List;

/**
 * DTO de resposta paginada de produtos.
 *
 * Plano e estável — desacoplado do modelo de domínio. Inclui metadados de
 * paginação (totalItems, totalPages, page, size) para o front navegar.
 */
public record PagedProductsResponse(
        List<ProductResponse> items,
        long totalItems,
        int totalPages,
        int page,
        int size
) {

    public static PagedProductsResponse from(Page<Product> page) {
        List<ProductResponse> items = page.items().stream()
                .map(ProductResponse::from)
                .toList();
        return new PagedProductsResponse(
                items,
                page.totalItems(),
                page.totalPages(),
                page.page(),
                page.size()
        );
    }
}
