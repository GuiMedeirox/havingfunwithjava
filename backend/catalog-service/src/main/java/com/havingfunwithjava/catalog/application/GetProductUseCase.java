package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import com.havingfunwithjava.catalog.domain.ProductNotFoundException;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: obter um produto pelo id.
 *
 * Lê via a porta {@link ProductRepository}. Se o produto não existir, lança
 * {@link ProductNotFoundException} (exceção de domínio traduzida para 404 pela
 * camada de interfaces).
 */
@Service
public class GetProductUseCase {

    private final ProductRepository productRepository;

    public GetProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(ProductId id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
