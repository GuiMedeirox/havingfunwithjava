package com.havingfunwithjava.catalog.domain;

/**
 * Porta de domínio: fornece uma leitura de saúde do serviço.
 *
 * É uma interface pura (porta) declarada no domínio. A implementação concreta
 * (adaptador) vive em infrastructure e injeta o que for preciso (ex.: relógio,
 * indicadores do Actuator). Isso mantém o domínio desacoplado de detalhes técnicos.
 *
 * Este é o padrão "hexagonal ports & adapters": o domínio define a porta; a
 * infraestrutura a implementa; a application a consome.
 */
public interface HealthProbe {
    HealthInfo check();
}
