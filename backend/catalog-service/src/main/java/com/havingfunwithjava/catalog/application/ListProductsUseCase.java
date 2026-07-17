package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Page;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: listar produtos.
 *
 * Lê via a porta {@link ProductRepository}. Expõe dois métodos:
 * - execute() → lista completa (sem paginação, para casos simples)
 * - execute(categoryId, searchTerm, page, size) → página filtrada (issues #5/#6)
 */
@Service
public class ListProductsUseCase {

    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> execute() {
        return productRepository.findAll();
    }

    /**
     * Lista paginada, opcionalmente filtrada por categoria e termo de busca.
     *
     * @param categoryId filtro de categoria; null ignora (todos os ativos)
     * @param searchTerm termo de busca no nome (case-insensitive); null/blank ignora
     * @param page       índice base-0
     * @param size       tamanho da página
     */
    public Page<Product> execute(CategoryId categoryId, String searchTerm, int page, int size) {
        return productRepository.findActive(categoryId, searchTerm, page, size);
    }
}
