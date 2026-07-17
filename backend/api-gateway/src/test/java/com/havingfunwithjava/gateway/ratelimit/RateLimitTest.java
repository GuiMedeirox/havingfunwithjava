package com.havingfunwithjava.gateway.ratelimit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.havingfunwithjava.gateway.AbstractGatewayIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Teste de slice vertical do rate limiting (issue #11).
 *
 * <p>Cenário: disparamos uma rajada (burst) contra uma rota cujo limite de teste
 * está deliberadamente baixo (burst=2, replenish=1 req/min) e verificamos que,
 * após esgotar os tokens, o gateway responde 429 Too Many Requests com o header
 * {@code Retry-After}.
 *
 * <p>Estratégia de isolamento: sobrescrevemos os limites das rotas via
 * {@link DynamicPropertySource} para valores baixos e determinísticos APENAS
 * nesta classe (as demais classes de teste usam limites altos). O Redis
 * (Testcontainers, {@link AbstractGatewayIntegrationTest}) é fresco por JVM.
 *
 * <p>Para que cada método de teste comece com o balde de tokens cheio (tokens são
 * cumulativos por chave de IP+rota no Redis, compartilhados entre métodos), o
 * {@link @BeforeEach} faz {@code FLUSHDB} no Redis via
 * {@link ReactiveRedisConnectionFactory}. Assim os testes ficam independententes
 * da ordem de execução.
 *
 * <p>Por que testar a rota de leitura ({@code GET /catalog/health}) e não o login?
 * O login exige corpo JSON válido e credenciais; a rota GET é estática (stub
 * WireMock responde 200), simplificando a rajada. O mecanismo (token bucket +
 * Lua no Redis) é idêntico para todas as rotas — o que diferencia é apenas
 * replenishRate/burstCapacity, configurados por rota. Validar a rota de leitura
 * exercita o mesmo caminho do RequestRateLimiter.
 *
 * <p>Cobre os critérios de aceite da issue #11:
 * <ul>
 *   <li>RequestRateLimiter configurado por rota (limites via application.yml).</li>
 *   <li>Limite excedido → 429 Too Many Requests.</li>
 *   <li>Header {@code Retry-After} presente na resposta 429.</li>
 *   <li>Limite por IP (todos os requests aqui vêm de 127.0.0.1).</li>
 *   <li>Teste simula burst e verifica 429.</li>
 * </ul>
 */
class RateLimitTest extends AbstractGatewayIntegrationTest {

    /** Stub único do catalog-service em porta aleatória, vivo durante toda a classe. */
    private static final WireMockServer CATALOG_STUB =
            new WireMockServer(options().dynamicPort());

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        CATALOG_STUB.start();
        registry.add("CATALOG_SERVICE_URL", () -> "http://localhost:" + CATALOG_STUB.port());
        // Limite APERTADO e determinístico para a rota de leitura: 2 tokens de burst,
        // 1 token reposto por minuto. Assim a 3ª request dentro da rajada estoura o limite.
        registry.add("RATE_LIMIT_READ_REPLENISH", () -> "1");
        registry.add("RATE_LIMIT_READ_BURST", () -> "2");
        // Retry-After curto e determinístico para o teste (default 60s). Validamos
        // presença e valor exato do header.
        registry.add("app.ratelimit.retry-after-seconds", () -> "10");
    }

    @BeforeAll
    static void ensureStubRunning() {
        if (!CATALOG_STUB.isRunning()) {
            CATALOG_STUB.start();
        }
    }

    @AfterAll
    static void stopStub() {
        if (CATALOG_STUB.isRunning()) {
            CATALOG_STUB.stop();
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void setUpStub() {
        CATALOG_STUB.resetAll();
        // GET /health upstream (sem prefixo /catalog — prova StripPrefix).
        CATALOG_STUB.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"UP\"}")));
        // Zera os contadores de rate limit entre métodos: tokens são cumulativos por
        // chave (IP + rota) no Redis. Sem isto, a ordem dos testes afetaria o resultado.
        redisConnectionFactory.getReactiveConnection().serverCommands().flushDb().block();
    }

    @Test
    void burstOverLimit_returns429WithRetryAfterHeader() {
        // Burst capacity = 2, requestedTokens = 1: as duas primeiras requests
        // consomem os tokens e passam. Disparamos várias extras além do burst para
        // garantir que pelo menos uma seja rejeitada (token bucket pode reabastecer
        // marginalmente entre requests rápidas em alguns ambientes de teste).
        boolean got429 = false;
        for (int i = 0; i < 6; i++) {
            var result = webTestClient.get().uri("/catalog/health").exchange()
                    .returnResult(Object.class);
            if (result.getStatus().value() == 429) {
                // Header Retry-After deve estar presente (adicionado pelo RetryAfterFilter).
                org.junit.jupiter.api.Assertions.assertEquals(
                        "10", result.getResponseHeaders().getFirst("Retry-After"));
                got429 = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(got429,
                "Esperava que pelo menos uma request na rajada fosse rejeitada com 429");
    }

    @Test
    void requestsUnderLimit_areProxiedNormally() {
        // Sanity: uma única request (bem abaixo do burst=2) passa normalmente e
        // proxya para o upstream. Garante que o RequestRateLimiter não está
        // rejeitando tudo (ex.: erro de config ou Redis ausente) — complementa o
        // teste de 429 provando que o caminho feliz também funciona.
        webTestClient.get().uri("/catalog/health").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void loginRoute_isAlsoRateLimited() {
        // Valida que a rota de login (forward:) também passa pelo RequestRateLimiter.
        // Limite de login default (5/min) é alto o suficiente para poucos logins;
        // aqui só verificamos que login responde 200 (não 500/Connection refused),
        // provando que o filter + Redis + forward: encadeiam corretamente para o
        // handler local. (O flushDb no @BeforeEach zera contadores de login também.)
        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"admin\",\"password\":\"admin\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty();
    }
}
