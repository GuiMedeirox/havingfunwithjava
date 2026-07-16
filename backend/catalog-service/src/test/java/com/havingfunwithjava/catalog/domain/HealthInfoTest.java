package com.havingfunwithjava.catalog.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de domínio (Seam 2): JUnit puro, sem contexto Spring.
 *
 * Valida o value object HealthInfo isoladamente. Confirma que a camada de
 * domínio é testável sem nenhuma dependência técnica — prova da independência
 * do domínio em Clean Architecture.
 */
class HealthInfoTest {

    @Test
    void shouldHoldValues() {
        Instant now = Instant.now();
        HealthInfo info = new HealthInfo("UP", "catalog-service", now);

        assertEquals("UP", info.status());
        assertEquals("catalog-service", info.service());
        assertEquals(now, info.at());
    }
}
