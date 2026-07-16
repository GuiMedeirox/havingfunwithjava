package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: listar produtos.
 *
 * Lê via a porta {@link ProductRepository}. Apenas delega — sem regra de negócio
 * adicional neste slice. Filtros/paginação entram nas issues #5/#6.
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
}
