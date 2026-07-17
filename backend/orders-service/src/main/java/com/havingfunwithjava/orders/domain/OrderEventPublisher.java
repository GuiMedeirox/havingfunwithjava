package com.havingfunwithjava.orders.domain;

/**
 * Porta de domínio: publica eventos de pedido.
 *
 * <p>Abstração da infraestrutura de mensageria (RabbitMQ na implementação real,
 * pode ser mock em testes). O domínio não sabe que há AMQP embaixo — só declara
 * a intenção de publicar um {@link OrderCreatedEvent}.
 */
public interface OrderEventPublisher {

    /**
     * Publica o evento de pedido criado.
     */
    void publishOrderCreated(OrderCreatedEvent event);
}
