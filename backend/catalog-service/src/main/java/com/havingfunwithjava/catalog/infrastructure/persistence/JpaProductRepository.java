package com.havingfunwithjava.catalog.infrastructure.persistence;

import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Page;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Page<Product> findActive(CategoryId categoryId, int page, int size) {
        UUID categoryIdValue = categoryId == null ? null : categoryId.value();
        org.springframework.data.domain.Page<ProductEntity> result =
                jpaRepository.findActiveByCategory(categoryIdValue, PageRequest.of(page, size));

        List<Product> items = result.stream().map(ProductMapper::toDomain).toList();
        return new Page<>(items, result.getTotalElements(), page, size);
    }

    @Override
    public long countActive(CategoryId categoryId) {
        UUID categoryIdValue = categoryId == null ? null : categoryId.value();
        return jpaRepository.countActiveByCategory(categoryIdValue);
    }
}
