package com.havingfunwithjava.orders.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade de domínio: Pedido (agregado raiz).
 *
 * <p>POJO puro (sem JPA, sem Spring). Invariantes no construtor:
 * <ul>
 *   <li>cliente não-nulo</li>
 *   <li>ao menos 1 item</li>
 *   <li>todos os itens na mesma moeda</li>
 * </ul>
 *
 * <p>O total é CALCULADO a partir dos itens (não armazenado como campo mutável):
 * soma dos subtotais. O status nasce como {@link OrderStatus#PENDING_PAYMENT};
 * transições são formalizadas na issue #17 (aqui só expomos o accessor).
 *
 * @param id          identificador (gerado ao criar)
 * @param customerId  quem comprou
 * @param items       linhas do pedido (imutável após criação)
 * @param status      estado atual
 * @param createdAt   instante de criação
 */
public record Order(
        OrderId id,
        CustomerId customerId,
        List<OrderItem> items,
        OrderStatus status,
        Instant createdAt
) {

    public Order {
        Objects.requireNonNull(customerId, "customerId não pode ser nulo");
        Objects.requireNonNull(items, "items não pode ser nulo");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("pedido deve ter ao menos 1 item");
        }
        // Itens imutáveis externamente
        items = List.copyOf(items);
        // Todos os itens na mesma moeda
        String firstCurrency = items.get(0).currency();
        for (OrderItem item : items) {
            if (!firstCurrency.equals(item.currency())) {
                throw new IllegalArgumentException(
                        "todos os itens devem ter a mesma moeda: esperado " + firstCurrency
                                + ", encontrado " + item.currency());
            }
        }
        Objects.requireNonNull(status, "status não pode ser nulo");
    }

    /**
     * Factory: cria um novo pedido em PENDING_PAYMENT com timestamp atual.
     */
    public static Order createNew(CustomerId customerId, List<OrderItem> items) {
        return new Order(
                OrderId.generate(),
                customerId,
                items,
                OrderStatus.PENDING_PAYMENT,
                Instant.now()
        );
    }

    /**
     * Total do pedido: soma dos subtotais dos itens ( Money na moeda do pedido).
     */
    public Money total() {
        Money sum = items.get(0).subtotal();
        for (int i = 1; i < items.size(); i++) {
            sum = sum.add(items.get(i).subtotal());
        }
        return sum;
    }

    /**
     * Moeda do pedido (tomada do primeiro item — todos são iguais por invariante).
     */
    public String currency() {
        return items.get(0).currency();
    }
}
