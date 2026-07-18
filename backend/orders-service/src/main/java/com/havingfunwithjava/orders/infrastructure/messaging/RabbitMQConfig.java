package com.havingfunwithjava.orders.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para o fluxo orders → payment (issue #18).
 *
 * <p>Topologia declarada:
 * <pre>
 *   orders.exchange (topic)
 *     └── order.created → payment.queue
 *                          └── x-dead-letter-exchange → orders.dlx (direct)
 *                                                         └── payment.dlq
 * </pre>
 *
 * <p>Quando o payment-service (issue #19) não conseguir processar uma mensagem
 * após retentativas, ela vai para a DLQ via Dead-Letter Exchange. Isso evita
 * "message poison" (mensagem falha sendo reprocessada infinitamente).
 *
 * <p>O {@link MessageConverter} Jackson serializa eventos como JSON — formato
 * estável e legível, independente da serialização Java.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.messaging.orders-exchange}")
    private String ordersExchange;

    @Value("${app.messaging.payment-queue}")
    private String paymentQueue;

    @Value("${app.messaging.payment-routing-key}")
    private String paymentRoutingKey;

    @Value("${app.messaging.dlx-exchange}")
    private String dlxExchange;

    @Value("${app.messaging.dlq-queue}")
    private String dlqQueue;

    // --- Fila de resultado (issue #22): consome PaymentSucceeded/Failed do payment-service ---

    @Value("${app.messaging.result-queue:orders.payment-result.queue}")
    private String resultQueue;

    @Value("${app.messaging.result-routing-key-succeeded:payment.succeeded}")
    private String resultRoutingKeySucceeded;

    @Value("${app.messaging.result-routing-key-failed:payment.failed}")
    private String resultRoutingKeyFailed;

    // --- Exchange principal (topic): orders.exchange ---

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ordersExchange, true, false);
    }

    // --- Fila principal: payment.queue com DLX configurado ---

    @Bean
    public Queue paymentQueue() {
        // x-dead-letter-exchange: mensagens rejeitadas/expiradas vão para orders.dlx.
        return QueueBuilder.durable(paymentQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqQueue)
                .build();
    }

    @Bean
    public Binding paymentBinding() {
        // payment.queue recebe mensagens de orders.exchange com routing key order.created
        return BindingBuilder.bind(paymentQueue())
                .to(ordersExchange())
                .with(paymentRoutingKey);
    }

    // --- Fila de resultado (issue #22): orders.payment-result.queue ---
    // O payment-service publica PaymentSucceeded/Failed aqui; o consumer do
    // orders-service atualiza o status do pedido.

    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(resultQueue).build();
    }

    @Bean
    public Binding resultSucceededBinding() {
        return BindingBuilder.bind(resultQueue())
                .to(ordersExchange())
                .with(resultRoutingKeySucceeded);
    }

    @Bean
    public Binding resultFailedBinding() {
        return BindingBuilder.bind(resultQueue())
                .to(ordersExchange())
                .with(resultRoutingKeyFailed);
    }

    // --- Dead-Letter Exchange (direct) + DLQ ---

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
        // payment.dlq recebe tudo que chega no DLX com routing key = payment.dlq
        return BindingBuilder.bind(dlqQueue())
                .to(dlxExchange())
                .with(dlqQueue);
    }

    // --- Serialização JSON (Jackson) ---

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Customiza o RabbitTemplate auto-configurado para usar o converter Jackson.
     * Garante que eventos sejam serializados como JSON (não serialização Java).
     */
    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        org.springframework.amqp.rabbit.core.RabbitTemplate template =
                new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
