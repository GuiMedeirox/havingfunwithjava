package com.havingfunwithjava.orders.infrastructure.persistence;

import com.havingfunwithjava.orders.domain.CustomerId;
import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderId;
import com.havingfunwithjava.orders.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador: implementa a porta de domínio {@link OrderRepository} via JPA.
 */
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public JpaOrderRepository(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity saved = jpaRepository.save(OrderMapper.toEntity(order));
        return OrderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(OrderMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomer(CustomerId customerId) {
        return jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId.value()).stream()
                .map(OrderMapper::toDomain)
                .toList();
    }
}
