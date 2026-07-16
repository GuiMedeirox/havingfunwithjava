package com.havingfunwithjava.gateway.auth;

/**
 * Resposta 200 do {@code POST /auth/login}.
 *
 * <p>Record imutável serializado para JSON pelo Jackson do WebFlux. O cliente usa
 * o {@code token} no header {@code Authorization: Bearer <token>} nas rotas
 * protegidas (POST/PUT/DELETE /catalog/**).
 */
public record LoginResponse(

        String token,
        String username,
        String role

) {
}
