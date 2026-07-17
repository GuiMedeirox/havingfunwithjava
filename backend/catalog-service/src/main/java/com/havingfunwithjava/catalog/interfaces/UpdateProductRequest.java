package com.havingfunwithjava.catalog.interfaces;

import java.util.UUID;

/**
 * DTO de request para atualizar um produto (PUT/PATCH).
 *
 * Todos os campos são opcionais para suportar PATCH parcial. Em PUT, o cliente
 * envia todos. A validação de invariantes (preço > 0, nome não-vazio) acontece
 * no caso de uso ao reconstruir a entidade de domínio — não no DTO, porque em
 * PATCH campos ausentes não devem falhar validação.
 *
 * @param name        novo nome (null = manter em PATCH)
 * @param description nova descrição
 * @param amount      novo preço (string p/ precisão)
 * @param currency    nova moeda
 * @param categoryId  nova categoria
 */
public record UpdateProductRequest(
        String name,
        String description,
        String amount,
        String currency,
        UUID categoryId
) {
}
