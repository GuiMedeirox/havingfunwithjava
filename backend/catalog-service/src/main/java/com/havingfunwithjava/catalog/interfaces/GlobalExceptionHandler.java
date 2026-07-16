package com.havingfunwithjava.catalog.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduz exceções (de validação e de domínio) em respostas HTTP padronizadas
 * usando ProblemDetail (RFC 7807). Garante que o front sempre receba um corpo
 * de erro consistente, com status, título, detalhe e timestamp.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Erros de Bean Validation (ex.: @NotBlank no request) → 400 Bad Request.
     * Inclui um mapa com os campos inválidos e suas mensagens.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "inválido" : fe.getDefaultMessage(),
                        (a, b) -> a));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Payload inválido");
        problem.setTitle("Validation error");
        problem.setProperty("fields", fieldErrors);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Invariantes de domínio (ex.: preço <= 0 no construtor de Product) → 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleDomainInvariant(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Domain error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Erro genérico não esperado → 500, sem vazar stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
        problem.setTitle("Internal error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
