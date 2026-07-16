package com.havingfunwithjava.gateway.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuração do JWT, ligada ao prefixo {@code app.jwt} no application.yml.
 *
 * <p>Propriedades:
 * <ul>
 *   <li>{@code app.jwt.secret} — chave HMAC (HS256). Padrão dev apenas; em produção
 *       sobrescrever via variável de ambiente {@code JWT_SECRET}.</li>
 *   <li>{@code app.jwt.expiration-minutes} — tempo de vida do token em minutos
 *       (default 60). Atributo exposto como {@link #expirationMinutes}; convertido
 *       para {@link Duration} em {@link #expiration()} para uso no {@link JwtService}.</li>
 * </ul>
 *
 * <p>Bind declarado em {@link com.havingfunwithjava.gateway.ApiGatewayApplication}
 * via {@code @EnableConfigurationProperties(JwtProperties.class)} para manter o
 * registro explícito (em vez de {@code @ConfigurationPropertiesScan}).
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Segredo dev — explícito para a app rodar sem config, mas claramente não-prod. */
    private String secret = "dev-secret-please-override-via-JWT_SECRET-env-var-havingfunwithjava";

    /** Expiração padrão de 1h (portfolio-grade, sem refresh token). */
    private long expirationMinutes = 60;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    /** Conveniência: expiração já como Duration para o JwtService usar direto. */
    public Duration expiration() {
        return Duration.ofMinutes(expirationMinutes);
    }
}
