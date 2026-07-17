package com.havingfunwithjava.catalog.infrastructure.persistence;

import com.havingfunwithjava.catalog.domain.Category;
import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador: implementa a porta de domínio {@link CategoryRepository} usando
 * Spring Data JPA ({@link CategoryJpaRepository}) e o {@link CategoryMapper}.
 */
@Repository
public class JpaCategoryRepository implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public JpaCategoryRepository(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity saved = jpaRepository.save(CategoryMapper.toEntity(category));
        return CategoryMapper.toDomain(saved);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream()
                .map(CategoryMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.value()).map(CategoryMapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug, CategoryId excludeId) {
        if (excludeId == null) {
            return jpaRepository.existsBySlug(slug);
        }
        return jpaRepository.existsBySlugAndIdNot(slug, excludeId.value());
    }
}
