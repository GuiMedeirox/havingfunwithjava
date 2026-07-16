package com.havingfunwithjava.catalog.domain;

import java.time.Instant;

/**
 * Record imutável com a informação de saúde do serviço.
 *
 * Vive na camada de domínio: é um objeto puro, sem nenhuma dependência de Spring
 * ou de infraestrutura. A camada de application o produz e a de interfaces o expõe.
 *
 * @param status  estado lógico do serviço ("UP")
 * @param service nome do serviço para identificação
 * @param at      instante em que a leitura foi feita
 */
public record HealthInfo(String status, String service, Instant at) {
}
