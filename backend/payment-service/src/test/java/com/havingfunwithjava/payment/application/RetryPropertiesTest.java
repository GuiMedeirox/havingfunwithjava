package com.havingfunwithjava.payment.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste unitário (Seam 2): cálculo de backoff exponencial do {@link RetryProperties}.
 *
 * <p>Valida que os delays seguem o padrão exponencial esperado e que a decisão
 * de parar (maxAttempts) funciona corretamente.
 */
class RetryPropertiesTest {

    @Test
    void shouldCalculateExponentialBackoff() {
        RetryProperties props = new RetryProperties();
        // defaults: initialDelay=1000, multiplier=2.0
        // tentativa 1: sem delay (primeira chamada)
        assertEquals(0, props.delayForAttempt(1));
        // tentativa 2: 1000ms
        assertEquals(1000, props.delayForAttempt(2));
        // tentativa 3: 1000 × 2 = 2000ms
        assertEquals(2000, props.delayForAttempt(3));
        // tentativa 4: 1000 × 2² = 4000ms
        assertEquals(4000, props.delayForAttempt(4));
    }

    @Test
    void shouldRespectCustomMultiplier() {
        RetryProperties props = new RetryProperties();
        props.setInitialDelayMs(500);
        props.setMultiplier(3.0);
        // tentativa 2: 500
        assertEquals(500, props.delayForAttempt(2));
        // tentativa 3: 500 × 3 = 1500
        assertEquals(1500, props.delayForAttempt(3));
    }

    @Test
    void delaysShouldIncreaseExponentially() {
        RetryProperties props = new RetryProperties();
        long prev = 0;
        for (int i = 2; i <= 5; i++) {
            long delay = props.delayForAttempt(i);
            assertTrue(delay > prev, "Delay da tentativa " + i + " deveria ser maior que o anterior");
            prev = delay;
        }
    }

    @Test
    void shouldDefaultToThreeAttempts() {
        RetryProperties props = new RetryProperties();
        assertEquals(3, props.getMaxAttempts());
    }
}
