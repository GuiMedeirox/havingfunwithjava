# PRD — Plataforma de E-commerce (Microservices)

> **Status:** Rascunho · **Autor:** gu1lherm3 · **Última atualização:** 2026-07-16
> **Tipo:** Projeto de portfólio (demonstração técnica para candidaturas)
> Triage label sugerida: `ready-for-agent`

---

## Problem Statement

Como desenvolvedor back-end/full-stack buscando me posicionar para vagas que exigem uma stack moderna e completa (Java, Spring Boot, microservices, mensageria, observabilidade, Docker/K8s, CI/CD), eu não tenho hoje um projeto público que demonstre, de forma visível e integrada, essa combinação de competências. Cada conceito isolado eu já conheço ou posso aprender rápido, mas falta um **artefato único** que mostre a stacks conversando entre si — do domínio ao deploy — servindo tanto como prova técnica quanto como material de discussão em entrevistas.

Em suma: preciso transformar uma lista de tecnologias dispersas em um sistema coerente, executável e documentado, que conte uma história arquitetural clara para quem avalia.

## Solution

Uma plataforma de e-commerce de escopo médio (catálogo, pedidos, pagamentos) implementada como **arquitetura de microservices** em Java + Spring Boot, com front-end React, comunicação síncrona (REST via API Gateway) e assíncrona (RabbitMQ), observabilidade ponta-a-ponta (Prometheus/Grafana + tracing), containerização via Docker, orquestração via Kubernetes manifests e pipeline de CI/CD no GitHub Actions.

Do ponto de vista do usuário final (o cliente que navega no front-end), a solução se apresenta como uma loja simples: ele navega pelo catálogo, adiciona itens ao carrinho, finaliza um pedido e acompanha o status do pagamento. Toda a complexidade arquitetural fica atrás do API Gateway.

## User Stories

### Catálogo (`catalog-service`)

1. Como cliente, quero ver uma lista de produtos disponíveis, para que eu possa navegar pelo que a loja oferece.
2. Como cliente, quero ver os detalhes de um produto (descrição, preço, categoria, estoque), para que eu possa decidir sobre a compra.
3. Como cliente, quero filtrar produtos por categoria, para encontrar rapidamente o que procuro.
4. Como cliente, quero buscar produtos por nome, para localizar um item específico.
5. Como cliente, quero ver o preço formatado corretamente, para não ter dúvidas sobre o valor.
6. Como administrador, quero cadastrar um novo produto, para ampliar o catálogo da loja.
7. Como administrador, quero editar os dados de um produto, para manter o catálogo atualizado.
8. Como administrador, quero ajustar o preço de um produto, para refletir reajustes ou promoções.
9. Como administrador, quero inativar um produto, para que ele não apareça mais no catálogo sem perder o histórico.
10. Como administrador, quero cadastrar categorias e subcategorias, para organizar o catálogo.

### Carrinho & Checkout (front-end)

11. Como cliente, quero adicionar produtos a um carrinho, para agrupar itens antes de comprar.
12. Como cliente, quero remover itens do carrinho, para corrigir mudanças de ideia.
13. Como cliente, quero ajustar a quantidade de um item no carrinho, para comprar mais ou menos unidades.
14. Como cliente, quero ver o valor total do carrinho atualizado em tempo real, para saber quanto vou pagar.
15. Como cliente, quero que o carrinho persista entre sessões (localmente), para não perder itens ao fechar o navegador.
16. Como cliente, quero finalizar o pedido informando meus dados e endereço, para concluir a compra.

### Pedidos (`orders-service`)

17. Como sistema, ao finalizar o checkout quero criar um pedido em status `PENDING_PAYMENT`, para iniciar o fluxo de pagamento.
18. Como sistema, ao criar um pedido quero validar (via `catalog-service`) que os itens existem e os preços conferem, para evitar pedidos inconsistentes.
19. Como sistema, ao criar um pedido quero reservar/deduzir estoque, para evitar oversell.
20. Como sistema, quero decrementar definitivamente o estoque somente após pagamento confirmado, para manter consistência.
21. Como cliente, quero receber um identificador de pedido após o checkout, para acompanhar minha compra.
22. Como cliente, quero consultar meus pedidos anteriores, para acompanhar o histórico.
23. Como cliente, quero ver o status atual de um pedido (`PENDING_PAYMENT`, `PAID`, `PAYMENT_FAILED`, `CANCELLED`), para saber onde está minha compra.
24. Como sistema, quero receber o resultado do pagamento via evento e atualizar o status do pedido, para refletir a realidade transacional.
25. Como sistema, quero cancelar automaticamente pedidos cujo pagamento falhou após retentativas, para liberar estoque reservado.

