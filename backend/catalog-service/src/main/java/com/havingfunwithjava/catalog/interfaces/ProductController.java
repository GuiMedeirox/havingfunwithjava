package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.application.CreateProductCommand;
import com.havingfunwithjava.catalog.application.CreateProductUseCase;
import com.havingfunwithjava.catalog.application.GetProductUseCase;
import com.havingfunwithjava.catalog.application.ListProductsUseCase;
import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Page;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para produtos.
 *
 * Vive na camada de interfaces. Recebe DTOs, valida, converte para comandos de
 * domínio, chama os casos de uso, e traduz o resultado de volta para DTOs.
 * Sem regra de negócio aqui — só orquestração de borda.
 *
 * - POST   /products                   → cria produto (201)
 * - GET    /products                   → lista produtos ativos (200)
 * - GET    /products?category=&page=&size= → lista paginada/filtrada (200)
 * - GET    /products/{id}              → obtém um produto por id (200 ou 404)
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    /** Tamanho de página default quando o cliente não informa. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CreateProductUseCase createProduct;
    private final ListProductsUseCase listProducts;
    private final GetProductUseCase getProduct;

    public ProductController(CreateProductUseCase createProduct,
                             ListProductsUseCase listProducts,
                             GetProductUseCase getProduct) {
        this.createProduct = createProduct;
        this.listProducts = listProducts;
        this.getProduct = getProduct;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.description(),
                request.amount(),
                request.currency(),
                new CategoryId(request.categoryId())
        );
        Product created = createProduct.execute(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id().value())
                .toUri();

        return ResponseEntity.created(location).body(ProductResponse.from(created));
    }

    /**
     * Lista produtos. Se algum parâmetro de paginação/filtro for informado,
     * retorna a resposta paginada {@link PagedProductsResponse}; caso contrário,
     * retorna a lista simples (compatibilidade com o comportamento anterior).
     */
    @GetMapping
    public Object list(
            @RequestParam(value = "category", required = false) UUID categoryId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        boolean wantsPagination = page != null || size != null || categoryId != null;
        if (!wantsPagination) {
            return listProducts.execute().stream()
                    .map(ProductResponse::from)
                    .toList();
        }

        int resolvedPage = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null || size <= 0 ? DEFAULT_PAGE_SIZE : size;
        CategoryId filter = categoryId == null ? null : new CategoryId(categoryId);

        Page<Product> result = listProducts.execute(filter, resolvedPage, resolvedSize);
        return PagedProductsResponse.from(result);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        Product product = getProduct.execute(new ProductId(id));
        return ProductResponse.from(product);
    }
}
