package com.havingfunwithjava.catalog.application;

import com.havingfunwithjava.catalog.domain.CategoryId;

/**
 * Comando (input) para criar um produto.
 *
 * Objeto plano que chega da camada de interfaces (controller). Não é uma
 * entidade de domínio — apenas transporta os dados da intenção. O caso de uso
 * é quem valida e constrói a entidade de domínio.
 *
 * @param name        nome do produto (não-vazio)
 * @param description descrição (pode ser vazia)
 * @param amount      valor do preço como string (para preservar precisão)
 * @param currency    código ISO da moeda (ex.: "BRL")
 * @param categoryId  identificador da categoria
 */
public record CreateProductCommand(
        String name,
        String description,
        String amount,
        String currency,
        CategoryId categoryId
) {
}
