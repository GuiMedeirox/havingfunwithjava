package com.havingfunwithjava.catalog.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havingfunwithjava.catalog.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) dos filtros e paginação de produtos.
 *
 * Cria produtos em duas categorias distintas e valida:
 * - paginação (page/size) com metadados (totalItems, totalPages)
 * - filtro por categoria retorna apenas os daquela categoria
 * - ausência de parâmetros retorna lista simples (compatibilidade)
 */
class ProductFilterTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private String createProduct(String name, UUID categoryId) throws Exception {
        String payload = """
                {
                  "name": "%s",
                  "amount": "10.00",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(name, categoryId);
        MvcResult result = mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asText();
    }

    @Test
    void shouldReturnPaginatedResponseWithMetadata() throws Exception {
        // Usa categoria única para isolar de produtos criados por outros testes
        // (o container Postgres é compartilhado entre todas as classes de teste).
        UUID categoryId = UUID.randomUUID();
        createProduct("Prod A", categoryId);
        createProduct("Prod B", categoryId);

        mockMvc.perform(get("/products")
                        .param("category", categoryId.toString())
                        .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void shouldFilterByCategory() throws Exception {
        UUID catA = UUID.randomUUID();
        UUID catB = UUID.randomUUID();
        createProduct("Item Cat A", catA);
        createProduct("Item Cat B", catB);
        createProduct("Another Cat A", catA);

        mockMvc.perform(get("/products")
                        .param("category", catA.toString())
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldReturnSimpleListWhenNoParams() throws Exception {
        UUID categoryId = UUID.randomUUID();
        createProduct("No Params Item", categoryId);

        // Sem parâmetros → array direto (não paginado)
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'No Params Item')]").exists());
    }
}
