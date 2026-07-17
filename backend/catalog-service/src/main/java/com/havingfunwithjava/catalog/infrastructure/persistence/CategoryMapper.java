package com.havingfunwithjava.catalog.infrastructure.persistence;

import com.havingfunwithjava.catalog.domain.Category;
import com.havingfunwithjava.catalog.domain.CategoryId;

import java.util.UUID;

/**
 * Mapper (adaptador): traduz entre o modelo de domínio {@link Category} e a
 * entidade JPA {@link CategoryEntity}.
 */
final class CategoryMapper {

    private CategoryMapper() {
    }

    static CategoryEntity toEntity(Category category) {
        UUID parentId = category.parentId() == null ? null : category.parentId().value();
        return new CategoryEntity(category.id().value(), category.name(), category.slug(), parentId);
    }

    static Category toDomain(CategoryEntity entity) {
        CategoryId parentId = entity.getParentId() == null ? null : new CategoryId(entity.getParentId());
        return new Category(
                new CategoryId(entity.getId()),
                entity.getName(),
                entity.getSlug(),
                parentId
        );
    }
}
