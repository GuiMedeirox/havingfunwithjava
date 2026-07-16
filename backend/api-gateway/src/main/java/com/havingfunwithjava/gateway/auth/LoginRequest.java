package com.havingfunwithjava.gateway.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo do {@code POST /auth/login}.
 *
 * <p>Record imutável (Java 21). A validação {@code @NotBlank} é acionada quando o
 * controller anota o parâmetro com {@code @Valid}; falha vira 400 automático do
 * WebFlux (sem chegar ao fluxo de credenciais).
 */
public record LoginRequest(

        @NotBlank(message = "username é obrigatório")
        String username,

        @NotBlank(message = "password é obrigatório")
        String password

) {
}
