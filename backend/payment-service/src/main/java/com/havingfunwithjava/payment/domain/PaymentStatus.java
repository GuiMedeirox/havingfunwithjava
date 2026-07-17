package com.havingfunwithjava.payment.domain;

/**
 * Estado de um pagamento no ciclo de vida.
 *
 * <pre>
 *   PENDING ──→ AUTHORIZED  (gateway autorizou)
 *   PENDING ──→ DECLINED    (gateway recusou — falha definitiva, cartão recusado)
 *   PENDING ──→ FAILED      (falha técnica após retentativas, issue #20)
 * </pre>
 *
 * <p>Estados terminais: AUTHORIZED, DECLINED, FAILED.
 */
public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    DECLINED,
    FAILED
}
