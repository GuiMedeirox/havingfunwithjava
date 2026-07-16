package com.havingfunwithjava.catalog.domain;

import java.util.List;

/**
 * Página de resultados (value object de domínio).
 *
 * Abstração de paginação que vive no domínio, independente de Spring Data
 * (não vaza Pageable/PageImpl para o domínio). Contém os itens da página atual
 * e metadados suficientes para o cliente navegar (total, página atual, tamanho,
 * total de páginas).
 *
 * @param items      itens da página atual (possivelmente vazia)
 * @param totalItems total absoluto de itens que casam com o filtro
 * @param page       índice da página atual (base-0)
 * @param size       tamanho da página
 */
public record Page<T>(
        List<T> items,
        long totalItems,
        int page,
        int size
) {

    /**
     * Total de páginas (mínimo 1, mesmo com zero itens, para consistência de resposta).
     */
    public int totalPages() {
        return size <= 0 ? 1 : (int) Math.ceil((double) totalItems / size);
    }
}
