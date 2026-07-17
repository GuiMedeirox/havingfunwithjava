package com.havingfunwithjava.gateway.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Controller de fallback do circuit breaker (issue #12).
 *
 * <p>Quando o circuito do catalog-service está ABERTO (após falhas repetidas),
 * o filter {@code CircuitBreaker} do Spring Cloud Gateway redireciona a request
 * para {@code forward:/fallback}, que cai aqui. Retornamos um 503 Service
 * Unavailable com ProblemDetail (RFC 7807) amigável, em vez de propagar o erro
 * 500/timeout do upstream.
 *
 * <p>O fallback é genérico (não distingue rota); o cliente recebe uma mensagem
 * clara de que o serviço está temporariamente indisponível. A rota original é
 * preservada no attribute {@code original-uri-path} caso queiramos enriquecer a
 * resposta no futuro.
 */
@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/fallback")
    public Mono<ProblemDetail> fallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        log.warn("Circuit breaker aberto: fallback acionado para {}", path);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço temporariamente indisponível. Tente novamente em alguns instantes.");
        problem.setTitle("Service unavailable");
        problem.setProperty("timestamp", Instant.now());
        return Mono.just(problem);
    }
}
