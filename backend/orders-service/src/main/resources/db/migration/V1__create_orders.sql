-- Tabela de pedidos (orders-service).
-- O total NÃO é persistido como coluna: sempre derivado dos itens (soma de subtotais).
-- status é serializado como string (enum name); convenção UPPER_SNAKE_CASE.
CREATE TABLE orders (
    id              UUID            NOT NULL,
    customer_id     UUID            NOT NULL,
    currency        CHAR(3)         NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    created_at      TIMESTAMP       NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

-- Índice para a consulta de pedidos por cliente (orderby created_at desc).
CREATE INDEX idx_orders_customer ON orders (customer_id, created_at DESC);

-- Itens de pedido. Id próprio IDENTITY ( surrogate) porque a chave natural
-- (order_id, product_id) complicaria o cascade; o snapshot (product_name,
-- unit_price, currency) é travado no momento da compra.
CREATE TABLE order_items (
    id              BIGSERIAL       NOT NULL,
    order_id        UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    product_name    VARCHAR(255)    NOT NULL,
    quantity        INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(19, 4)  NOT NULL CHECK (unit_price > 0),
    currency        CHAR(3)         NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
