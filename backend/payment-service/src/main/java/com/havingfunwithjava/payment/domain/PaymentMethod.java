package com.havingfunwithjava.payment.domain;

/**
 * Método de pagamento suportado pelo payment-service.
 *
 * <p>Cada método tem sua {@link PaymentStrategy} correspondente (issue #19).
 */
public enum PaymentMethod {
    CREDIT_CARD,
    PIX
}
