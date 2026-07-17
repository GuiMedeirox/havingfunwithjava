package com.havingfunwithjava.orders.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório Spring Data JPA (adaptador técnico) de pedidos.
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Pedidos de um cliente, ordenados pelo mais recente (createdAt desc).
     */
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