### Pagamentos (`payment-service`)

26. Como sistema, quero consumir o evento `OrderCreated` e iniciar o processamento de pagamento, para destravar o pedido.
27. Como cliente, quero pagar com cartão de crédito, para usar minha forma de pagamento preferida.
28. Como cliente, quero pagar via Pix, para aproveitar uma opção rápida e sem taxa.
29. Como sistema, quero aplicar a estratégia de pagamento correta conforme o método escolhido, para processar cada um segundo suas regras.
30. Como sistema, quero simular a autorização junto a um gateway externo (mock), para demonstrar o fluxo sem custo.
31. Como sistema, ao confirmar o pagamento quero publicar `PaymentSucceeded`, para que o `orders-service` atualize o status.
32. Como sistema, ao falhar o pagamento quero publicar `PaymentFailed`, para que o pedido seja cancelado.
33. Como sistema, quero retentar pagamentos com falha transitória (exponential backoff), para maximizar conversão.
34. Como operador, quero ver o histórico de tentativas de pagamento por pedido, para investigar disputas.

### API Gateway (`api-gateway`)

35. Como cliente (front-end), quero um único ponto de entrada para todas as chamadas, para não precisar conhecer os serviços internos.
36. Como sistema, quero que o gateway valide tokens JWT nas rotas protegidas, para garantir autenticação centralizada.
37. Como sistema, quero rate limiting no gateway, para proteger os serviços de abuso.
38. Como sistema, quero roteamento configurável baseado em path prefix, para adicionar/remover serviços sem rebuild do front.
39. Como sistema, quero circuit breaking no gateway, para que uma falha em um serviço não derrube toda a plataforma.

### Observabilidade & Operação

40. Como operador, quero ver métricas de cada serviço (latência, throughput, erros) em dashboards Grafana, para monitorar a saúde da plataforma.
41. Como operador, quero trace IDs propagados entre serviços (HTTP e RabbitMQ), para rastrear uma requisição ponta-a-ponta.
42. Como operador, quero logs estruturados em JSON com correlation, para correlacionar eventos entre serviços.
43. Como operador, quero healthchecks expostos em `/actuator/health`, para que orquestradores (Docker/K8s) saibam quando reiniciar um serviço.
44. Como operador, quero alertas do Prometheus sobre erro-rate elevado, para reagir a incidentes.

### Qualidade, Deploy & DevEx

45. Como desenvolvedor, quero rodar toda a plataforma com um único `docker compose up`, para subir o ambiente de desenvolvimento rápido.
46. Como desenvolvedor, quero testes automatizados rodando no CI a cada push, para garantir que regressões sejam pegas cedo.
47. Como desenvolvedor, quero relatório de cobertura de testes (JaCoCo), para acompanhar a saúde da suíte.
48. Como operador, quero manifests Kubernetes por serviço, para fazer deploy da plataforma em um cluster.
49. Como operador, quero pipeline de CI/CD no GitHub Actions (build → test → imagem Docker → registry), para automatizar a entrega.
50. Como avaliador/entrevistador, quero um README claro com diagrama, decisões e como rodar, para entender o projeto em poucos minutos.

## Implementation Decisions

### Domínio e decomposição

- A plataforma é dividida em **4 serviços** cortados por capacidade de domínio: `api-gateway`, `catalog-service`, `orders-service`, `payment-service`.
- Decomposição orientada a domínio (domain-driven), com `customers`/`shipping` deliberadamente fora do escopo do MVP.
- Cada serviço possui seu próprio banco (database-per-service). Em dev, schemas isolados em uma instância compartilhada de PostgreSQL; em prod, instâncias separadas.

### Comunicação entre serviços

