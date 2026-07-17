package com.havingfunwithjava.payment.domain;

/**
 * Porta de domínio: publica o resultado do processamento de pagamento.
 *
 * <p>Abstração da infraestrutura de mensageria (RabbitMQ na implementação real,
 * mock em testes). O domínio não sabe que há AMQP embaixo.
 */
public interface PaymentResultPublisher {

    /**
     * Publica o resultado do pagamento ({@link PaymentResultEvent}).
     * O orders-service (issue #22) consome e atualiza o status do pedido.
     */
    void publish(PaymentResultEvent event);
}
