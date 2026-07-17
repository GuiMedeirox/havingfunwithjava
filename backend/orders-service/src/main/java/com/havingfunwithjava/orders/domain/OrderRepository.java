package com.havingfunwithjava.orders.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de domínio: repositório de pedidos.
 *
 * <p>Interface pura (porta) declarada no domínio. A implementação JPA vive em
 * infrastructure. Usada pelos casos de uso.
 */
public interface OrderRepository {

    /**
     * Persiste (ou atualiza) um pedido. Retorna a instância persistida.
     */
    Order save(Order order);

    /**
     * Busca um pedido pelo id. Vazio se não existir.
     */
    Optional<Order> findById(OrderId id);

    /**
     * Lista pedidos de um cliente (ordenados por criação, mais recente primeiro).
     */
    List<Order> findByCustomer(CustomerId customerId);
}
