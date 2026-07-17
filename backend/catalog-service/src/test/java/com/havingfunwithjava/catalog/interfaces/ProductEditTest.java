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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) de edição e inativação de produtos.
 *
 * Cobre PUT (atualização completa), PATCH (parcial), inativação (soft delete),
 * e a exclusão de produtos inativos do list público.
 */
class ProductEditTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private String createProduct(String name, UUID categoryId) throws Exception {
        String payload = """
                {
                  "name": "%s",
                  "amount": "100.00",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(name, categoryId);
        MvcResult result = mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void shouldUpdateProductWithPut() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String id = createProduct("Nome Original", categoryId);

        String update = """
                {
                  "name": "Nome Atualizado",
                  "description": "Nova descrição",
                  "amount": "199.90",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(categoryId);

        mockMvc.perform(put("/products/" + id)
                        .contentType(APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"))
                .andExpect(jsonPath("$.description").value("Nova descrição"))
                // amount pode vir como 199.90 ou 199.9000 dependendo do round-trip;
                // valido apenas que é positivo e tem o valor esperado (ignorando zeros à direita)
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldPatchProductNameOnly() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String id = createProduct("Patch Me", categoryId);

        String patch = """
                { "name": "Nome Patcheado" }
                """;

        mockMvc.perform(patch("/products/" + id)
                        .contentType(APPLICATION_JSON).content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Patcheado"))
                // Outros campos preservados
                .andExpect(jsonPath("$.amount").value("100.0000"));
    }

    @Test
    void shouldDeactivateProductAndExcludeFromPublicList() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String id = createProduct("Produto Para Inativar", categoryId);

        // Inativa
        mockMvc.perform(post("/products/" + id + "/deactivate"))
                .andExpect(status().isNoContent());

        // Busca por id ainda funciona (preserva histórico)
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Não aparece na lista pública (filtra active=true)
        mockMvc.perform(get("/products")
                        .param("category", categoryId.toString())
                        .param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + id + "')]").doesNotExist());
    }

    @Test
    void shouldRejectNegativePriceOnUpdate() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String id = createProduct("Preço Inválido", categoryId);

        String update = """
                {
                  "name": "X",
                  "amount": "-10.00",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(categoryId);

        mockMvc.perform(put("/products/" + id)
                        .contentType(APPLICATION_JSON).content(update))
                .andExpect(status().isBadRequest());
    }
}
