package com.havingfunwithjava.payment.infrastructure.messaging;

import com.havingfunwithjava.payment.domain.PaymentFailed;
import com.havingfunwithjava.payment.domain.PaymentResultEvent;
import com.havingfunwithjava.payment.domain.PaymentResultPublisher;
import com.havingfunwithjava.payment.domain.PaymentSucceeded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador: implementa {@link PaymentResultPublisher} publicando no RabbitMQ.
 *
 * <p>Publica {@link PaymentResultEvent} na exchange topic {@code orders.exchange}
 * (a mesma do OrderCreated) com routing keys distintas:
 * <ul>
 *   <li>{@code payment.succeeded} para {@link PaymentResultEvent.PaymentSucceeded}</li>
 *   <li>{@code payment.failed} para {@link PaymentResultEvent.PaymentFailed}</li>
 * </ul>
 * O orders-service (issue #22) consome da fila bindada a essas routing keys.
 *
 * <p>O {@link RabbitTemplate} cuida da serialização Jackson. A escolha da routing
 * key acontece aqui (adaptador), mantendo o domínio agnóstico ao protocolo.
 */
@Component
public class RabbitPaymentResultPublisher implements PaymentResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitPaymentResultPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String ordersExchange;
    private final String routingKeySucceeded;
    private final String routingKeyFailed;

    public RabbitPaymentResultPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.orders-exchange}") String ordersExchange,
            @Value("${app.messaging.result-routing-key-succeeded:payment.succeeded}") String routingKeySucceeded,
            @Value("${app.messaging.result-routing-key-failed:payment.failed}") String routingKeyFailed) {
        this.rabbitTemplate = rabbitTemplate;
        this.ordersExchange = ordersExchange;
        this.routingKeySucceeded = routingKeySucceeded;
        this.routingKeyFailed = routingKeyFailed;
    }

    @Override
    public void publish(PaymentResultEvent event) {
        String routingKey = switch (event) {
            case PaymentSucceeded s -> {
                log.info("Publicando PaymentSucceeded para o pedido {}", s.orderId());
                yield routingKeySucceeded;
            }
            case PaymentFailed f -> {
                log.info("Publicando PaymentFailed para o pedido {}: {}", f.orderId(), f.reason());
                yield routingKeyFailed;
            }
            default -> throw new IllegalArgumentException("Tipo de evento desconhecido: " + event);
        };
        rabbitTemplate.convertAndSend(ordersExchange, routingKey, event);
    }
}
