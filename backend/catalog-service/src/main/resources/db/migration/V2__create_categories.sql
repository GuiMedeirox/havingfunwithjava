-- Tabela de categorias de produtos.
-- O id é UUID gerado pelo domínio. Slug é único (constraint).
-- parent_id opcional permite subcategorias (auto-relacionamento).
CREATE TABLE categories (
    id          UUID            NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    slug        VARCHAR(255)    NOT NULL,
    parent_id   UUID,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories (id)
);

-- NOTA sobre a FK products.category_id → categories.id:
-- A coluna category_id já existe em products (criada em V1). A FK NÃO é
-- adicionada neste slice porque os testes atuais criam produtos com
-- categoryIds aleatórios (sem categoria correspondente), e a validação
-- de categoria no momento de criar produto será tratada no domínio em uma
-- issue futura. Adicionar a FK agora quebraria a suíte de testes existente.
-- Quando a validação for implementada no fluxo de criação de produto,
-- esta migration poderá adicionar a FK com confiança.
CREATE INDEX idx_categories_slug ON categories (slug);
