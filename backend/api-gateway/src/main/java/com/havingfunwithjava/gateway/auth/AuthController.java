package com.havingfunwithjava.gateway.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

/**
 * Endpoint público de autenticação ({@code POST /auth/login}).
 *
 * <p>Implementado como controller reativo (retorna {@link Mono}) porque o gateway
 * roda em WebFlux/Netty; um controller bloqueante atrapalharia o event loop.
 *
 * <p>Credenciais são mock em memória (Map) — suficiente para o padrão de portfólio
 * da issue #10. Em produção isto viria de um users-service / banco.
 *
 * <p>Fluxo:
 * <ol>
 *   <li>Recebe {@link LoginRequest} validado.</li>
 *   <li>Procura username no map de mocks; confere a senha.</li>
 *   <li>Se OK → 200 + {@link LoginResponse} com JWT assinado (role no claim).</li>
 *   <li>Se credenciais inválidas → 401 (sem corpo discriminativo, por segurança).</li>
 * </ol>
 *
 * <p>Rota pública: o {@link AuthenticationFilter} não exige token aqui.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /** Mocks: username -> (password, role). Apenas admin/client, conforme a issue. */
    private static final Map<String, String[]> CREDENTIALS = Map.of(
            "admin", new String[]{"admin", "admin"},
            "cliente", new String[]{"cliente", "client"}
    );

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody Mono<LoginRequest> body) {
        return body.handle((request, sink) -> {
            Optional<String[]> matched = Optional.ofNullable(CREDENTIALS.get(request.username()))
                    .filter(creds -> creds[0].equals(request.password()));
            if (matched.isEmpty()) {
                sink.next(ResponseEntity.status(401).build());
                return;
            }
            String role = matched.get()[1];
            String token = jwtService.generateToken(request.username(), role);
            sink.next(ResponseEntity.ok(new LoginResponse(token, request.username(), role)));
        });
    }
}
