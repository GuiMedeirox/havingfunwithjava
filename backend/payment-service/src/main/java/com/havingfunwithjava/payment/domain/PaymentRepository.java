package com.havingfunwithjava.payment.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de domínio: repositório de pagamentos.
 *
 * <p>Interface pura (porta) declarada no domínio. A implementação JPA vive em
 * infrastructure.
 */
public interface PaymentRepository {

    /**
     * Persiste (ou atualiza) um pagamento.
     */
    Payment save(Payment payment);

    /**
     * Busca um pagamento pelo id.
     */
    Optional<Payment> findById(PaymentId id);

    /**
     * Busca o pagamento de um pedido (1 pedido → 1 pagamento neste fluxo).
     * Vazio se não houver.
     */
    Optional<Payment> findByOrderId(UUID orderId);
}
