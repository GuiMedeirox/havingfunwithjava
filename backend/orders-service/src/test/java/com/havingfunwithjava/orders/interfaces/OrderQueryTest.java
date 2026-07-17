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
 * Teste de slice vertical (Seam 1) de consulta de pedidos (issue #16).
 *
 * <p>Cria pedidos via POST (com CatalogClient mockado) e valida:
 * - GET /orders?customerId= lista apenas pedidos daquele cliente
 * - GET /orders/{id} retorna detalhes
 * - GET /orders/{id} inexistente → 404
 * - isolamento: cliente A não vê pedidos do cliente B
 */
class OrderQueryTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogClient catalogClient;

    private final ObjectMapper mapper = new ObjectMapper();

    private UUID createOrder(UUID customerId, UUID productId) throws Exception {
        when(catalogClient.findProductsByIds(anyList()))
                .thenReturn(List.of(new CatalogItem(productId, "Produto", new Money(new BigDecimal("10.00"), "BRL"), true)));
        String payload = """
                {
                  "customerId": "%s",
                  "items": [
                    { "productId": "%s", "quantity": 1, "expectedUnitPrice": "10.00", "currency": "BRL" }
                  ]
                }
                """.formatted(customerId, productId);
        MvcResult result = mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(body.get("id").asText());
    }

    @Test
    void shouldListOrdersByCustomer() throws Exception {
        UUID customerA = UUID.randomUUID();
        UUID customerB = UUID.randomUUID();
        createOrder(customerA, UUID.randomUUID());
        createOrder(customerA, UUID.randomUUID());
        createOrder(customerB, UUID.randomUUID());

        // Cliente A deve ver apenas seus 2 pedidos
        mockMvc.perform(get("/orders").param("customerId", customerA.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Cliente B vê apenas 1
        mockMvc.perform(get("/orders").param("customerId", customerB.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID orderId = createOrder(customerId, UUID.randomUUID());

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.items[0].productName").value("Produto"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }
}
