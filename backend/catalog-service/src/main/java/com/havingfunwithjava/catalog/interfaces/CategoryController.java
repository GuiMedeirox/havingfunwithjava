package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.application.CreateCategoryUseCase;
import com.havingfunwithjava.catalog.application.ListCategoriesUseCase;
import com.havingfunwithjava.catalog.application.UpdateCategoryUseCase;
import com.havingfunwithjava.catalog.domain.Category;
import com.havingfunwithjava.catalog.domain.CategoryId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para categorias.
 *
 * - POST   /categories       → cria categoria (201)
 * - GET    /categories       → lista categorias (200)
 * - PUT    /categories/{id}  → atualiza nome/slug (200)
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategory;
    private final ListCategoriesUseCase listCategories;
    private final UpdateCategoryUseCase updateCategory;

    public CategoryController(CreateCategoryUseCase createCategory,
                              ListCategoriesUseCase listCategories,
                              UpdateCategoryUseCase updateCategory) {
        this.createCategory = createCategory;
        this.listCategories = listCategories;
        this.updateCategory = updateCategory;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryId parentId = request.parentId() == null ? null : new CategoryId(request.parentId());
        Category created = createCategory.execute(request.name(), request.slug(), parentId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id().value())
                .toUri();

        return ResponseEntity.created(location).body(CategoryResponse.from(created));
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return listCategories.execute().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        Category updated = updateCategory.execute(new CategoryId(id), request.name(), request.slug());
        return CategoryResponse.from(updated);
    }
}
