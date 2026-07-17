package com.havingfunwithjava.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Teste de slice vertical do api-gateway.
 *
 * Estratégia: sobe o contexto completo do gateway (@SpringBootTest, RANDOM_PORT
 * — herdados de {@link AbstractGatewayIntegrationTest}, que também sobe o Redis
 * via Testcontainers para o filter RequestRateLimiter) e aponta
 * CATALOG_SERVICE_URL para um {@link WireMockServer} local que responde
 * a {@code GET /health} com um corpo fixo. Assim exercitamos o roteamento real
 * do Spring Cloud Gateway (predicates + StripPrefix) sem depender do
 * catalog-service de verdade — rápido e determinístico.
 *
 * O que verificamos:
 *  - {@code GET /catalog/health} (via gateway) retorna 200 e o corpo do upstream.
 *  - O filtro StripPrefix=1 removeu o prefixo /catalog, pois o WireMock só
 *    registra a rota em /health (sem o prefixo).
 */
class ApiGatewayRoutingTest extends AbstractGatewayIntegrationTest {

    /** Stub do catalog-service em porta aleatória. */
    private static final WireMockServer CATALOG_STUB =
            new WireMockServer(options().dynamicPort());

    @DynamicPropertySource
    static void catalogUrl(DynamicPropertyRegistry registry) {
        CATALOG_STUB.start();
        registry.add("CATALOG_SERVICE_URL", () -> "http://localhost:" + CATALOG_STUB.port());
    }

    @LocalServerPort
    private int gatewayPort; // mantido para diagnose; não usado nas asserções (WebTestClient já aponta para a porta correta)

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUpStub() {
        // O upstream só registra /health (sem /catalog) — prova que o StripPrefix funcionou.
        CATALOG_STUB.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"UP\",\"service\":\"catalog-service\",\"at\":\"2026-07-16T00:00:00Z\"}")));
    }

    @AfterEach
    void stopStub() {
        if (CATALOG_STUB.isRunning()) {
            CATALOG_STUB.stop();
        }
    }

    @Test
    void catalogHealthRoute_proxiesToCatalogServiceHealthEndpoint() {
        webTestClient.get().uri("/catalog/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("catalog-service");

        // Confirma que o gateway bateu no upstream correto (com prefixo removido).
        CATALOG_STUB.verify(getRequestedFor(urlEqualTo("/health")));
    }

    @Test
    void unknownPath_returns404() {
        webTestClient.get().uri("/not-a-route")
                .exchange()
                .expectStatus().isNotFound();
    }
}
