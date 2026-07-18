package com.havingfunwithjava.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Configuração CORS global do api-gateway (WebFlux).
 *
 * <p>O frontend (localhost:5173) faz requisições para o gateway (localhost:8080) —
 * origens diferentes, o que dispara CORS preflight no browser. Este filtro adiciona
 * os headers Access-Control-Allow-* em todas as respostas, permitindo o tráfego.
 *
 * <p>Em produção, restringir allowedOriginPatterns para o domínio do frontend.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // allowedOriginPatterns("*") funciona com allowCredentials=true
        // (allowedOrigins("*") não funciona com credentials).
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
