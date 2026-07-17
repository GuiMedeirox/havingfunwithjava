package com.havingfunwithjava.payment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio: Pagamento (agregado raiz).
 *
 * <p>POJO puro (sem JPA, sem Spring). Representa a tentativa de pagamento de um
 * pedido. Invariantes: orderId não-nulo, método não-nulo, valor > 0.
 *
 * <p>Transições de status (issue #19):
 * <pre>
 *   PENDING ──authorize()──→ AUTHORIZED
 *   PENDING ──decline()────→ DECLINED
 *   PENDING ──fail()───────→ FAILED
 * </pre>
 *
 * @param id         identificador (gerado ao criar)
 * @param orderId    id do pedido que este pagamento liquida
 * @param method     método escolhido (cartão/Pix)
 * @param amount     valor ( amount + currency)
 * @param status     estado atual
 * @param attempts   número de tentativas já feitas (issue #20 usa para backoff)
 * @param createdAt  instante de criação
 * @param updatedAt  instante da última atualização de status
 */
public record Payment(
        PaymentId id,
        UUID orderId,
        PaymentMethod method,
        Money amount,
        PaymentStatus status,
        int attempts,
        Instant createdAt,
        Instant updatedAt
) {

    public Payment {
        Objects.requireNonNull(orderId, "orderId não pode ser nulo");
        Objects.requireNonNull(method, "method não pode ser nulo");
        Objects.requireNonNull(amount, "amount não pode ser nulo");
        Objects.requireNonNull(status, "status não pode ser nulo");
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("valor do pagamento deve ser maior que zero");
        }
        if (attempts < 0) {
            throw new IllegalArgumentException("tentativas não pode ser negativo");
        }
    }

    /**
     * Factory: cria um novo pagamento PENDING para um pedido.
     */
    public static Payment createNew(UUID orderId, PaymentMethod method, Money amount) {
        Instant now = Instant.now();
        return new Payment(
                PaymentId.generate(),
                orderId,
                method,
                amount,
                PaymentStatus.PENDING,
                1,
                now,
                now
        );
    }

    /**
     * Transição: gateway autorizou o pagamento.
     */
    public Payment authorize() {
        requireFrom(PaymentStatus.PENDING, PaymentStatus.AUTHORIZED);
        return withStatus(PaymentStatus.AUTHORIZED);
    }

    /**
     * Transição: gateway recusou (falha definitiva, ex.: cartão recusado).
     */
    public Payment decline() {
        requireFrom(PaymentStatus.PENDING, PaymentStatus.DECLINED);
        return withStatus(PaymentStatus.DECLINED);
    }

    /**
     * Transição: falha técnica após esgotar retentativas (issue #20).
     */
    public Payment fail() {
        requireFrom(PaymentStatus.PENDING, PaymentStatus.FAILED);
        return withStatus(PaymentStatus.FAILED);
    }

    /**
     * Incrementa o contador de tentativas (issue #20 — usado pelo backoff).
     */
    public Payment incrementAttempts() {
        return new Payment(id, orderId, method, amount, status, attempts + 1, createdAt, Instant.now());
    }

    private void requireFrom(PaymentStatus required, PaymentStatus target) {
        if (this.status != required) {
            throw new IllegalStateException(
                    "Transição inválida de pagamento: " + status + " → " + target);
        }
    }

    private Payment withStatus(PaymentStatus newStatus) {
        return new Payment(id, orderId, method, amount, newStatus, attempts, createdAt, Instant.now());
    }
}
