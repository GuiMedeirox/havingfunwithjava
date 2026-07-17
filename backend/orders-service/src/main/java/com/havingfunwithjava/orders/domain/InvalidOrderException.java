package com.havingfunwithjava.orders.domain;

/**
 * Exceção de domínio: pedido inválido (item inexistente, preço divergente, etc.).
 *
 * <p>Lançada pelo {@code CreateOrderUseCase} quando a validação contra o catalog
 * falha. Traduzida para HTTP 422 Unprocessable Entity pela camada de interfaces.
 */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
