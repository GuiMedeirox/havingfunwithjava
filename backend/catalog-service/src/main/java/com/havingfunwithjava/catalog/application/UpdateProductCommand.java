package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.CategoryId;

/**
 * Comando (input) para atualizar um produto.
 *
 * Todos os campos são opcionais (null = manter valor existente), o que atende
 * tanto PUT (todos preenchidos) quanto PATCH (apenas os alterados). A validação
 * de invariantes (preço > 0, nome não-vazio) acontece no construtor de Product
 * quando o caso de uso reconstrói a entidade.
 *
 * @param name        novo nome (null = manter)
 * @param description nova descrição (null = manter)
 * @param amount      novo valor do preço como string (null = manter)
 * @param currency    nova moeda (null = manter; amount e currency vão juntos)
 * @param categoryId  nova categoria (null = manter)
 */
public record UpdateProductCommand(
        String name,
        String description,
        String amount,
        String currency,
        CategoryId categoryId
) {
}
