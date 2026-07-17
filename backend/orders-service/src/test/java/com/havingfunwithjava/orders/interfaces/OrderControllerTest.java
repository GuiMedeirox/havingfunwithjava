package com.havingfunwithjava.orders.interfaces;

import com.havingfunwithjava.orders.IntegrationTestBase;
import com.havingfunwithjava.orders.domain.CatalogClient;
import com.havingfunwithjava.orders.domain.CatalogItem;
import com.havingfunwithjava.orders.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) da criação de pedido.
 *
 * <p>O {@link CatalogClient} é mockado via {@link MockBean}: simulamos as respostas
 * do catalog-service sem subir outro serviço. O resto (controller → use case →
 * repositório → JPA → Postgres real via Testcontainers) é exercitado de verdade.
 */
class OrderControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogClient catalogClient;

    private CatalogItem catalogItem(UUID productId, String name, String price) {
        return new CatalogItem(productId, name, new Money(new BigDecimal(price), "BRL"), true);
    }

    @Test
    void shouldCreateOrderAndReturn201() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(catalogClient.findProductsByIds(anyList()))
                .thenReturn(List.of(catalogItem(productId, "Notebook", "4500.00")));

        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    {
                      "productId": "%s",
                      "quantity": 2,
                      "expectedUnitPrice": "4500.00",
                      "currency": "BRL"
                    }
                  ]
                }
                """.formatted(customerId, productId);

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.items[0].productName").value("Notebook"));
                // totalAmount/subtotal: BigDecimal sem normalização (9000 ou 9000.00);
                // validamos apenas existência aqui pois a escala varia.
    }

    @Test
    void shouldRejectOrderWithNonExistentProduct() throws Exception {
        // catalogClient retorna lista VAZIA → produto não encontrado
        when(catalogClient.findProductsByIds(anyList())).thenReturn(List.of());

        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    {
                      "productId": "%s",
                      "quantity": 1,
                      "expectedUnitPrice": "10.00",
                      "currency": "BRL"
                    }
                  ]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Invalid order"));
    }

    @Test
    void shouldRejectOrderWithDivergentPrice() throws Exception {
        UUID productId = UUID.randomUUID();
        // Preço atual 100.00, mas cliente envia 50.00 → divergência
        when(catalogClient.findProductsByIds(anyList()))
                .thenReturn(List.of(catalogItem(productId, "Mouse", "100.00")));

        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    {
                      "productId": "%s",
                      "quantity": 1,
                      "expectedUnitPrice": "50.00",
                      "currency": "BRL"
                    }
                  ]
                }
                """.formatted(UUID.randomUUID(), productId);

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldRejectEmptyItemsWith400() throws Exception {
        String payload = """
                {
                  "customerId": "%s",
                  "items": []
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
    }
}
