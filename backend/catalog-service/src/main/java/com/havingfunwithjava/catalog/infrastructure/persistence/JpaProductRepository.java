package com.havingfunwithjava.catalog.infrastructure.persistence;

import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador: implementa a porta de domínio {@link ProductRepository} usando
 * Spring Data JPA ({@link ProductJpaRepository}) e o {@link ProductMapper}.
 *
 * Esta classe é o "adapter" da porta definida em domain. O @Repository faz o
 * Spring detectá-la e injetá-la nos casos de uso — mas o domínio não sabe disso,
 * ele só conhece a interface {@link ProductRepository}.
 */
@Repository
public class JpaProductRepository implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public JpaProductRepository(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity saved = jpaRepository.save(ProductMapper.toEntity(product));
        return ProductMapper.toDomain(saved);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findByActiveTrue().stream()
                .map(ProductMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value()).map(ProductMapper::toDomain);
    }
}
