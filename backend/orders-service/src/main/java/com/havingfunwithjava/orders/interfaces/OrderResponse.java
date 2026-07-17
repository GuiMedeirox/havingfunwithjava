package com.havingfunwithjava.orders.interfaces;

import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de response: representa um pedido no payload HTTP.
 *
 * <p>Inclui o total calculado (não persistido) e os itens com seus subtotais.
 */
public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        Instant createdAt,
        String totalAmount,
        String currency,
        List<ItemResponse> items
) {

    public static OrderResponse from(Order order) {
        List<ItemResponse> items = order.items().stream()
                .map(ItemResponse::from)
                .toList();
        return new OrderResponse(
                order.id().value(),
                order.customerId().value(),
                order.status().name(),
                order.createdAt(),
                order.total().amount().toPlainString(),
                order.currency(),
                items
        );
    }

    public record ItemResponse(
            UUID productId,
            String productName,
            int quantity,
            String unitPrice,
            String subtotal,
            String currency
    ) {

        static ItemResponse from(OrderItem item) {
            return new ItemResponse(
                    item.productId(),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice().toPlainString(),
                    item.subtotal().amount().toPlainString(),
                    item.currency()
            );
        }
    }
}
