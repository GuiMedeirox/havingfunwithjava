package com.havingfunwithjava.orders.application;

import java.util.List;
import java.util.UUID;

/**
 * Comando (input) para criar um pedido.
 *
 * <p>DTO plano que chega da camada de interfaces. Cada item traz o productId
 * solicitado, a quantidade, o preço unitário que o cliente acredita ser o atual
 * (para checagem contra o catalog) e a moeda. O caso de uso valida tudo.
 *
 * @param customerId id do cliente
 * @param items      itens solicitados (productId, quantity, expectedUnitPrice, currency)
 */
public record CreateOrderCommand(
        UUID customerId,
        List<ItemRequest> items
) {

    /**
     * Item solicitado no comando.
     *
     * @param productId         id do produto
     * @param quantity          quantidade (> 0)
     * @param expectedUnitPrice preço unitário que o cliente espera pagar (string p/ precisão)
     * @param currency          moeda ISO
     */
    public record ItemRequest(
            UUID productId,
            int quantity,
            String expectedUnitPrice,
            String currency
    ) {
    }
}
