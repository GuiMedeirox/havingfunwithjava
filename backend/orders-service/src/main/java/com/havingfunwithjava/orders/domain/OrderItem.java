package com.havingfunwithjava.orders.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Item de linha de um pedido.
 *
 * <p>Invariantes: productId não-nulo, quantidade > 0, preço unitário > 0.
 * O subtotal é derivado (unitPrice × quantity).
 *
 * @param productId  identificador do produto no catalog-service
 * @param productName nome do produto no momento da compra (snapshot, para histórico)
 * @param quantity   quantidade solicitada (> 0)
 * @param unitPrice  preço unitário acordado (> 0, snapshot do preço na compra)
 * @param currency   moeda (ISO 4217) — deve bater entre itens do mesmo pedido
 */
public record OrderItem(
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {

    public OrderItem {
        Objects.requireNonNull(productId, "productId não pode ser nulo");
        Objects.requireNonNull(unitPrice, "unitPrice não pode ser nulo");
        Objects.requireNonNull(currency, "currency não pode ser nula");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantidade deve ser maior que zero");
        }
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("preço unitário deve ser maior que zero");
        }
    }

    /**
     * Subtotal do item: unitPrice × quantity (BigDecimal, mesma moeda).
     */
    public Money subtotal() {
        return new Money(unitPrice.multiply(BigDecimal.valueOf(quantity)), currency);
    }
}
