package com.havingfunwithjava.orders.domain;

import java.util.List;
import java.util.UUID;

/**
 * Porta de domínio: cliente do catalog-service.
 *
 * <p>Abstração da chamada REST ao catalog-service para validar itens de um pedido.
 * Vive no domínio como interface (porta); a implementação concreta (WebClient/
 * RestTemplate) mora em infrastructure. Em testes, é mockada — o domínio não
 * sabe que há HTTP embaixo.
 */
public interface CatalogClient {

    /**
     * Busca os dados atuais de uma lista de produtos no catalog-service.
     *
     * @param productIds ids dos produtos a validar
     * @return lista de {@link CatalogItem} para os produtos encontrados e ativos.
     *         Produtos inexistentes ou inativos SÃO OMITIDOS da lista — o chamador
     *         detecta a divergência comparando com os ids solicitados.
     */
    List<CatalogItem> findProductsByIds(List<UUID> productIds);
}
