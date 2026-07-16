package com.havingfunwithjava.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point do api-gateway.
 *
 * Serviço de infraestrutura baseado em Spring Cloud Gateway (reativo/WebFlux).
 * As rotas são declaradas em {@code application.yml} (predicates + filters), não
 * em código — por isso esta classe é apenas o bootstrap. Não há Clean Architecture
 * em 4 camadas aqui: o gateway só roteia.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
