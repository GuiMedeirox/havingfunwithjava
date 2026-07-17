package com.havingfunwithjava.orders.application;

import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderId;
import com.havingfunwithjava.orders.domain.OrderNotFoundException;
import com.havingfunwithjava.orders.domain.OrderRepository;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: obter um pedido pelo id.
 *
 * <p>Se o pedido não existir, lança {@link OrderNotFoundException} (404). O
 * isolamento por cliente (cliente não vê pedido alheio) é responsabilidade da
 * camada de interfaces, que conhece o customerId do JWT; aqui o caso de uso só
 * busca pelo id.
 */
@Service
public class GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order execute(OrderId id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
