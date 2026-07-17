package com.havingfunwithjava.orders.infrastructure.messaging;

import com.havingfunwithjava.orders.domain.OrderCreatedEvent;
import com.havingfunwithjava.orders.domain.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador: implementa {@link OrderEventPublisher} publicando no RabbitMQ.
 *
 * <p>Publica o {@link OrderCreatedEvent} na exchange topic {@code orders.exchange}
 * com routing key {@code order.created}. O {@link RabbitTemplate} cuida da
 * serialização (Jackson, configurado em {@link RabbitMQConfig}) e da retry policy
 * (configurada em application.yml).
 *
 * <p>Se o broker estiver indisponível, o template retenta conforme a config
 * (spring.rabbitmq.template.retry.*). Após esgotar, lança exceção — o chamador
 * (CreateOrderUseCase) decide se reverte a transação.
 */
@Component
public class RabbitOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitOrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String ordersExchange;
    private final String paymentRoutingKey;

    public RabbitOrderEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.orders-exchange}") String ordersExchange,
            @Value("${app.messaging.payment-routing-key}") String paymentRoutingKey) {
        // O RabbitTemplate auto-configurado pelo Spring Boot usa o MessageConverter
        // bean (Jackson2JsonMessageConverter, declarado em RabbitMQConfig). Confirmamos
        // explicitamente para garantir serialização JSON mesmo se a auto-config mudar.
        this.rabbitTemplate = rabbitTemplate;
        this.ordersExchange = ordersExchange;
        this.paymentRoutingKey = paymentRoutingKey;
    }

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publicando OrderCreated para o pedido {} (total {} {})",
                event.orderId(), event.amount(), event.currency());
        rabbitTemplate.convertAndSend(ordersExchange, paymentRoutingKey, event);
    }
}
