package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical (Seam 1) do CRUD de produtos.
 *
 * Sobe o contexto Spring completo e usa Testcontainers com Postgres REAL —
 * nada de H2 ou mock de banco. Exercita o caminho completo: HTTP → controller
 * → use case → repositório → JPA → Postgres → Flyway migration.
 *
 * Valida comportamento externo: criar produto (201), listar (200), e rejeitar
 * payloads inválidos (400).
 *
 * Herda de {@link IntegrationTestBase} para reaproveitar a configuração comum
 * (Spring + Testcontainers + profile test).
 */
class ProductControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateProductAndReturn201() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String payload = """
                {
                  "name": "Notebook Gamer",
                  "description": "16GB RAM, RTX 4060",
                  "amount": "4500.00",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(categoryId);

        mockMvc.perform(post("/products").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Notebook Gamer"))
                .andExpect(jsonPath("$.amount").value("4500.00"))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldListCreatedProducts() throws Exception {
        // Cria um produto primeiro
        UUID categoryId = UUID.randomUUID();
        String payload = """
                {
                  "name": "Mouse Sem Fio",
                  "description": "",
                  "amount": "89.90",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(categoryId);
        mockMvc.perform(post("/products").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        // Lista — deve conter ao menos o produto criado
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Mouse Sem Fio')]").exists());
    }

    @Test
    void shouldRejectInvalidPayloadWith400() throws Exception {
        // nome vazio + preço ausente
        String payload = """
                {
                  "name": "",
                  "amount": "0",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/products").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNegativePriceWith400() throws Exception {
        // preço negativo passa pela validação de DTO (DecimalMin) → 400
        String payload = """
                {
                  "name": "Inválido",
                  "amount": "-5.00",
                  "currency": "BRL",
                  "categoryId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/products").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
    }
}
