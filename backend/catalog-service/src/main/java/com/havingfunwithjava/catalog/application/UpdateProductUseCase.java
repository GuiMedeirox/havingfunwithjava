package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Money;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import com.havingfunwithjava.catalog.domain.ProductNotFoundException;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: atualizar um produto (edição completa ou parcial).
 *
 * Localiza o produto existente e o recria com os novos valores. Como Product é
 * um record imutável, "atualizar" = construir nova instância preservando o id
 * e aplicando os campos informados. As invariantes (preço > 0, nome não-vazio)
 * são validadas no construtor de Product.
 *
 * Para PATCH (parcial): campos null no comando mantêm o valor existente.
 * Para PUT (completo): todos os campos vêm preenchidos.
 */
@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(ProductId id, UpdateProductCommand command) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String name = command.name() != null ? command.name() : existing.name();
        String description = command.description() != null ? command.description() : existing.description();
        Money price = command.amount() != null && command.currency() != null
                ? Money.of(command.amount(), command.currency())
                : existing.price();
        CategoryId categoryId = command.categoryId() != null ? command.categoryId() : existing.categoryId();

        Product updated = new Product(
                existing.id(),
                name,
                description,
                price,
                categoryId,
                existing.active()
        );
        return productRepository.save(updated);
    }
}
