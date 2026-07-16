package com.havingfunwithjava.catalog.interfaces;

import com.havingfunwithjava.catalog.application.GetHealthUseCase;
import com.havingfunwithjava.catalog.domain.HealthInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controller REST que expõe a saúde do serviço.
 *
 * Vive na camada de interfaces (ponta de entrada). Não contém regra de negócio:
 * delega ao caso de uso {@link GetHealthUseCase} e traduz o modelo de domínio
 * {@link HealthInfo} para o DTO {@link HealthResponse} que vai pro cliente HTTP.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final GetHealthUseCase getHealth;

    public HealthController(GetHealthUseCase getHealth) {
        this.getHealth = getHealth;
    }

    @GetMapping
    public HealthResponse health() {
        HealthInfo info = getHealth.execute();
        return new HealthResponse(info.status(), info.service(), info.at().toString());
    }

    /**
     * DTO de resposta: formato estável e plano, desacoplado do modelo de domínio.
     * O instante é serializado como ISO-8601 (String) para o cliente.
     */
    public record HealthResponse(String status, String service, String at) {
    }
}
