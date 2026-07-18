package com.havingfunwithjava.orders.application;

import com.havingfunwithjava.orders.domain.Order;
import com.havingfunwithjava.orders.domain.OrderId;
import com.havingfunwithjava.orders.domain.OrderRepository;
import com.havingfunwithjava.orders.domain.PaymentFailed;
import com.havingfunwithjava.orders.domain.PaymentSucceeded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: aplicar o resultado do pagamento a um pedido (issue #22).
 *
 * <p>Consome {@link PaymentSucceeded} / {@link PaymentFailed} do payment-service
 * (via {@code PaymentResultConsumer}) e transita o status do pedido conforme a
 * máquina de estados (issue #17):
 * <ul>
 *   <li>Sucesso: {@code PENDING_PAYMENT → PAID}. Estoque confirmado (definitivo).</li>
 *   <li>Falha: {@code PENDING_PAYMENT → PAYMENT_FAILED → CANCELLED}. Estoque
 *       liberado (reposto no catalog — placeholder neste slice, pois o catalog
 *       ainda não tem controle de estoque).</li>
 * </ul>
 *
 * <p><b>Idempotência</b>: se o pedido já está num estado terminal (PAID, CANCELLED),
 * o evento é ignorado (log de warning). Transições inválidas (ex.: PAYMENT_FAILED
 * → PAID) são protegidas pela máquina de estados do {@link Order} e simplesmente
 * logadas — não quebram o consumer.
 */
@Service
public class ApplyPaymentResultUseCase {

    private static final Logger log = LoggerFactory.getLogger(ApplyPaymentResultUseCase.class);

    private final OrderRepository orderRepository;

    public ApplyPaymentResultUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Processa um pagamento bem-sucedido: marca o pedido como PAID.
     */
    @Transactional
    public void onPaymentSucceeded(PaymentSucceeded event) {
        Order order = findOrder(event.orderId());
        if (order == null) return;

        if (order.status().name().equals("PAID")) {
            log.info("Pedido {} já está PAID; evento duplicado ignorado (idempotência)", order.id());
            return;
        }

        try {
            Order paid = order.markAsPaid();
            orderRepository.save(paid);
            log.info("Pedido {} marcado como PAID", order.id());
            // Estoque: confirmado definitivamente. Catalog atual não tem controle
            // de estoque, então esta é uma confirmação lógica (no-op física).
        } catch (Exception ex) {
            log.warn("Transição inválida ao marcar pedido {} como PAID (status atual: {}): {}",
                    order.id(), order.status(), ex.getMessage());
        }
    }

    /**
     * Processa um pagamento falho: marca PAYMENT_FAILED → CANCELLED e libera estoque.
     */
    @Transactional
    public void onPaymentFailed(PaymentFailed event) {
        Order order = findOrder(event.orderId());
        if (order == null) return;

        if (order.status().name().equals("CANCELLED") || order.status().name().equals("PAYMENT_FAILED")) {
            log.info("Pedido {} já está {} (idempotência)", order.id(), order.status());
            return;
        }

        try {
            // PENDING_PAYMENT → PAYMENT_FAILED → CANCELLED
            Order failed = order.markAsPaymentFailed();
            Order cancelled = failed.cancel();
            orderRepository.save(cancelled);
            log.info("Pedido {} cancelado por falha de pagamento: {}", order.id(), event.reason());

            // Liberação de estoque: o catalog atual não tem controle de estoque
            // (só active flag). Em produção, chamaríamos o catalog para repor.
            // Placeholder documentado — a integração física entra quando o catalog
            // ganhar controle de estoque.
            log.info("Estoque reservado do pedido {} liberado (lógico; catalog sem estoque físico)",
                    order.id());
        } catch (Exception ex) {
            log.warn("Transição inválida ao cancelar pedido {} (status atual: {}): {}",
                    order.id(), order.status(), ex.getMessage());
        }
    }

    private Order findOrder(java.util.UUID orderId) {
        return orderRepository.findById(new OrderId(orderId)).orElse(null);
    }
}
