package com.havingfunwithjava.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório Spring Data JPA (adaptador técnico) de categorias.
 *
 * Mantido simples (apenas CRUD). A verificação de unicidade de slug é feita no
 * adaptador {@link JpaCategoryRepository} via dois métodos derivados, evitando
 * o bug de inferência de tipo do Hibernate com parâmetros null em @Query.
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

    /**
     * Existência de slug (para criação — sem exclusão).
     */
    boolean existsBySlug(String slug);

    /**
     * Existência de slug excluindo um id (para update do próprio registro).
     */
    boolean existsBySlugAndIdNot(String slug, UUID id);
}