- **Síncrona (REST):** o front-end conversa apenas com o `api-gateway`; o `orders-service` chama o `catalog-service` durante o checkout para validar itens e preços.
- **Assíncrona (RabbitMQ):** o `orders-service` publica `OrderCreated` numa exchange topic (`orders.exchange`); o `payment-service` consome e devolve `PaymentSucceeded`/`PaymentFailed` em outra fila. Dead-letter queue configurada para mensagens não processáveis.
- Justificativa: o pagamento é um fluxo naturalmente assíncrono (autorização externa, retentativas), e o desacoplamento torna o sistema resiliente a indisponibilidades do gateway de pagamento.

### Arquitetura interna de cada serviço (Clean Architecture + SOLID)

- Cada serviço segue Clean Architecture em 4 camadas com dependências apontando para dentro:
  - `domain` — entidades e value objects puros (sem dependência de Spring), portas (interfaces de repositório e serviços).
  - `application` — casos de uso que orquestram o domínio.
  - `infrastructure` — adaptadores externos: persistência (JPA + Spring Data), mensageria (Spring AMQP), configuração Spring.
  - `interfaces` — controladores REST, DTOs e exception handlers.
- A regra de ouro é o Princípio da Inversão de Dependência: o `domain` declara interfaces (portas); o `infrastructure` as implementa. Isso torna o núcleo testável sem Spring e sem banco.
- Entidades JPA vivem em `infrastructure` (não em `domain`); a tradução entre modelo de domínio e modelo de persistência é feita por mappers, mantendo o domínio puro.

### Padrões de projeto deliberadamente empregados

- **Repository** — abstração de persistência definida em `domain`, implementada em `infrastructure`.
- **Strategy** — estratégias de pagamento (cartão, Pix) selecionáveis em runtime.
- **Factory** — construção de entidades de domínio garantindo invariantes.
- **Observer/Pub-Sub** — traduzido em RabbitMQ para integração entre serviços.
- **Builder** — construção de DTOs e fixtures de teste.

### Persistência

- **PostgreSQL** como banco de dados único para todos os serviços (decisão do autor do projeto).
- Spring Data JPA + Hibernate para ORM; HikariCP para pool de conexões.
- **Migrations versionadas via Flyway** (`db/migration` em cada serviço).
- **Nota sobre Oracle:** a vaga-alvo lista Oracle como requisito obrigatório e PostgreSQL como item separado. Este projeto opta por PostgreSQL exclusivamente por simplicidade de ambiente. Para realinhar à vaga, basta trocar o driver/flyway do `orders-service` para Oracle (XE em Testcontainers) e ajustar dialetos — a abstração do repositório torna essa troca localizada. O ADR de persistência documenta esta decisão.

### Front-end

- React + Vite + TypeScript (Vite por ser o padrão moderno; CRA está descontinuado).
- Gerenciamento de estado de servidor via TanStack Query (React Query) para cache e sincronização.
- React Router para roteamento; TailwindCSS para estilos.
- Todas as chamadas de rede vão ao `api-gateway` (nunca direto aos serviços internos), via Axios com interceptor para JWT.
- Escopo do front: catálogo (lista + detalhe + filtros), carrinho persistido em `localStorage`, checkout, tela de "meus pedidos".

### Autenticação

- Autenticação centralizada via JWT emitido por um endpoint de login (inicialmente no `api-gateway` ou em um `auth-service` mínimo). O gateway valida o token nas rotas protegidas.
- Para o MVP, um usuário admin e um usuário cliente são suficientes; sem recuperação de senha nem OAuth completo no escopo inicial.

### Observabilidade

- Micrometer + Spring Boot Actuator expõem métricas em cada serviço; Prometheus scrapeia e Grafana renderiza dashboards (um dashboard por serviço + um overview).
- Distributed tracing via Micrometer Tracing + OpenTelemetry; o trace ID propaga por HTTP headers e por propriedades da mensagem RabbitMQ.
- Logs em JSON estruturado (Logback) com MDC para correlation/trace ID. Em produção seguiriam para Loki ou ELK.

### Containerização e orquestração

