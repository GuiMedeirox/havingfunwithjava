package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.application.CreateProductCommand;
import com.havingfunwithjava.catalog.application.CreateProductUseCase;
import com.havingfunwithjava.catalog.application.GetProductUseCase;
import com.havingfunwithjava.catalog.application.ListProductsUseCase;
import com.havingfunwithjava.catalog.domain.CategoryId;
import com.havingfunwithjava.catalog.domain.Product;
import com.havingfunwithjava.catalog.domain.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
 * - POST   /products       → cria produto (201)
 * - GET    /products       → lista produtos ativos (200)
 * - GET    /products/{id}  → obtém um produto por id (200 ou 404)
 */
@RestController
@RequestMapping("/products")
public class ProductController {

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

    @GetMapping
    public List<ProductResponse> list() {
        return listProducts.execute().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        Product product = getProduct.execute(new ProductId(id));
        return ProductResponse.from(product);
    }
}
