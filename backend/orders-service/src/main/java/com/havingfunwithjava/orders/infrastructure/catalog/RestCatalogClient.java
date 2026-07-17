package com.havingfunwithjava.orders.infrastructure.catalog;

import com.havingfunwithjava.orders.domain.CatalogClient;
import com.havingfunwithjava.orders.domain.CatalogItem;
import com.havingfunwithjava.orders.domain.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador: implementa {@link CatalogClient} chamando o catalog-service via REST.
 *
 * <p>Para cada productId, faz GET /products/{id} no catalog-service e mapeia o
 * JSON de resposta para {@link CatalogItem}. Produtos inexistentes (404) ou
 * inativos (active=false) são OMITIDOS do retorno — o caso de uso detecta a
 * divergência comparando com os ids solicitados.
 *
 * <p>Usa o blocking {@link RestClient} (Spring 6.1+) por simplicidade — a chamada
 * é síncrona no caso de uso, que roda numa thread de servlet. Para alta carga,
 * poderia usar WebClient reativo; fora do escopo deste slice.
 */
@Component
public class RestCatalogClient implements CatalogClient {

    private final RestClient restClient;

    public RestCatalogClient(@Value("${app.catalog.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public List<CatalogItem> findProductsByIds(List<UUID> productIds) {
        return productIds.stream()
                .map(this::fetchProduct)
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Busca um produto; retorna lista de 0 (não encontrado/inativo) ou 1 elemento.
     */
    @SuppressWarnings("unchecked")
    private List<CatalogItem> fetchProduct(UUID productId) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(Map.class);
            if (body == null) {
                return List.of();
            }
            boolean active = Boolean.TRUE.equals(body.get("active"));
            if (!active) {
                return List.of();
            }
            String name = String.valueOf(body.get("name"));
            String amount = String.valueOf(body.get("amount"));
            String currency = String.valueOf(body.get("currency"));
            Money price = new Money(new BigDecimal(amount), currency);
            return List.of(new CatalogItem(productId, name, price, true));
        } catch (Exception ex) {
            // 404 ou erro de conexão → produto não disponível para validação
            return List.of();
        }
    }
}
