package com.havingfunwithjava.catalog.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Produtos ativos filtrados opcionalmente por categoria, paginados.
     * Se categoryId for null/empty, retorna todos os ativos.
     */
    @Query("""
            select p from ProductEntity p
            where p.active = true
              and (:categoryId is null or p.categoryId = :categoryId)
            """)
    org.springframework.data.domain.Page<ProductEntity> findActiveByCategory(
            @Param("categoryId") UUID categoryId, Pageable pageable);

    /**
     * Conta produtos ativos, opcionalmente filtrados por categoria.
     */
    @Query("""
            select count(p) from ProductEntity p
            where p.active = true
              and (:categoryId is null or p.categoryId = :categoryId)
            """)
    long countActiveByCategory(@Param("categoryId") UUID categoryId);
}
