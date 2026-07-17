package com.havingfunwithjava.payment.domain;

/**
 * Porta de domínio: gateway de pagamento externo.
 *
 * <p>Abstrai a integração com um gateway real (Stripe, Pagar.me, etc.). A
 * implementação concreta (mock em dev/testes) mora em infrastructure. O domínio
 * só conhece esta interface — não sabe que há HTTP ou mock embaixo.
 */
public interface PaymentGateway {

    /**
     * Tenta autorizar um pagamento no gateway externo.
     *
     * @param payment  o pagamento a processar (já com método e valor definidos)
     * @return resultado da tentativa: AUTHORIZED, DECLINED, ou FALHA_TRANSIENTE
     */
    GatewayResult authorize(Payment payment);

    /**
     * Resultado da tentativa de autorização no gateway.
     *
     * @param status        AUTHORIZED (sucesso), DECLINED (recusa definitiva),
     *                      FALHA_TRANSIENTE (erro técnico, retentável)
     * @param reason        mensagem descritiva (para logs/auditoria)
     */
    record GatewayResult(GatewayStatus status, String reason) {
    }

    enum GatewayStatus {
        AUTHORIZED,
        DECLINED,
        FALHA_TRANSIENTE
    }
}
