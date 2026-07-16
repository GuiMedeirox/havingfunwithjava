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
 * Lê via a porta {@link ProductRepository}. Expor dois métodos:
 * - execute() → lista completa (sem paginação, para casos simples)
 * - execute(categoryId, page, size) → página filtrada (issue #5)
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
     * Lista paginada, opcionalmente filtrada por categoria.
     *
     * @param categoryId filtro; null ignora o filtro (todos os ativos)
     * @param page       índice base-0
     * @param size       tamanho da página
     */
    public Page<Product> execute(CategoryId categoryId, int page, int size) {
        return productRepository.findActive(categoryId, page, size);
    }
}
