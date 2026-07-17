package com.havingfunwithjava.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste de domínio (Seam 2): valida invariantes da entidade Category.
 * JUnit puro, sem Spring.
 */
class CategoryTest {

    @Test
    void shouldCreateValidCategoryWithoutParent() {
        Category category = Category.createNew("Eletrônicos", "eletronicos");

        assertNotNull(category.id());
        assertEquals("Eletrônicos", category.name());
        assertEquals("eletronicos", category.slug());
        assertNull(category.parentId());
    }

    @Test
    void shouldCreateCategoryWithParent() {
        CategoryId parentId = CategoryId.generate();
        Category category = Category.createNew("Notebooks", "notebooks", parentId);

        assertEquals(parentId, category.parentId());
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.createNew("  ", "slug"));
    }

    @Test
    void shouldRejectBlankSlug() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.createNew("Nome", "  "));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                Category.createNew(null, "slug"));
    }
}
