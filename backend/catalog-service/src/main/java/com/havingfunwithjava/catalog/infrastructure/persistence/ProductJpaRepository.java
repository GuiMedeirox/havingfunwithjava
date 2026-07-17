package com.havingfunwithjava.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório Spring Data JPA (adaptador técnico).
 *
 * Expõe operações CRUD simples sobre {@link ProductEntity}. As buscas filtradas/
 * paginadas ficam em {@link JpaProductRepository} via Criteria API (filtros
 * opcionais são tratados dinamicamente, evitando o bug de inferência de tipo do
 * Hibernate com parâmetros null em @Query).
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Apenas produtos ativos (não excluídos via soft-delete).
     */
    List<ProductEntity> findByActiveTrue();
}
