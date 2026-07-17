package com.havingfunwithjava.payment.domain;

import java.util.List;
import java.util.UUID;

/**
 * Evento de entrada: OrderCreated (publicado pelo orders-service, consumido aqui).
 *
 * <p>Este DTO espelha o schema JSON publicado pelo orders-service ({@code OrderCreatedEvent}
 * de lá). Mantemos uma cópia local para desacoplar os serviços (cada serviço conhece
 * apenas o contrato de integração, não as entidades internas do outro).
 *
 * <p>O método de pagamento ({@code method}) é escolhido pelo consumer neste slice
 * (default CREDIT_CARD); em produção, viria no evento ou seria selecionado pelo
 * cliente no checkout.
 *
 * @param orderId    id do pedido criado
 * @param customerId id do cliente
 * @param amount     valor total (string p/ preservar precisão)
 * @param currency   moeda ISO
 * @param items      itens do pedido
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        String amount,
        String currency,
        List<Item> items
) {

    public record Item(
            UUID productId,
            String productName,
            int quantity,
            String unitPrice,
            String currency
    ) {
    }
}
