package com.havingfunwithjava.payment.infrastructure.strategy;

import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentGateway;
import com.havingfunwithjava.payment.domain.PaymentMethod;
import com.havingfunwithjava.payment.domain.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy: pagamento por cartão de crédito.
 *
 * <p>Encapsula lógica específica de cartão (aqui apenas logging/validação básica;
 * em produção validaria bandeira, CVV, score antifraude) e delega a autorização
 * final ao {@link PaymentGateway}.
 */
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CreditCardPaymentStrategy.class);

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentGateway.GatewayResult process(Payment payment, PaymentGateway gateway) {
        log.info("Processando pagamento por cartão do pedido {}", payment.orderId());
        // Validações específicas de cartão entrariam aqui (bandeira, CVV, validade).
        return gateway.authorize(payment);
    }
}
