package com.havingfunwithjava.payment.infrastructure.gateway;

import com.havingfunwithjava.payment.domain.Payment;
import com.havingfunwithjava.payment.domain.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adaptador: implementa {@link PaymentGateway} com lógica MOCK determinística.
 *
 * <p>Substitui um gateway real (Stripe, Pagar.me) em dev e testes. Regras:
 * <ul>
 *   <li>Valor > 10000 → DECLINED (limite do mock).</li>
 *   <li>Valor ímpar na última casa decimal (ex.: 10.01, 50.03) → simula falha
 *       transitória (erro técnico retentável).</li>
 *   <li>Demais casos → AUTHORIZED (sucesso).</li>
 * </ul>
 *
 * <p>Em produção, esta classe seria trocada por uma implementação real que chama
 * a API do gateway via HTTP. O domínio não muda — só este adaptador.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);
    private static final BigDecimal MAX_AUTHORIZED_AMOUNT = new BigDecimal("10000");

    @Override
    public GatewayResult authorize(Payment payment) {
        BigDecimal amount = payment.amount().amount();
        log.info("Mock gateway: autorizando {} {} para o pedido {} (método {})",
                amount, payment.amount().currency(), payment.orderId(), payment.method());

        // Limite do mock: valores muito altos são recusados
        if (amount.compareTo(MAX_AUTHORIZED_AMOUNT) > 0) {
            return new GatewayResult(GatewayStatus.DECLINED,
                    "Valor excede o limite autorizado (mock)");
        }

        // Simula falha transitória: última casa decimal ímpar
        int lastDecimal = amount.remainder(BigDecimal.ONE)
                .multiply(new BigDecimal("100"))
                .intValue();
        if (lastDecimal % 2 == 1) {
            return new GatewayResult(GatewayStatus.FALHA_TRANSIENTE,
                    "Timeout simulado no gateway (mock)");
        }

        // Sucesso
        return new GatewayResult(GatewayStatus.AUTHORIZED, "Autorizado (mock)");
    }
}
