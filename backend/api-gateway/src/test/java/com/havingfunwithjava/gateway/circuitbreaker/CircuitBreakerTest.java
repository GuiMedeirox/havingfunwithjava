package com.havingfunwithjava.gateway.circuitbreaker;

import com.havingfunwithjava.gateway.AbstractGatewayIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Teste de slice vertical do circuit breaker (issue #12).
 *
 * <p>Cenário: o catalog-service está indisponível (CATALOG_SERVICE_URL aponta
 * para uma porta onde nada escuta → connection refused). Disparamos chamadas
 * repetidas contra {@code GET /catalog/health}; após o threshold de falha
 * (50% das últimas 5, janela de 10), o circuito ABRE e o gateway passa a
 * retornar o fallback 503 em vez de propagar o erro de conexão.
 *
 * <p>Por que connection refused e não 500? O circuit breaker do Spring Cloud
 * Gateway (Resilience4j) por padrão conta como falha apenas EXCEÇÕES (timeout,
 * connection refused), não respostas 5xx. Simular backend down (porta fechada)
 * dispara o circuito de forma determinística.
 *
 * <p>Cobre os critérios de aceite da issue #12:
 * <ul>
 *   <li>Resilience4j configurado por rota.</li>
 *   <li>Falhas repetidas (backend down) abrem o circuito.</li>
 *   <li>Circuito aberto retorna fallback 503 com corpo amigável.</li>
 * </ul>
 */
class CircuitBreakerTest extends AbstractGatewayIntegrationTest {

    /**
     * Aponta o catalog-service para uma porta onde nada escuta (connection refused).
     * A porta 1 é virtualmente sempre livrete — nada escuta ali em ambientes de teste.
     */
    @DynamicPropertySource
    static void unreachableCatalog(DynamicPropertyRegistry registry) {
        registry.add("CATALOG_SERVICE_URL", () -> "http://localhost:1");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void repeatedFailures_openCircuitAndReturnFallback() {
        // minimum-number-of-calls=5, failure-rate-threshold=50%, sliding-window=10.
        // Disparamos 10 requests: as primeiras falham com erro de conexão; após o
        // threshold, o circuito abre e o gateway passa a retornar 503 de fallback.
        boolean circuitOpened = false;
        for (int i = 0; i < 12; i++) {
            int status = webTestClient.get().uri("/catalog/health").exchange()
                    .returnResult(Object.class)
                    .getStatus().value();
            if (status == 503) {
                circuitOpened = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(circuitOpened,
                "Esperava que o circuito abrisse e retornasse 503 após falhas repetidas");
    }

    @Test
    void fallbackBodyIsProblemDetail() {
        // Dispara requests até o circuito abrir e captura o corpo do fallback.
        for (int i = 0; i < 12; i++) {
            var result = webTestClient.get().uri("/catalog/health").exchange()
                    .returnResult(String.class);
            if (result.getStatus().value() == 503) {
                String body = result.getResponseBody().blockFirst();
                org.junit.jupiter.api.Assertions.assertNotNull(body);
                org.junit.jupiter.api.Assertions.assertTrue(body.contains("Service unavailable"),
                        "Corpo do fallback deve conter a mensagem amigável: " + body);
                return;
            }
        }
        org.junit.jupiter.api.Assertions.fail("Circuito não abriu após 12 requests");
    }
}