- Cada serviço tem um `Dockerfile` multi-stage (build Maven → imagem JRE slim).
- Um `docker-compose.yml` sobe a stack completa localmente: serviços + PostgreSQL + RabbitMQ + Prometheus + Grafana.
- Manifests Kubernetes por serviço (`Deployment`, `Service`, `ConfigMap`, `Secret`, `HorizontalPodAutoscaler`).
- Pipeline de CI/CD no GitHub Actions: build → testes (JUnit/Testcontainers) → build de imagem → push para um registry (GHCR).

## Testing Decisions

### Seams de teste (2 seams, alta confiança)

A estratégia adota **dois seams** por serviço — o mínimo que mantém a confiança alta sem explodir a manutenção de dublês:

1. **Seam de API REST (slice vertical)** — `@SpringBootTest` + MockMvc + Testcontainers com instâncias reais de PostgreSQL e RabbitMQ. Exercita o caminho completo: controller → caso de uso → persistência (banco real) → mensageria (broker real). É o seam preferido: testa comportamento externo, não detalhes internos. Um pedido criado via API deve resultar em evento publicado e em mudança visível no banco.
2. **Seam de domínio puro** — JUnit 5 puro, sem contexto Spring. Testa regras de negócio (cálculo de total, transições de status do pedido, seleção de estratégia de pagamento) isoladamente, com feedback rápido.

### Princípios

- **Testar comportamento externo, não implementação.** Testes não devem quebrar ao refatorar internals, desde que o comportamento observável se mantenha.
- **Testcontainers sobre H2/Mock de banco.** Banco em memória não reproduz dialetos e constraints do PostgreSQL; Testcontainers dá realismo mantendo o isolamento do teste.
- **Sem mocks excessivos.** Usa-se dublê apenas quando a dependência é cara/lenta (gateway de pagamento externo, relógio). Regras de domínio são testadas com objetos reais.
- Prior art: o padrão de slice vertical com `@SpringBootTest` + Testcontainers é o que o ecossistema Spring recomenda hoje para microservices.

### Cobertura

- JaCoCo configurado em cada serviço; meta inicial de **80% nas camadas `domain` e `application`** (onde mora o risco de negócio). Sem meta rígida para `infrastructure`/`interfaces`.

## Out of Scope

- **Cadastro completo de clientes** (CRM, recuperação de senha, OAuth/SSO, 2FA). Autenticação fica no mínimo necessário (login + JWT).
- **Serviço de logística/envio** (`shipping-service`).
- **Serviço de avaliações/comentários** de produtos.
- **Integração real com gateway de pagamento** (Stripe/Pagar.me) — usa-se um mock determinístico.
- **Search engine dedicado** (Elasticsearch/Meilisearch) — a busca no catálogo é via SQL.
- **CDN e infraestrutura de produção real** (WAF, multi-região, autoscaling real em provedor).
- **App mobile nativo.**
- **Internationalization/i18n.**
- **Administração via back-office sofisticado** — CRUDs admin ficam no mínimo funcional.
- Migração para Oracle (decisão explícita — ver Implementation Decisions > Persistência).

## Further Notes

### Justificativa de escopo para portfólio

Este projeto foi dimensionado como **escopo médio (4 serviços)** propositalmente: é suficiente para demonstrar comunicação síncrona e assíncrona, API Gateway, database-per-service, Clean Architecture, observabilidade ponta-a-ponta e deploy via K8s/CI — sem a complexidade de orquestrar 6+ serviços, que prejudicaria a completude do entregável.

### Artefatos que devem acompanhar o código

- `README.md` na raiz com diagrama ASCII da topologia, como rodar, e link para os ADRs.
- `docs/ARCHITECTURE.md` com o desenho detalhado e justificativas.
- `docs/DECISIONS.md` (ADRs curtos) cobrindo: nº de serviços, escolha do RabbitMQ, escolha do PostgreSQL, Clean Architecture, estratégia de testes.
- Cada serviço deve expor `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.

### Sequência sugerida de implementação (não parte deste PRD, apenas referência)

Os serviços podem ser construídos incrementalmente em ordem: (1) `catalog-service` (autônomo, mais simples), (2) `api-gateway` + front consumindo catálogo, (3) `orders-service` com integração ao catálogo, (4) `payment-service` com RabbitMQ, (5) fechamento do fluxo pedido→pagamento→atualização de status, (6) observabilidade e CI/CD. Cada fase é candidata a um PR/issue separado.
