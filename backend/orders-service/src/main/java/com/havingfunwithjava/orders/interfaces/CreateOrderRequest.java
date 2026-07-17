package com.havingfunwithjava.orders.interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

/**
 * DTO de request para criar um pedido (POST /orders).
 */
public record CreateOrderRequest(
        @NotNull(message = "customerId é obrigatório")
        UUID customerId,

        @NotEmpty(message = "items não pode ser vazio")
        @Valid
        List<ItemRequest> items
) {

    public record ItemRequest(
            @NotNull(message = "productId é obrigatório")
            UUID productId,

            @Positive(message = "quantity deve ser maior que zero")
            int quantity,

            @NotNull(message = "expectedUnitPrice é obrigatório")
            String expectedUnitPrice,

            @NotNull(message = "currency é obrigatório")
            String currency
    ) {
    }
}
