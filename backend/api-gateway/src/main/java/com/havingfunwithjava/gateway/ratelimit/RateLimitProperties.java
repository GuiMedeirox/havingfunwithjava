package com.havingfunwithjava.gateway.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de rate limiting do gateway, ligadas ao prefixo {@code app.ratelimit}.
 *
 * <p>Mantém apenas valores que NÃO são por-rota (estes ficam nos filtros
 * {@code RequestRateLimiter} de cada rota em {@code application.yml}). Hoje:
 * <ul>
 *   <li>{@code retry-after-seconds} — valor (conservador) do header {@code Retry-After}
 *       adicionado às respostas 429 pelo {@code RetryAfterFilter}. Default 60s,
 *       alinhado à janela de 1 minuto usada nos limites das rotas.</li>
 * </ul>
 *
 * <p>Bind registrado em {@link com.havingfunwithjava.gateway.ApiGatewayApplication}
 * via {@code @EnableConfigurationProperties}, mesmo padrão do {@code JwtProperties}.
 */
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {

    /**
     * Segundos até o cliente poder tentar de novo, reportados no header
     * {@code Retry-After} das respostas 429. Default conservador: 60s (janela de
     * 1 minuto dos limites por rota).
     */
    private int retryAfterSeconds = 60;

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public void setRetryAfterSeconds(int retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
