package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import com.havingfunwithjava.catalog.domain.ProductNotFoundException;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: inativar um produto (soft delete).
 *
 * Marca o produto como active=false sem removê-lo do banco, preservando o
 * histórico. Produtos inativos não aparecem no GET /products público (o
 * repositório filtra por active=true), mas permanecem consultáveis por id
 * e por rotas administrativas futuras.
 */
@Service
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;

    public DeactivateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(ProductId id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        Product deactivated = new Product(
                existing.id(),
                existing.name(),
                existing.description(),
                existing.price(),
                existing.categoryId(),
                false  // inativado
        );
        productRepository.save(deactivated);
    }
}
