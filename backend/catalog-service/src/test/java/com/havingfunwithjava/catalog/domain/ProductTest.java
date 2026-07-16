package com.havingfunwithjava.catalog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de domínio (Seam 2): valida invariantes da entidade Product.
 * JUnit puro, sem Spring — prova que o domínio é independente de infraestrutura.
 */
class ProductTest {

    private final CategoryId categoryId = CategoryId.generate();

    @Test
    void shouldCreateValidProduct() {
        Product product = Product.createNew(
                "Notebook", "Notebook gamer 16GB",
                Money.of("4500.00", "BRL"), categoryId);

        assertNotNull(product.id());
        assertEquals("Notebook", product.name());
        assertEquals("4500.00", product.price().amount().toString());
        assertEquals("BRL", product.price().currency());
        assertTrue(product.active());
    }

    @Test
    void shouldRejectBlankName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Product.createNew("  ", "desc", Money.of("10.00", "BRL"), categoryId));
        assertTrue(ex.getMessage().toLowerCase().contains("nome"));
    }

    @Test
    void shouldRejectZeroPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.createNew("X", "desc", Money.of("0", "BRL"), categoryId));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.createNew("X", "desc", Money.of("-5.00", "BRL"), categoryId));
    }

    @Test
    void shouldRejectNullCategoryId() {
        assertThrows(NullPointerException.class, () ->
                Product.createNew("X", "desc", Money.of("10.00", "BRL"), null));
    }

    @Test
    void moneyShouldRejectBlankCurrency() {
        assertThrows(IllegalArgumentException.class, () ->
                new Money(new BigDecimal("10.00"), "  "));
    }
}
