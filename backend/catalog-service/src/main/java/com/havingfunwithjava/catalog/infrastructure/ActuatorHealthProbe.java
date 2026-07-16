package com.havingfunwithjava.catalog.infrastructure;

import com.havingfunwithjava.catalog.domain.HealthInfo;
import com.havingfunwithjava.catalog.domain.HealthProbe;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * Adaptador de infraestrutura: implementa a porta {@link HealthProbe} usando
 * recursos concretos do Spring Boot Actuator (o {@link HealthEndpoint}) e um
 * {@link Clock} injetado (para relógio testável).
 *
 * Esta classe é o "adapter" da porta definida no domínio. Toda dependência
 * técnica (Spring, Actuator) mora aqui — o domínio permanece puro.
 */
@Component
public class ActuatorHealthProbe implements HealthProbe {

    private static final String SERVICE_NAME = "catalog-service";

    private final HealthEndpoint healthEndpoint;
    private final Clock clock;

    public ActuatorHealthProbe(HealthEndpoint healthEndpoint, Clock clock) {
        this.healthEndpoint = healthEndpoint;
        this.clock = clock;
    }

    @Override
    public HealthInfo check() {
        Status status = healthEndpoint.health().getStatus();
        return new HealthInfo(
                mapStatus(status),
                SERVICE_NAME,
                Instant.now(clock)
        );
    }

    private String mapStatus(Status status) {
        return Status.UP.equals(status) ? "UP" : status.getCode();
    }
}
