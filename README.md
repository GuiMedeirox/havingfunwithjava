# havingfunwithjava

> Plataforma de e-commerce em microservices — projeto de portfólio para praticar e demonstrar uma stack Java completa: Spring Boot, Clean Architecture, mensageria, observabilidade, containerização e CI/CD.

## Visão geral

Projeto de portfólio construído para exercitar (e mostrar publicamente) a combinação de tecnologias exigidas em vagas back-end/full-stack modernas: Java + Spring Boot em microservices, mensageria com RabbitMQ, front-end React, observabilidade ponta-a-ponta, Docker/Kubernetes e pipeline de CI/CD.

O domínio escolhido é um e-commerce de escopo médio (catálogo, pedidos, pagamentos) — rico o suficiente para justificar microservices, eventos e consistência eventual, sem virar um monstro de orquestrar.

## Documentação

- 📄 [**PRD**](docs/PRD.md) — problema, solução, user stories, decisões de implementação e de teste.
- 🏛️ `docs/ARCHITECTURE.md` — _(a criar)_ desenho arquitetural detalhado.
- 📝 `docs/DECISIONS.md` — _(a criar)_ ADRs (Architecture Decision Records).

## Stack

| Camada           | Tecnologia                                            |
| ---------------- | ----------------------------------------------------- |
| Back-end         | Java, Spring Boot, Spring Cloud Gateway, Spring AMQP  |
| Arquitetura      | Clean Architecture, SOLID, Design Patterns            |
| Persistência     | PostgreSQL, Spring Data JPA, Flyway, HikariCP         |
| Mensageria       | RabbitMQ                                              |
| Front-end        | React, Vite, TypeScript, TanStack Query, TailwindCSS  |
| Observabilidade  | Micrometer, Prometheus, Grafana, OpenTelemetry        |
| Containerização  | Docker, Docker Compose, Kubernetes                    |
| CI/CD            | GitHub Actions                                        |
| Testes           | JUnit 5, Mockito, Testcontainers, JaCoCo              |

## Serviços planejados

| Serviço           | Responsabilidade                              | Porta |
| ----------------- | --------------------------------------------- | ----- |
| `api-gateway`     | Entry point único, JWT, rate limiting, routing | 8080  |
| `catalog-service` | Produtos, categorias, busca                    | 8081  |
| `orders-service`  | Ciclo de vida do pedido, emite `OrderCreated`  | 8082  |
| `payment-service` | Processa pagamento, consome eventos             | 8083  |

## Status

🚧 **Rascunho** — documentação inicial (PRD). Implementação por vir.

Veja o [PRD](docs/PRD.md) para o escopo completo e o plano de implementação sugerido.
