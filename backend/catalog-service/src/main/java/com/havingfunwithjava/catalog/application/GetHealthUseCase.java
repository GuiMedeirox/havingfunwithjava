package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.HealthInfo;
import com.havingfunwithjava.catalog.domain.HealthProbe;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: obter a saúde atual do serviço.
 *
 * Orquestra o domínio: pede a leitura para a porta {@link HealthProbe} (cuja
 * implementação é injetada por Spring, mas que o domínio não conhece).
 *
 * Vive na camada de application — pode depender do domínio (e de abstrações
 * técnicas anêmicas como a anotação @Service, que é estereótipo de Spring).
 * Aqui é onde moram as regras de orquestração dos casos de uso.
 */
@Service
public class GetHealthUseCase {

    private final HealthProbe healthProbe;

    public GetHealthUseCase(HealthProbe healthProbe) {
        this.healthProbe = healthProbe;
    }

    public HealthInfo execute() {
        return healthProbe.check();
    }
}
