package com.havingfunwithjava.catalog.interfaces;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de request para criar um produto (POST /products).
 *
 * Validação declarativa via Bean Validation (jakarta.validation): a camada de
 * interfaces rejeita payloads inválidos antes mesmo de chegar ao caso de uso.
 * O caso de uso também valida invariantes de domínio — defesa em profundidade.
 *
 * @param name        nome (não-vazio)
 * @param description descrição (opcional)
 * @param amount      preço como string para preservar precisão (ex.: "19.99")
 * @param currency    código ISO da moeda (ex.: "BRL")
 * @param categoryId  UUID da categoria
 */
public record ProductRequest(
        @NotBlank(message = "nome é obrigatório")
        String name,

        String description,

        @NotBlank(message = "amount é obrigatório")
        @DecimalMin(value = "0.0001", message = "preço deve ser maior que zero")
        String amount,

        @NotBlank(message = "currency é obrigatório")
        String currency,

        @NotNull(message = "categoryId é obrigatório")
        UUID categoryId
) {
}
