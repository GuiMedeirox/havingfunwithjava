package com.havingfunwithjava.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de domínio: repositório de produtos.
 *
 * Interface pura (porta) declarada no domínio. A implementação concreta vive em
 * infrastructure (adaptador JPA). Os casos de uso consomem esta interface —
 * eles não sabem se há JPA, SQL, ou arquivo embaixo.
 */
public interface ProductRepository {

    /**
     * Persiste (ou atualiza) um produto. Retorna a instância persistida com o id
     * atribuído (caso seja nova).
     */
    Product save(Product product);

    /**
     * Lista todos os produtos ativos. Vazia se não houver.
     */
    List<Product> findAll();

    /**
     * Busca um produto pelo id. Vazio se não existir.
     */
    Optional<Product> findById(ProductId id);

    /**
     * Busca produtos ativos com filtro opcional por categoria e paginação.
     *
     * @param categoryId filtro de categoria; se null, não filtra por categoria
     * @param page       índice da página (base-0)
     * @param size       tamanho da página
     * @return página de resultados com metadados (total, totalPages)
     */
    Page<Product> findActive(CategoryId categoryId, int page, int size);

    /**
     * Conta o total de produtos ativos (opcionalmente filtrados por categoria).
     */
    long countActive(CategoryId categoryId);
}
