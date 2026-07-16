package com.havingfunwithjava.catalog.infrastructure.persistence;

import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Money;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;

/**
 * Mapper (adaptador): traduz entre o modelo de domínio {@link Product} e a
 * entidade JPA {@link ProductEntity}.
 *
 * Manter a tradução isolada é o que permite ao domínio permanecer puro (sem
 * anotações JPA). Se amanhã trocarmos JPA por outra coisa, só este arquivo muda.
 */
final class ProductMapper {

    private ProductMapper() {
    }

    /**
     * Domínio → JPA.
     */
    static ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.id().value(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.price().currency(),
                product.categoryId().value(),
                product.active()
        );
    }

    /**
     * JPA → domínio.
     */
    static Product toDomain(ProductEntity entity) {
        return new Product(
                new ProductId(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                new Money(entity.getPriceAmount(), entity.getPriceCurrency()),
                new CategoryId(entity.getCategoryId()),
                entity.isActive()
        );
    }
}
