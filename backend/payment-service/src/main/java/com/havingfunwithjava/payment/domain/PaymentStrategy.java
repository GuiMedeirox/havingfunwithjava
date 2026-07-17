package com.havingfunwithjava.payment.domain;

/**
 * Porta de domínio: estratégia de processamento de pagamento por método.
 *
 * <p>Padrão Strategy (issue #19): cada método (cartão, Pix) tem sua implementação.
 * A seleção em runtime é feita pelo caso de uso, que conhece o {@link PaymentMethod}
 * e escolhe a strategy correspondente.
 *
 * <p>A strategy encapsula detalhes específicos do método (ex.: cartão valida CVV
 * e bandeira, Pix gera QR code) e delega a autorização final ao {@link PaymentGateway}.
 */
public interface PaymentStrategy {

    /**
     * @return o método que esta strategy atende.
     */
    PaymentMethod supports();

    /**
     * Processa o pagamento usando o gateway informado.
     *
     * @param payment  pagamento a processar (método já compatível)
     * @param gateway  gateway de pagamento (injetado pelo caso de uso)
     * @return resultado da tentativa no gateway
     */
    PaymentGateway.GatewayResult process(Payment payment, PaymentGateway gateway);
}
