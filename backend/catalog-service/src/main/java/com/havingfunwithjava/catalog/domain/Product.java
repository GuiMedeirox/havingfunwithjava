package com.havingfunwithjava.catalog.domain;

import java.util.Objects;

/**
 * Entidade de domínio: Produto.
 *
 * É um POJO puro — sem anotações JPA, sem Spring. As invariantes de negócio são
 * garantidas no construtor (fail-fast): nome não-vazio, preço positivo, moeda
 * e categoria obrigatórias. A persistência é traduzida por um mapper na camada
 * de infrastructure; o domínio não conhece esse detalhe.
 *
 * @param id          identificador único (null antes de persistir na 1ª vez é
 *                    aceito para novos produtos; o repositório atribui)
 * @param name        nome de exibição (não-vazio)
 * @param description descrição detalhada (pode ser vazia)
 * @param price       preço (valor + moeda) — deve ter amount > 0
 * @param categoryId  categoria à qual pertence
 * @param active      se está visível no catálogo
 */
public record Product(
        ProductId id,
        String name,
        String description,
        Money price,
        CategoryId categoryId,
        boolean active
) {

    public Product {
        Objects.requireNonNull(name, "nome não pode ser nulo");
        if (name.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
        Objects.requireNonNull(price, "preço não pode ser nulo");
        Objects.requireNonNull(categoryId, "categoryId não pode ser nulo");
        if (price.amount().signum() <= 0) {
            throw new IllegalArgumentException("preço deve ser maior que zero");
        }
    }

    /**
     * Factory para um novo produto (ativo por padrão), gerando um id novo.
     */
    public static Product createNew(String name, String description, Money price, CategoryId categoryId) {
        return new Product(ProductId.generate(), name, description, price, categoryId, true);
    }
}
