package com.havingfunwithjava.payment.infrastructure.messaging;

import com.havingfunwithjava.payment.application.ProcessPaymentUseCase;
import com.havingfunwithjava.payment.domain.Money;
import com.havingfunwithjava.payment.domain.OrderCreatedEvent;
import com.havingfunwithjava.payment.domain.PaymentMethod;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumer de {@code OrderCreated} da fila {@code payment.queue}.
 *
 * <p>Recebe o evento publicado pelo orders-service e dispara o processamento de
 * pagamento via {@link ProcessPaymentUseCase}. Usa MANUAL ack: só confirma a
 * mensagem após processar com sucesso. Se o processamento lançar exceção, a
 * mensagem é nack'd (rejeitada) — o Spring AMQP a reenfileira ou envia à DLQ
 * conforme a config de retry (issue #20 aprofunda o backoff).
 *
 * <p>NOTA sobre método: neste slice, o método é fixo (CREDIT_CARD). Em produção,
 * viria no evento OrderCreated ou seria escolhido pelo cliente no checkout.
 */
@Component
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);

    private final ProcessPaymentUseCase processPayment;

    public PaymentConsumer(ProcessPaymentUseCase processPayment) {
        this.processPayment = processPayment;
    }

    @RabbitListener(queues = "${app.messaging.payment-queue}")
    public void onOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) throws IOException {

        log.info("Recebido OrderCreated para o pedido {} (total {} {})",
                event.orderId(), event.amount(), event.currency());

        try {
            // Neste slice, método fixo em cartão. Em produção, viria do evento.
            PaymentMethod method = PaymentMethod.CREDIT_CARD;
            Money amount = Money.of(event.amount(), event.currency());

            processPayment.execute(event.orderId(), method, amount);

            // Ack manual: confirma processamento bem-sucedido
            channel.basicAck(deliveryTag, false);
            log.info("Pagamento processado e ack'd para o pedido {}", event.orderId());

        } catch (Exception ex) {
            // Nack: rejeita a mensagem. requeue=false envia à DLQ (após retry policy);
            // requeue=true reenfileira para retentativa imediata.
            log.error("Erro ao processar pagamento do pedido {}; nack",
                    event.orderId(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
