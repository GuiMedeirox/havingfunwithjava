package com.havingfunwithjava.orders.domain;

import java.util.List;
import java.util.UUID;

/**
 * Evento de domínio: pedido criado.
 *
 * <p>Publicado após a criação bem-sucedida de um pedido. Consumido assincronamente
 * pelo payment-service (issue #19) para iniciar o processamento de pagamento.
 *
 * <p>É um DTO de integração: contém apenas o necessário para o consumidor (orderId,
 * customerId, total em valor+moeda, e os itens com productId+quantidade+preço).
 * Não vaza a entidade {@link Order} inteira — interface estável entre serviços.
 *
 * <p>Campos são tipos primitivos/UUID/List (serializáveis em JSON via Jackson).
 *
 * @param orderId    id do pedido criado
 * @param customerId id do cliente que fez o pedido
 * @param amount     valor total (string para preservar precisão do BigDecimal)
 * @param currency   moeda ISO (ex.: "BRL")
 * @param items      itens do pedido (productId, quantity, unitPrice, currency)
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        String amount,
        String currency,
        List<Item> items
) {

    public record Item(
            UUID productId,
            String productName,
            int quantity,
            String unitPrice,
            String currency
    ) {
    }

    /**
     * Factory: constrói o evento a partir do agregado {@link Order}.
     */
    public static OrderCreatedEvent from(Order order) {
        List<Item> items = order.items().stream()
                .map(i -> new Item(
                        i.productId(),
                        i.productName(),
                        i.quantity(),
                        i.unitPrice().toPlainString(),
                        i.currency()))
                .toList();
        return new OrderCreatedEvent(
                order.id().value(),
                order.customerId().value(),
                order.total().amount().toPlainString(),
                order.currency(),
                items
        );
    }
}
