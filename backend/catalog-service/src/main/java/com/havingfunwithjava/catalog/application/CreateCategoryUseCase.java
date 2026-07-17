package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Category;
import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.CategoryRepository;
import com.havingfunwithjava.catalog.domain.DuplicateSlugException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: criar categoria.
 *
 * Valida que o slug é único (via {@link CategoryRepository#existsBySlug}) antes
 * de criar a entidade de domínio {@link Category} (que valida invariantes no
 * construtor) e persistir.
 */
@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category execute(String name, String slug, CategoryId parentId) {
        if (categoryRepository.existsBySlug(slug, null)) {
            throw new DuplicateSlugException(slug);
        }
        Category category = Category.createNew(name, slug, parentId);
        return categoryRepository.save(category);
    }
}
