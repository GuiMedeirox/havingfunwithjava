package com.havingfunwithjava.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de domínio: repositório de produtos.
 *
 * Interface pura (porta) declarada no domínio. A implementação concreta vive em
 * infrastructure (adaptador JPA). Os casos de uso consomem esta interface —
 * eles não sabem se há JPA, SQL, ou arquivo embaixo.
 *
 * Operações mínimas para o slice de CRUD (#3). Buscas/filtros/paginação entram
 * nas issues #5 e #6.
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
}
