package com.havingfunwithjava.gateway.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Emissão e validação de JWTs HS256.
 *
 * <p>Padrão de uso (issue #10):
 * <ul>
 *   <li>{@link #generateToken(String, String)} — chamado pelo {@link AuthController}
 *       após credenciais válidas. subject = username, claim {@code role} = admin/client.</li>
 *   <li>{@link #parseAndVerify(String)} — chamado pelo {@link AuthenticationFilter}
 *       para validar assinatura + expiração de tokens em rotas protegidas.</li>
 * </ul>
 *
     * <p>Usa a API fluente do jjwt 0.12.x: {@code Jwts.builder()...signWith(key).compact()}
 * para emitir e {@code Jwts.parser().verifyWith(key).build().parseSignedClaims(token)}
 * para validar. HMAC-SHA256 requer uma chave de pelo menos 256 bits (32 bytes); o
 * dev-secret do {@link JwtProperties} é propositalmente longo para satisfazer isso.
 *
 * <p>Na validação capturamos apenas {@link JwtException} (superclasse), que já cobre
 * expiração ({@code ExpiredJwtException}), assinatura inválida e token malformado.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expiration = properties.expiration();
    }

    /**
     * Emite um JWT assinado para o usuário + role informados.
     *
     * @param username vira o {@code subject} do token
     * @param role     claim customizado {@code role} (admin/client)
     * @return token compactado (String)
     */
    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plus(expiration);
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Valida assinatura + expiração e devolve os claims, ou {@code empty()} se
     * o token for inválido (assinatura ruim, malformado, expirado).
     *
     * <p>O filtro de proteção chama isto; devolver Optional (em vez de lançar)
     * simplifica o fluxo reativo e mantém o 401 como única consequência visível.
     */
    public Optional<Claims> parseAndVerify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
