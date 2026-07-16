package com.havingfunwithjava.catalog;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base para testes de integração (Seam 1: slice vertical).
 *
 * Usa o padrão "singleton container" oficial do Testcontainers: um único
 * container Postgres estático, compartilhado entre TODAS as classes de teste
 * que herdam desta base. O container é iniciado uma única vez (bloco estático)
 * e vive por toda a JVM. As propriedades de conexão são registradas via
 * {@link DynamicPropertySource}, sobrepondo as do application.yml.
 *
 * Isto evita o problema de "dirty context": múltiplos ApplicationContexts
 * reutilizam a MESMA conexão/porta, em vez de cada um subir seu próprio
 * container efêmero que pode ser reciclado entre testes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    /**
     * Container Postgres único para toda a suíte de testes.
     * Iniciado eager no carregamento da classe; parado no shutdown da JVM.
     */
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    /**
     * Registra dinamicamente as credenciais do container, sobrepondo
     * qualquer datasource do application.yml. Roda uma vez por ApplicationContext.
     */
    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
