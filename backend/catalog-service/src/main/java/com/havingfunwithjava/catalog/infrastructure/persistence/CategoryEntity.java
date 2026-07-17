package com.havingfunwithjava.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidade JPA (adaptador de persistência) de categoria.
 *
 * Separada da entidade de domínio {@link com.havingfunwithjava.catalog.domain.Category};
 * o {@link CategoryMapper} traduz entre as duas.
 */
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "parent_id")
    private UUID parentId;

    protected CategoryEntity() {
    }

    public CategoryEntity(UUID id, String name, String slug, UUID parentId) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.parentId = parentId;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public UUID getParentId() { return parentId; }
}
