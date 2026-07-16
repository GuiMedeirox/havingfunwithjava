package com.havingfunwithjava.catalog.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuração de beans técnicos da infraestrutura.
 *
 * O {@link Clock} é exposto como bean para permitir relógio determinístico nos
 * testes (padrão: zone UTC). Provedores externos, datasources e clientes
 * entrarão aqui conforme os slices evoluírem.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("UTC"));
    }
}
