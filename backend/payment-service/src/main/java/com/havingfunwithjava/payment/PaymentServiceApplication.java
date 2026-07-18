package com.havingfunwithjava.payment;

import com.havingfunwithjava.payment.application.RetryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Ponto de entrada (bootstrap) do payment-service.
 *
 * <p>Vive no pacote raiz {@code com.havingfunwithjava.payment} para que o
 * component-scan do Spring cubra todas as camadas (domain, application,
 * infrastructure, interfaces).
 */
@SpringBootApplication
@EnableConfigurationProperties(RetryProperties.class)
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
