package com.havingfunwithjava.catalog.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Este teste prova que o molde Clean Architecture está "vivo": as 4 camadas
 * se conectam via injeção de dependência e o endpoint responde corretamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

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
