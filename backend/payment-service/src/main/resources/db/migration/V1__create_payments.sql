-- Tabela de pagamentos (payment-service).
-- Um pagamento liquida um pedido (orderId é unique neste fluxo: 1 pedido → 1 pagamento).
-- O status é serializado como string (enum name). attempts rastreia retentativas (issue #20).
CREATE TABLE payments (
    id              UUID            NOT NULL,
    order_id        UUID            NOT NULL,
    method          VARCHAR(32)     NOT NULL,
    amount          NUMERIC(19, 4)  NOT NULL CHECK (amount > 0),
    currency        CHAR(3)         NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    attempts        INTEGER         NOT NULL DEFAULT 1 CHECK (attempts >= 0),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uq_payments_order UNIQUE (order_id)
);

-- Índice para a consulta de pagamento por pedido.
CREATE INDEX idx_payments_order ON payments (order_id);
