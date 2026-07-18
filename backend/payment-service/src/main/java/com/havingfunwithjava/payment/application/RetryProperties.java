package com.havingfunwithjava.payment.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração de retentativas de pagamento (issue #20).
 *
 * <p>Quando o gateway retorna {@code FALHA_TRANSIENTE} (timeout, indisponível),
 * o payment-service retenta com backoff exponencial:
 * <pre>
 *   espera = initialDelayMs * (multiplier ^ (attempt - 1))
 *   ex.: 1000ms, 2000ms, 4000ms (com multiplier=2, initialDelay=1000)
 * </pre>
 *
 * <p>Após {@code maxAttempts} falhas consecutivas, o pagamento vai para FAILED
 * (definitivo) e publica PaymentFailed.
 *
 * <p>Bind registrado em {@code PaymentServiceApplication} via
 * {@code @EnableConfigurationProperties}.
 */
@ConfigurationProperties(prefix = "app.payment.retry")
public class RetryProperties {

    /**
     * Número máximo de tentativas (incluindo a primeira). Default 3.
     */
    private int maxAttempts = 3;

    /**
     * Delay inicial em milissegundos. Default 1000 (1s).
     */
    private long initialDelayMs = 1000;

    /**
     * Multiplicador do backoff exponencial. Default 2.0 (dobra a cada tentativa).
     */
    private double multiplier = 2.0;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public void setInitialDelayMs(long initialDelayMs) {
        this.initialDelayMs = initialDelayMs;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Calcula o delay (em ms) para a tentativa N (base-1).
     * Tentativa 1 não tem delay (é a primeira chamada).
     * Tentativa 2 → initialDelayMs
     * Tentativa 3 → initialDelayMs × multiplier
     * Tentativa N → initialDelayMs × multiplier^(N-2)
     */
    public long delayForAttempt(int attempt) {
        if (attempt <= 1) return 0;
        double delay = initialDelayMs * Math.pow(multiplier, attempt - 2);
        return (long) delay;
    }
}
