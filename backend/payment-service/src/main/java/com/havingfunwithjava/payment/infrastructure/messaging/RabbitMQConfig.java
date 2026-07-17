package com.havingfunwithjava.payment.infrastructure.messaging;

import com.havingfunwithjava.payment.domain.OrderCreatedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuração RabbitMQ do payment-service.
 *
 * <p>Declara a MESMA topologia do orders-service (idempotente — se o orders-service
 * já declarou, o RabbitMQ ignora; se o payment-service sobe primeiro, ele declara).
 * Isso garante que o consumer funcione independente da ordem de boot.
 *
 * <p>Além da topologia, configura o {@link MessageConverter} Jackson com
 * {@link DefaultJackson2JavaTypeMapper} mapeando o evento para
 * {@link OrderCreatedEvent} — assim o consumer desserializa o JSON no DTO correto.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.messaging.orders-exchange}")
    private String ordersExchange;

    @Value("${app.messaging.payment-queue}")
    private String paymentQueue;

    @Value("${app.messaging.payment-routing-key}")
    private String paymentRoutingKey;

    @Value("${app.messaging.dlx-exchange:orders.dlx}")
    private String dlxExchange;

    @Value("${app.messaging.dlq-queue:payment.dlq}")
    private String dlqQueue;

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ordersExchange, true, false);
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(paymentQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqQueue)
                .build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(ordersExchange())
                .with(paymentRoutingKey);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchange, true, false);
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(dlqQueue).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue())
                .to(dlxExchange())
                .with(dlqQueue);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // Permite desserializar o JSON no DTO OrderCreatedEvent (trusted package).
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.havingfunwithjava.payment.domain");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
