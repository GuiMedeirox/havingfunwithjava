package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.Category;
import com.havingfunwithjava.catalog.domain.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: listar categorias.
 */
@Service
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public ListCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> execute() {
        return categoryRepository.findAll();
    }
}
