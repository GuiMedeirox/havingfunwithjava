package com.havingfunwithjava.gateway.auth;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * Filtro global do Spring Cloud Gateway que protege rotas de escrita do catalog.
 *
 * <p>Regras (issue #10):
 * <ul>
 *   <li><b>Públicas</b> (seguem sem token): {@code POST /auth/login},
 *       e qualquer {@code GET /catalog/**}.</li>
 *   <li><b>Protegidas</b> (exigem token admin): {@code POST/PUT/DELETE /catalog/**}.</li>
 * </ul>
 *
 * <p>Para rotas protegidas:
 * <ol>
 *   <li>Extrai {@code Authorization: Bearer <token>}.</li>
 *   <li>Valida assinatura + expiração via {@link JwtService}.</li>
 *   <li>Confere que o claim {@code role} = {@code admin}.</li>
 *   <li>Falha em qualquer passo → 401 com corpo JSON {@code {"error":"..."}} e
 *       o fluxo é encerrado (chain não continua).</li>
 * </ol>
 *
 * <p>Ordem {@link #getOrder()} alta negativa ({@code -100}) roda cedo, antes dos
 * filtros de rota. É um {@link GlobalFilter}, então só roda uma vez por request.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    /** Métodos HTTP considerados de escrita (protegidos no /catalog). */
    private static final Set<String> PROTECTED_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().pathWithinApplication().value();
        String method = request.getMethod().name();

        // 1. Rotas públicas: /auth/** sempre abertas.
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // 2. Apenas POST/PUT/DELETE/PATCH sob /catalog exigem admin. GET e demais passam.
        boolean isProtectedCatalog = path.startsWith("/catalog/") && PROTECTED_METHODS.contains(method);
        if (!isProtectedCatalog) {
            return chain.filter(exchange);
        }

        // 3. Extrai o Bearer token.
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return reject(exchange, "missing or invalid Authorization header");
        }
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // 4. Valida assinatura + expiração.
        Optional<Claims> claims = jwtService.parseAndVerify(token);
        if (claims.isEmpty()) {
            return reject(exchange, "invalid or expired token");
        }

        // 5. Confere role admin.
        String role = claims.get().get("role", String.class);
        if (!"admin".equals(role)) {
            return reject(exchange, "insufficient privileges: admin role required");
        }

        return chain.filter(exchange);
    }

    /**
     * Encerra o fluxo com 401 + corpo JSON simples.
     *
     * <p>Não usamos exception handler para manter o filtro autossuficiente e
     * determinístico (sem depender de ordenação de exception resolvers do WebFlux).
     */
    private Mono<Void> reject(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        log.debug("Auth rejected on {}: {}", exchange.getRequest().getPath(), reason);
        String body = "{\"error\":\"" + reason + "\"}";
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
