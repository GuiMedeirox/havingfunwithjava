-- Tabela de produtos do catálogo.
-- O id é UUID gerado pelo domínio (não delegamos ao banco).
-- Preço decomposto em amount (BigDecimal, precisão 19/4) + currency (ISO 4217, 3 chars).
-- Soft-delete via coluna 'active' (issue #8 inativará produtos sem apagar o histórico).
CREATE TABLE products (
    id              UUID            NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    price_amount    NUMERIC(19, 4)  NOT NULL,
    price_currency  CHAR(3)         NOT NULL,
    category_id     UUID            NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

-- Facilita a consulta de produtos ativos (GET /products público filtra por active=true).
CREATE INDEX idx_products_active ON products (active) WHERE active = TRUE;
