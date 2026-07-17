package com.havingfunwithjava.orders.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havingfunwithjava.orders.IntegrationTestBase;
import com.havingfunwithjava.orders.domain.CatalogClient;
import com.havingfunwithjava.orders.domain.CatalogItem;
import com.havingfunwithjava.orders.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) do endpoint GET /orders/{id}/status (issue #17).
 */
class OrderStatusEndpointTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogClient catalogClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldReturnCurrentStatus() throws Exception {
        UUID productId = UUID.randomUUID();
        when(catalogClient.findProductsByIds(anyList()))
                .thenReturn(List.of(new CatalogItem(productId, "Item", new Money(new BigDecimal("10.00"), "BRL"), true)));

        // Cria pedido
        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    { "productId": "%s", "quantity": 1, "expectedUnitPrice": "10.00", "currency": "BRL" }
                  ]
                }
                """.formatted(UUID.randomUUID(), productId);
        MvcResult createResult = mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        String orderId = mapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Consulta o status
        mockMvc.perform(get("/orders/" + orderId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }
}
