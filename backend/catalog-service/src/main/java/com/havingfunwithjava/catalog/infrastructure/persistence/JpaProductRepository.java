package com.havingfunwithjava.catalog.infrastructure.persistence;

import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Page;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import com.havingfunwithjava.catalog.domain.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    private final EntityManager entityManager;

    public JpaProductRepository(ProductJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
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
    public Page<Product> findActive(CategoryId categoryId, String searchTerm, int page, int size) {
        UUID categoryIdValue = categoryId == null ? null : categoryId.value();
        String term = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, categoryIdValue, term);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("name")));

        List<ProductEntity> entities = entityManager.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        long total = countByCriteria(categoryIdValue, term);
        List<Product> items = entities.stream().map(ProductMapper::toDomain).toList();
        return new Page<>(items, total, page, size);
    }

    @Override
    public long countActive(CategoryId categoryId, String searchTerm) {
        UUID categoryIdValue = categoryId == null ? null : categoryId.value();
        String term = (searchTerm == null || searchTerm.isBlank()) ? null : searchTerm.trim();
        return countByCriteria(categoryIdValue, term);
    }

    /**
     * Contagem interna (recebe UUID já extraído do CategoryId).
     */
    private long countByCriteria(UUID categoryId, String searchTerm) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(cb.count(root));
        cq.where(buildPredicates(cb, root, categoryId, searchTerm).toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * Constrói os predicados comuns (active=true, filtro de categoria opcional,
     * filtro de nome opcional case-insensitive). Usado tanto por find quanto count.
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<ProductEntity> root,
                                            UUID categoryId, String searchTerm) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("categoryId"), categoryId));
        }
        if (searchTerm != null) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + searchTerm.toLowerCase() + "%"));
        }
        return predicates;
    }
}
