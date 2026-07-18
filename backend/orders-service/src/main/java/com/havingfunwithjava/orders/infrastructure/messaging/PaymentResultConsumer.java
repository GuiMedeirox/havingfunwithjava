package com.havingfunwithjava.orders.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havingfunwithjava.orders.application.ApplyPaymentResultUseCase;
import com.havingfunwithjava.orders.domain.PaymentFailed;
import com.havingfunwithjava.orders.domain.PaymentSucceeded;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Consumer de {@code PaymentSucceeded}/{@code PaymentFailed} da fila
 * {@code orders.payment-result.queue} (issue #22).
 *
 * <p>Recebe o payload como String (JSON cru) e desserializa manualmente. Isso
 * desacopla do tipo concreto publicado pelo payment-service: como os serviços
 * têm pacotes diferentes, a desserialização automática por {@code __TypeId__}
 * não funciona. Lendo o JSON e checando a presença do campo discriminador
 * ({@code paymentId} → sucesso; {@code reason} → falha) fica robusto.
 *
 * <p>Usa MANUAL ack: só confirma após aplicar o resultado. Erros vão para nack.
 */
@Component
public class PaymentResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final ApplyPaymentResultUseCase applyResult;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaymentResultConsumer(ApplyPaymentResultUseCase applyResult) {
        this.applyResult = applyResult;
    }

    @RabbitListener(queues = "${app.messaging.result-queue}")
    public void onPaymentResult(
            @Payload String json,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) throws IOException {

        try {
            JsonNode node = mapper.readTree(json);
            UUID orderId = UUID.fromString(node.get("orderId").asText());

            if (node.has("paymentId")) {
                // PaymentSucceeded
                UUID paymentId = UUID.fromString(node.get("paymentId").asText());
                log.info("Recebido PaymentSucceeded para o pedido {}", orderId);
                applyResult.onPaymentSucceeded(new PaymentSucceeded(orderId, paymentId));
            } else if (node.has("reason")) {
                // PaymentFailed
                String reason = node.get("reason").asText();
                log.info("Recebido PaymentFailed para o pedido {}: {}", orderId, reason);
                applyResult.onPaymentFailed(new PaymentFailed(orderId, reason));
            } else {
                log.warn("Evento de resultado sem campos discriminadores; descartando: {}", json);
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Erro ao processar resultado de pagamento; nack", ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
