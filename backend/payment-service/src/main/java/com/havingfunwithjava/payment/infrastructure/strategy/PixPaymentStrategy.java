package com.havingfunwithjava.payment.infrastructure.strategy;

import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentGateway;
import com.havingfunwithjava.payment.domain.PaymentMethod;
import com.havingfunwithjava.payment.domain.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy: pagamento via Pix.
 *
 * <p>Encapsula lógica específica de Pix (aqui apenas logging; em produção geraria
 * QR code, validaria chave, conciliação) e delega a autorização ao gateway.
 * Pix tende a ser mais rápido e sem taxa — capturamos isso só semanticamente.
 */
@Component
public class PixPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(PixPaymentStrategy.class);

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.PIX;
    }

    @Override
    public PaymentGateway.GatewayResult process(Payment payment, PaymentGateway gateway) {
        log.info("Processando pagamento via Pix do pedido {}", payment.orderId());
        // Geração de QR code / validação de chave entrariam aqui.
        return gateway.authorize(payment);
    }
}
