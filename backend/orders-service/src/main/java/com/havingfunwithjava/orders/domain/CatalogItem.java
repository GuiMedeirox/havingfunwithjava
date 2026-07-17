package com.havingfunwithjava.orders.domain;

import java.util.UUID;

/**
 * Snapshot de um produto do catalog-service, retornado por {@link CatalogClient}.
 *
 * <p>É um DTO de domínio (não de infraestrutura): representa o que o orders-service
 * precisa saber de um produto para validar um pedido — id, nome, preço atual e
 * se está ativo. A implementação de {@code CatalogClient} (chamada REST real)
 * mora em infrastructure; o domínio só consome esta interface de retorno.
 *
 * @param productId  id do produto no catalog
 * @param name       nome do produto
 * @param price      preço atual ( amount + currency)
 * @param active     se o produto está ativo no catálogo
 */
public record CatalogItem(
        UUID productId,
        String name,
        Money price,
        boolean active
) {
}
