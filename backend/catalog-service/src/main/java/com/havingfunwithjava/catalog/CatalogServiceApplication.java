package com.havingfunwithjava.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada (bootstrap) do catalog-service.
 *
 * Vive no pacote raiz {@code com.havingfunwithjava.catalog} para que o
 * component-scan do Spring cubra todas as camadas (domain, application,
 * infrastructure, interfaces). A classe em si é apenas técnica — não contém
 * regra de negócio.
 */
@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
