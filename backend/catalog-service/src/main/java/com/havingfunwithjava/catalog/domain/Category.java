package com.havingfunwithjava.catalog.domain;

import java.util.Objects;

/**
 * Entidade de domínio: Categoria de produto.
 *
 * POJO puro (sem JPA, sem Spring). Invariantes no construtor: nome não-vazio,
 * slug não-vazio. O slug deve ser único (regra aplicada no repositório/caso de
 * uso, que conhece as outras categorias). Suporte opcional a subcategoria via
 * parentId (auto-relacionamento).
 *
 * @param id       identificador (null antes da 1ª persistência é aceito)
 * @param name     nome de exibição (não-vazio)
 * @param slug     slug único para URLs (não-vazio, formato lowercase-hyphenated)
 * @param parentId id da categoria pai (null para categoria raiz)
 */
public record Category(
        CategoryId id,
        String name,
        String slug,
        CategoryId parentId
) {

    public Category {
        Objects.requireNonNull(name, "nome não pode ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
        Objects.requireNonNull(slug, "slug não pode ser nulo");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug não pode ser vazio");
        }
        // parentId pode ser null (categoria raiz) — não validamos aqui
    }

    /**
     * Factory para nova categoria (gera id novo), com ou sem pai.
     */
    public static Category createNew(String name, String slug, CategoryId parentId) {
        return new Category(CategoryId.generate(), name, slug, parentId);
    }

    public static Category createNew(String name, String slug) {
        return createNew(name, slug, null);
    }
}
