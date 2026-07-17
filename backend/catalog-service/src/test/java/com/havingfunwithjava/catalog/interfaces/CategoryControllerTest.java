package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) do CRUD de categorias.
 *
 * Cobre criar (201), listar (200), atualizar (200) e o conflito de slug (409).
 */
class CategoryControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private String categoryPayload(String name, String slug) {
        return """
                {
                  "name": "%s",
                  "slug": "%s"
                }
                """.formatted(name, slug);
    }

    @Test
    void shouldCreateCategoryAndReturn201() throws Exception {
        mockMvc.perform(post("/categories")
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Eletrônicos", "eletronicos")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Eletrônicos"))
                .andExpect(jsonPath("$.slug").value("eletronicos"))
                .andExpect(jsonPath("$.parentId").doesNotExist());
    }

    @Test
    void shouldListCreatedCategories() throws Exception {
        mockMvc.perform(post("/categories")
                .contentType(APPLICATION_JSON)
                .content(categoryPayload("Informática", "informatica-list")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'informatica-list')]").exists());
    }

    @Test
    void shouldUpdateCategoryNameAndSlug() throws Exception {
        // Cria
        String createResponse = mockMvc.perform(post("/categories")
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Nome Antigo", "slug-antigo-update")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(createResponse).get("id").asText();

        // Atualiza
        mockMvc.perform(put("/categories/" + id)
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Nome Novo", "slug-novo-update")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Novo"))
                .andExpect(jsonPath("$.slug").value("slug-novo-update"));
    }

    @Test
    void shouldReturn409WhenSlugDuplicated() throws Exception {
        // Cria uma categoria com o slug
        mockMvc.perform(post("/categories")
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Primeira", "slug-duplicado-409")))
                .andExpect(status().isCreated());

        // Tenta criar outra com o mesmo slug → 409
        mockMvc.perform(post("/categories")
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Segunda", "slug-duplicado-409")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void shouldAllowSameSlugOnUpdateOfSameCategory() throws Exception {
        // Cria
        String createResponse = mockMvc.perform(post("/categories")
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Cat", "slug-mesmo-update")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(createResponse).get("id").asText();

        // Atualiza mantendo o mesmo slug → deve permitir (não é duplicado consigo mesmo)
        mockMvc.perform(put("/categories/" + id)
                        .contentType(APPLICATION_JSON)
                        .content(categoryPayload("Cat Atualizada", "slug-mesmo-update")))
                .andExpect(status().isOk());
    }
}
