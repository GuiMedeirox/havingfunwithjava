package com.havingfunwithjava.gateway.auth;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.havingfunwithjava.gateway.AbstractGatewayIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Testes de slice vertical da autenticação JWT (issue #10).
 *
 * <p>Estratégia: contexto completo do gateway (@SpringBootTest, RANDOM_PORT —
 * herdados de {@link AbstractGatewayIntegrationTest}, que também sobe o Redis via
 * Testcontainers para o filter RequestRateLimiter) com o
 * {@code CATALOG_SERVICE_URL} apontando para um {@link WireMockServer} único e
 * durável (start em @BeforeAll, stop em @AfterAll). Os stubs são (re)registrados
 * em @BeforeEach — {@code resetAll} limpa o estado entre métodos.
 *
 * <p>Por que start/stop em @BeforeAll/@AfterAll (e não @BeforeEach/@AfterEach como
 * no ApiGatewayRoutingTest)? O routing-test para o stub a cada método no @AfterEach,
 * o que quebraria métodos subsequentes em classes com vários testes. Manter uma
 * única instância viva durante toda a classe é robusto e evita
 * "Connection refused" entre métodos.
 *
 * <p>Cobre os critérios de aceite:
 * <ul>
 *   <li>Login sucesso → 200 + JWT não-vazio (admin e client).</li>
 *   <li>Login falha → 401.</li>
 *   <li>Rota protegida (POST /catalog/...) com token admin válido → proxya upstream (201).</li>
 *   <li>Rota protegida sem token → 401.</li>
 *   <li>Rota protegida com token de role client → 401.</li>
 *   <li>Rota protegida com token expirado → 401.</li>
 *   <li>Rota protegida com token adulterado → 401.</li>
 *   <li>Rota pública GET /catalog/... segue sem token.</li>
 * </ul>
 *
 * <p>NOTA sobre rate limiting (issue #11): cada método faz poucas requests, bem
 * abaixo do limite apertado de login (5/min). Mas como o limite é por IP e a
 * classe inteira compartilha o mesmo IP de teste (127.0.0.1), métodos que fazem
 * login poderiam, em tese, acumular. Na prática cada login consome 1 token e há
 * poucos logins por classe, então não estouramos o burst. Se o teste ficar flaky,
 * a suíte usa um Redis fresco por JVM (ver AbstractGatewayIntegrationTest).
 */
class AuthFlowTest extends AbstractGatewayIntegrationTest {

    /** Stub único do catalog-service em porta aleatória, vivo durante toda a classe. */
    private static final WireMockServer CATALOG_STUB =
            new WireMockServer(options().dynamicPort());

    @DynamicPropertySource
    static void catalogUrl(DynamicPropertyRegistry registry) {
        CATALOG_STUB.start();
        String url = "http://localhost:" + CATALOG_STUB.port();
        registry.add("CATALOG_SERVICE_URL", () -> url);
        // Esta classe foca no fluxo de auth/JWT, não no rate limiting. Sobe todos
        // os limites bem acima do que a suíte consome (vários logins por classe,
        // todos do mesmo IP de teste) para evitar 429 falsos. Os limites reais
        // (apertados) são exercitados isoladamente em RateLimitTest.
        registry.add("RATE_LIMIT_LOGIN_REPLENISH", () -> "1000");
        registry.add("RATE_LIMIT_LOGIN_BURST", () -> "1000");
        registry.add("RATE_LIMIT_WRITE_REPLENISH", () -> "1000");
        registry.add("RATE_LIMIT_WRITE_BURST", () -> "1000");
        registry.add("RATE_LIMIT_READ_REPLENISH", () -> "1000");
        registry.add("RATE_LIMIT_READ_BURST", () -> "1000");
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

    @BeforeEach
    void setUpStub() {
        CATALOG_STUB.resetAll();
        // POST /products upstream (sem prefixo /catalog — prova StripPrefix).
        CATALOG_STUB.stubFor(post(urlEqualTo("/products"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"prod-1\",\"name\":\"Widget\"}")));
        // GET /products upstream (rota pública).
        CATALOG_STUB.stubFor(get(urlEqualTo("/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));
    }

    @Test
    void login_withValidAdminCredentials_returns200AndJwt() {
        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"admin\",\"password\":\"admin\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.username").isEqualTo("admin")
                .jsonPath("$.role").isEqualTo("admin");
    }

    @Test
    void login_withValidClientCredentials_returns200AndJwt() {
        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"cliente\",\"password\":\"cliente\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.role").isEqualTo("client");
    }

    @Test
    void login_withInvalidPassword_returns401() {
        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"admin\",\"password\":\"wrong\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_withUnknownUser_returns401() {
        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"nope\",\"password\":\"nope\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedCatalogRoute_withValidAdminToken_proxiesUpstream() {
        String token = loginAs("admin", "admin");

        webTestClient.post().uri("/catalog/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Widget\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("prod-1");

        CATALOG_STUB.verify(postRequestedFor(urlEqualTo("/products")));
    }

    @Test
    void protectedCatalogRoute_withoutToken_returns401() {
        webTestClient.post().uri("/catalog/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Widget\"}")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isNotEmpty();
    }

    @Test
    void protectedCatalogRoute_withClientRoleToken_returns401() {
        String token = loginAs("cliente", "cliente");

        webTestClient.post().uri("/catalog/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Widget\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedCatalogRoute_withExpiredToken_returns401() {
        String expired = expiredAdminToken();

        webTestClient.post().uri("/catalog/products")
                .header("Authorization", "Bearer " + expired)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Widget\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedCatalogRoute_withTamperedToken_returns401() {
        String token = loginAs("admin", "admin");
        // Quebra a assinatura trocando um char do payload segment.
        char last = token.charAt(token.length() - 2);
        char swapped = last == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, token.length() - 2) + swapped
                + token.charAt(token.length() - 1);

        webTestClient.post().uri("/catalog/products")
                .header("Authorization", "Bearer " + tampered)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Widget\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void publicGetRoute_worksWithoutToken() {
        // GET é pública — não pode cair no filtro (não pode dar 401).
        webTestClient.get().uri("/catalog/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();

        CATALOG_STUB.verify(getRequestedFor(urlEqualTo("/products")));
    }

    @Test
    void putCatalogRoute_isProtected_andRequiresAdmin() {
        String token = loginAs("admin", "admin");

        // PUT não tem stub, mas o que importa é NÃO ser 401 (passou pelo filtro).
        webTestClient.put().uri("/catalog/products/123")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"X\"}")
                .exchange()
                .expectStatus().value(s -> assertNotEquals(401, s));
    }

    // ---- helpers ----

    /** Faz login e devolve o token decodificando o JSON para LoginResponse. */
    private String loginAs(String username, String password) {
        LoginResponse response = webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult()
                .getResponseBody();
        return response.token();
    }

    /**
     * Emite um token admin já expirado com a MESMA chave dev default que a app usa
     * (testes rodam sem JWT_SECRET, então caem no default). Valida que o filtro
     * rejeita por expiração, não por assinatura.
     */
    private static String expiredAdminToken() {
        byte[] keyBytes = "dev-secret-please-override-via-JWT_SECRET-env-var-havingfunwithjava"
                .getBytes(StandardCharsets.UTF_8);
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
        Instant past = Instant.now().minus(Duration.ofHours(2));
        return io.jsonwebtoken.Jwts.builder()
                .subject("admin")
                .claim("role", "admin")
                .issuedAt(Date.from(past.minusSeconds(3600)))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();
    }
}
