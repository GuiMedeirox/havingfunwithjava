package com.havingfunwithjava.gateway;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Base para testes de integração do api-gateway.
 *
 * <p>A partir da issue #11, todas as rotas do gateway carregam o filter
 * {@code RequestRateLimiter}, que executa o script Lua do token bucket no Redis
 * em cada request. Por isso QUALQUER teste que dispara uma request cascateada por
 * uma rota precisa de um Redis real — sem ele, o limiter falha ao conectar e a
 * request explode (500) em vez de passar.
 *
 * <p>Esta base sobe um único Redis (singleton container) compartilhado entre todas
 * as classes que a estendem. Usamos {@link GenericContainer} com a imagem
 * {@code redis:7-alpine} (a mesma do docker-compose): o
 * {@code RedisContainerConnectionDetailsFactory} do spring-boot-testcontainers
 * detecta containers cuja imagem começa com {@code redis} e registra
 * {@code spring.data.redis.host/port} via {@link ServiceConnection} — sem
 * depender do artefato extra {@code com.redis:testcontainers-redis}.
 *
 * <p>O container é {@code static} + {@code @Container}: iniciado uma vez por JVM
 * e reusado por todos os ApplicationContexts de teste (padrão "singleton
 * container" do Testcontainers), evitando reiniciar a app entre classes.
 *
 * <p>Expor a porta 6379 {@code @ServiceConnection} injeta host/porta corretos.
 * Subclasses fornecem {@code CATALOG_SERVICE_URL} via {@code @DynamicPropertySource}
 * (stub WireMock) — esta base cuida apenas do Redis.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractGatewayIntegrationTest {

    /**
     * Redis 7 (mesma imagem do docker-compose) para o ReactiveRedisRateLimiter.
     * {@link ServiceConnection} preenche {@code spring.data.redis.*} a partir do
     * host/porta expostos pelo container.
     */
    @Container
    @ServiceConnection
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
}
