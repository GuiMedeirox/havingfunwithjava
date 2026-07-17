package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Category;
import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.CategoryNotFoundException;
import com.havingfunwithjava.catalog.domain.CategoryRepository;
import com.havingfunwithjava.catalog.domain.DuplicateSlugException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: atualizar categoria (nome e slug).
 *
 * Verifica unicidade do slug (excluindo o próprio id da checagem), localiza a
 * categoria existente e recria a entidade com os novos valores. Como Category é
 * um record imutável, atualizar = substituir.
 */
@Service
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category execute(CategoryId id, String name, String slug) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Se o slug mudou, verifica unicidade excluindo o próprio id
        if (!existing.slug().equals(slug)
                && categoryRepository.existsBySlug(slug, id)) {
            throw new DuplicateSlugException(slug);
        }

        Category updated = new Category(existing.id(), name, slug, existing.parentId());
        return categoryRepository.save(updated);
    }
}
