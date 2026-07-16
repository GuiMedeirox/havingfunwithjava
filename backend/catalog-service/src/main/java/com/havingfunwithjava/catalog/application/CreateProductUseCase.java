package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Money;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar um produto.
 *
 * Orquestra o domínio: converte o comando em entidade de domínio {@link Product}
 * (que valida invariantes no construtor) e pede ao repositório que o persista.
 * A transação é demarcada aqui (@Transactional) porque é o ponto onde o caso de
 * uso coordena escritas — regra comum em application services.
 */
@Service
public class CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(CreateProductCommand command) {
        Money price = Money.of(command.amount(), command.currency());
        Product product = Product.createNew(
                command.name(),
                command.description(),
                price,
                command.categoryId()
        );
        return productRepository.save(product);
    }
}
