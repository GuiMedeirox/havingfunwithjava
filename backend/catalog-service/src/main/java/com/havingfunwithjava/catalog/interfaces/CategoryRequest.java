package com.havingfunwithjava.catalog.interfaces;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * DTO de request para criar/atualizar categoria.
 *
 * @param name     nome (não-vazio)
 * @param slug     slug único (não-vazio, formato lowercase-hyphenated)
 * @param parentId UUID da categoria pai (opcional — null = raiz)
 */
public record CategoryRequest(
        @NotBlank(message = "nome é obrigatório")
        String name,

        @NotBlank(message = "slug é obrigatório")
        String slug,

        UUID parentId
) {
}
