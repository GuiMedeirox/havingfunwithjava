package com.havingfunwithjava.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de domínio: repositório de categorias.
 *
 * Interface pura (porta) declarada no domínio. A implementação JPA vive em
 * infrastructure. Usada pelos casos de uso de categoria.
 */
public interface CategoryRepository {

    /**
     * Persiste (ou atualiza) uma categoria.
     */
    Category save(Category category);

    /**
     * Lista todas as categorias.
     */
    List<Category> findAll();

    /**
     * Busca uma categoria pelo id. Vazio se não existir.
     */
    Optional<Category> findById(CategoryId id);

    /**
     * Verifica se já existe uma categoria com o slug informado.
     * Opcionalmente exclui um id da checagem (para updates do próprio registro).
     *
     * @param slug      slug a verificar
     * @param excludeId id a ignorar (null para checagem de criação)
     * @return true se o slug já existe em outra categoria
     */
    boolean existsBySlug(String slug, CategoryId excludeId);
}
