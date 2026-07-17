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
 * Teste de slice vertical (Seam 1) da busca por nome de produtos.
 *
 * Cria produtos com nomes distintos e valida:
 * - busca por termo retorna apenas os cujo nome contém o termo (case-insensitive)
 * - busca por termo combina com filtro de categoria e paginação
 * - termo sem correspondência retorna totalItems=0
 */
class ProductSearchTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private void createProduct(String name, UUID categoryId) throws Exception {
        String payload = """
                {
                  "name": "%s",
                  "amount": "10.00",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(name, categoryId);
        mockMvc.perform(post("/products").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFindProductsByNameCaseInsensitive() throws Exception {
        // Usa termo único ("ZaphodBeeblebrox") p/ isolar de produtos de outros testes
        // que compartilham o mesmo banco (container singleton).
        UUID categoryId = UUID.randomUUID();
        createProduct("ZaphodBeeblebrox Gold", categoryId);
        createProduct("ZaphodBeeblebrox Silver", categoryId);
        createProduct("Mouse Logitech", categoryId);

        mockMvc.perform(get("/products")
                        .param("q", "zaphodbeeblebrox")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() throws Exception {
        UUID categoryId = UUID.randomUUID();
        createProduct("Teclado Mecânico", categoryId);

        mockMvc.perform(get("/products")
                        .param("q", "zzznaoexiste")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void shouldCombineSearchWithCategoryFilter() throws Exception {
        UUID catA = UUID.randomUUID();
        UUID catB = UUID.randomUUID();
        createProduct("Notebook Cat A", catA);
        createProduct("Notebook Cat B", catB);

        // Busca "notebook" filtrando por catA → só o de catA
        mockMvc.perform(get("/products")
                        .param("q", "notebook")
                        .param("category", catA.toString())
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Notebook Cat A"));
    }
}
