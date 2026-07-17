package com.havingfunwithjava.gateway;

import com.havingfunwithjava.gateway.auth.JwtProperties;
import com.havingfunwithjava.gateway.ratelimit.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point do api-gateway.
 *
 * Serviço de infraestrutura baseado em Spring Cloud Gateway (reativo/WebFlux).
 * As rotas são declaradas em {@code application.yml} (predicates + filters), não
 * em código — por isso esta classe é apenas o bootstrap. Não há Clean Architecture
 * em 4 camadas aqui: o gateway só roteia.
 *
 * <p>{@link EnableConfigurationProperties} registra {@link JwtProperties} (prefixo
 * {@code app.jwt}) e {@link RateLimitProperties} (prefixo {@code app.ratelimit})
 * explicitamente, em vez de scanear o classpath inteiro.
 */
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, RateLimitProperties.class})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
