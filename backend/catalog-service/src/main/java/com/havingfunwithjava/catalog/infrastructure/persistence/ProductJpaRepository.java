package com.havingfunwithjava.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório Spring Data JPA (adaptador técnico).
 *
 * Expõe operações CRUD sobre {@link ProductEntity}. Usado internamente por
 * {@link JpaProductRepository}, que implementa a porta de domínio.
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Apenas produtos ativos (não excluídos via soft-delete).
     */
    List<ProductEntity> findByActiveTrue();
}
