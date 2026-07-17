package com.havingfunwithjava.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada (bootstrap) do orders-service.
 *
 * Vive no pacote raiz {@code com.havingfunwithjava.orders} para que o
 * component-scan do Spring cubra todas as camadas (domain, application,
 * infrastructure, interfaces).
 */
@SpringBootApplication
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}
