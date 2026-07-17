package com.havingfunwithjava.orders.interfaces;

import com.havingfunwithjava.orders.domain.InvalidOrderException;
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
 * Traduz exceções em respostas HTTP padronizadas (ProblemDetail / RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation → 400.
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
     * Pedido inválido (item inexistente, preço divergente) → 422 Unprocessable Entity.
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ProblemDetail handleInvalidOrder(InvalidOrderException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Invalid order");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Invariantes de domínio (ex.: quantidade <= 0) → 400.
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
     * Erro genérico → 500, sem vazar stack trace.
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
