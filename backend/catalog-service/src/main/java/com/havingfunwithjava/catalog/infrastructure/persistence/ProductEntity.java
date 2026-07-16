package com.havingfunwithjava.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade JPA (adaptador de persistência) de produto.
 *
 * Vive em infrastructure, separada da entidade de domínio {@link com.havingfunwithjava.catalog.domain.Product}.
 * O {@link ProductMapper} traduz entre as duas. Mantê-las separadas preserva o
 * domínio puro: a anotação JPA não vaza para a camada de domínio.
 *
 * O id é UUID (gerado pelo domínio e repassado — não delegamos ao banco).
 */
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "active", nullable = false)
    private boolean active;

    // Construtores para JPA (Hibernate exige o default)
    protected ProductEntity() {
    }

    public ProductEntity(UUID id, String name, String description,
                         BigDecimal priceAmount, String priceCurrency,
                         UUID categoryId, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
        this.categoryId = categoryId;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getPriceCurrency() { return priceCurrency; }
    public UUID getCategoryId() { return categoryId; }
    public boolean isActive() { return active; }
}
