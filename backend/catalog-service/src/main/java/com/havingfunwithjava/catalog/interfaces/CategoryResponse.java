package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.domain.Category;

import java.util.UUID;

/**
 * DTO de response: representa uma categoria no payload HTTP.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        UUID parentId
) {

    public static CategoryResponse from(Category category) {
        UUID parentId = category.parentId() == null ? null : category.parentId().value();
        return new CategoryResponse(
                category.id().value(),
                category.name(),
                category.slug(),
                parentId
        );
    }
}
