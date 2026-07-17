package com.havingfunwithjava.catalog.domain;

/**
 * Exceção de domínio: slug de categoria duplicado.
 *
 * O slug é único por categoria. Quando o caso de uso detecta tentativa de criar
 * ou atualizar para um slug já existente, lança esta exceção. A camada de
 * interfaces a traduz para HTTP 409 Conflict.
 */
public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String slug) {
        super("Slug já existe: " + slug);
    }
}
