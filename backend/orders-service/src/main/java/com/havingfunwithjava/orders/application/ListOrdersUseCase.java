package com.havingfunwithjava.orders.application;

import com.havingfunwithjava.orders.domain.CustomerId;
import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: listar pedidos de um cliente.
 *
 * <p>Delega ao {@link OrderRepository#findByCustomer}. Em produção, o customerId
 * vem do claim do JWT (repassado pelo gateway); neste slice, aceito via parâmetro
 * para não acoplar a auth (que mora no gateway).
 */
@Service
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> execute(CustomerId customerId) {
        return orderRepository.findByCustomer(customerId);
    }
}
