package com.havingfunwithjava.gateway.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Configuração do rate limiting no api-gateway (issue #11).
 *
 * <p>O Spring Cloud Gateway fornece o filter {@code RequestRateLimiter}, que delega
 * a contagem (token bucket via script Lua) a um {@link
 * org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter} sobre o Redis
 * reativo. Este bean é auto-configurado quando {@code spring-boot-starter-data-redis-reactive}
 * está no classpath. Aqui fornecemos apenas:
 *
 * <ul>
 *   <li>{@link #ipKeyResolver()} — resolve a chave de limite (IP do cliente).</li>
 *   <li>{@link #retryAfterHeader()} — {@link GlobalFilter} que adiciona o header
 *       {@code Retry-After} às respostas 429, pois o RequestRateLimiter não o faz
 *       por padrão.</li>
 * </ul>
 *
 * <p>Os limites por rota (replenishRate / burstCapacity / requestedTokens) são
 * declarados em {@code application.yml} nos filtros {@code RequestRateLimiter} de
 * cada rota, permitindo diferenciar catálogo (generoso), login (apertado) e escrita
 * admin (moderado).
 */
@Configuration
public class RateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Resolve a chave do rate limiter para o IP do cliente (issue #11, critério:
     * "Limite é por IP").
     *
     * <p>Ordem de precedência:
     * <ol>
     *   <li>Primeiro IP do header {@code X-Forwarded-For} (quando o gateway está
     *       atrás de um proxy/reverse proxy que repassa o IP original).</li>
     *   <li>Endereço remoto da conexão TCP (fallback direto).</li>
     *   <li>Literal {@code 0.0.0.0} (limite compartilhado) caso ambos sejam
     *       indisponíveis — evita NPE no {@code RequestRateLimiter}.</li>
     * </ol>
     *
     * <p>Não usamos o {@code PrincipalNameKeyResolver} porque rotas públicas (login,
     * GET do catálogo) não têm principal autenticado; o IP cobre todos os casos.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(clientIp(exchange));
    }

    private static String clientIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        List<String> forwarded = request.getHeaders().get(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isEmpty()) {
            // X-Forwarded-For pode ter múltiplos IPs: "client, proxy1, proxy2".
            // O primeiro é o cliente original.
            String first = forwarded.get(0);
            if (first != null) {
                String ip = first.split(",")[0].trim();
                if (!ip.isEmpty()) {
                    return ip;
                }
            }
        }
        InetSocketAddress remote = request.getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }
        return "0.0.0.0";
    }

    /**
     * {@link GlobalFilter} que adiciona o header {@code Retry-After} (em segundos)
     * a toda resposta 429 Too Many Requests (issue #11).
     *
     * <p>O {@code RequestRateLimiter} rejeita com 429 mas não comunica ao cliente
     * quando tentar de novo. Este filtro estima o tempo de espera a partir da taxa
     * de reposição configurada da rota: com token bucket, 1 token é reposto a cada
     * {@code (60 / replenishRate)} segundos. Usamos exatamente esse valor como
     * dica conservadora (o cliente terá 1 token disponível após esse intervalo).
     *
     * <p>A estimativa é arredondada para cima e tem mínimo de 1 segundo. Como não
     * há mapeamento direto filtro→replenishRate no contexto do exchange, usamos o
     * valor de pior caso da rota mais apertada (login) configurado em
     * {@code app.ratelimit.retry-after-seconds}, com default 60s — seguro e nunca
     * subestima o tempo real de reset.
     *
     * <p><b>Implementação:</b> o {@code RequestRateLimiter} rejeita chamando
     * {@code exchange.getResponse().setComplete()} imediatamente, o que faz o
     * commit da resposta. Um {@code GlobalFilter} que inspeciona o status DEPOIS
     * do {@code chain.filter(...)} (via {@code .then(...)}) chega tarde demais:
     * os headers já estão travados. Por isso registramos um callback
     * {@code beforeCommit} no response logo no início do filtro: esse hook roda
     * no momento do commit, qualquer que seja o responsável (o limiter, um
     * upstream, ou um 404), permitindo adicionar o header quando o status já é
     * 429 mas antes dos bytes irem ao cliente.
     */
    @Bean
    public GlobalFilter retryAfterHeader(RateLimitProperties properties) {
        return new RetryAfterFilter(properties.getRetryAfterSeconds());
    }

    /** Filtro concreto, separado para facilitar leitura e teste. */
    static final class RetryAfterFilter implements GlobalFilter, Ordered {

        private final int retryAfterSeconds;

        RetryAfterFilter(int retryAfterSeconds) {
            this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // Registra um hook que roda no commit do response. Assim, quando o
            // RequestRateLimiter chamar setComplete() com status 429, nosso hook
            // dispara ANTES dos bytes serem escritos e ainda pode adicionar headers.
            exchange.getResponse().beforeCommit(() -> {
                HttpStatusCode status = exchange.getResponse().getStatusCode();
                if (status != null && status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                    String value = String.valueOf(retryAfterSeconds);
                    exchange.getResponse().getHeaders().add(HttpHeaders.RETRY_AFTER, value);
                    log.debug("Rate limit excedido em {}: Retry-After={}s",
                            exchange.getRequest().getPath(), value);
                }
                return Mono.empty();
            });
            return chain.filter(exchange);
        }

        @Override
        public int getOrder() {
            // Alta precedência (ordem muito negativa): registra o beforeCommit ANTES
            // de qualquer route filter (incluindo o RequestRateLimiter) rodar. O hook
            // em si só dispara no commit, mas o registro precisa acontecer cedo.
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
