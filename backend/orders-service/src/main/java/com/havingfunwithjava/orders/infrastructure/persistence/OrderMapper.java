package com.havingfunwithjava.orders.infrastructure.persistence;

import com.havingfunwithjava.orders.domain.CustomerId;
import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderId;
import com.havingfunwithjava.orders.domain.OrderItem;
import com.havingfunwithjava.orders.domain.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper (adaptador): traduz entre {@link Order} (domínio) e {@link OrderEntity} (JPA).
 *
 * <p>O total NÃO é persistido (sempre derivado dos itens). O status é serializado
 * como string (enum name). Os itens são mapeados bidirecionalmente (o lado filho
 * precisa da referência ao pai p/ o Hibernate salvar a FK).
 */
final class OrderMapper {

    private OrderMapper() {
    }

    /**
     * Domínio → JPA. Conecta cada item ao pedido pai (necessário p/ cascade).
     */
    static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity(
                order.id().value(),
                order.customerId().value(),
                order.currency(),
                order.status().name(),
                order.createdAt()
        );
        List<OrderItemEntity> itemEntities = new ArrayList<>();
        for (OrderItem item : order.items()) {
            itemEntities.add(new OrderItemEntity(
                    entity,
                    item.productId(),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice(),
                    item.currency()
            ));
        }
        entity.setItems(itemEntities);
        return entity;
    }

    /**
     * JPA → domínio.
     */
    static Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(ie -> new OrderItem(
                        ie.getProductId(),
                        ie.getProductName(),
                        ie.getQuantity(),
                        ie.getUnitPrice(),
                        ie.getCurrency()
                ))
                .toList();
        return new Order(
                new OrderId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                items,
                OrderStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }
}
