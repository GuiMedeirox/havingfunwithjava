package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de slice vertical do endpoint GET /health.
 *
 * Seam 1 (slice de API REST): sobe o contexto Spring completo e exercita o
 * caminho HTTP → controller → use case → adaptador → Actuator. Valida o
 * comportamento externo (status 200 + campos esperados), não a implementação.
 *
 * Herda de {@link IntegrationTestBase} para reaproveitar a configuração de
 * Spring + Testcontainers (Postgres real), já que o contexto agora exige datasource.
 */
class HealthControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOkWithServiceInfo() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("catalog-service"))
                .andExpect(jsonPath("$.at").exists());
    }

    @Test
    void actuatorHealthEndpointShouldBeUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
