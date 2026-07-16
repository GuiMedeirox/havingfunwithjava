# havingfunwithjava

> Plataforma de e-commerce em microservices — projeto de portfólio para praticar e demonstrar uma stack Java completa: Spring Boot, Clean Architecture, mensageria, observabilidade, containerização e CI/CD.

## Visão geral

Projeto de portfólio construído para exercitar (e mostrar publicamente) a combinação de tecnologias exigidas em vagas back-end/full-stack modernas: Java + Spring Boot em microservices, mensageria com RabbitMQ, front-end React, observabilidade ponta-a-ponta, Docker/Kubernetes e pipeline de CI/CD.

O domínio escolhido é um e-commerce de escopo médio (catálogo, pedidos, pagamentos) — rico o suficiente para justificar microservices, eventos e consistência eventual, sem virar um monstro de orquestrar.

## Documentação

- 📄 [**PRD**](docs/PRD.md) — problema, solução, user stories, decisões de implementação e de teste.
- 📋 [**Issues**](https://github.com/GuiMedeirox/havingfunwithjava/issues) — 26 slices verticais com critérios de aceite, prontos para implementação.
- 🏛️ `docs/ARCHITECTURE.md` — _(a criar)_ desenho arquitetural detalhado.
- 📝 `docs/DECISIONS.md` — _(a criar)_ ADRs (Architecture Decision Records).

## Stack

| Camada           | Tecnologia                                            |
| ---------------- | ----------------------------------------------------- |
| Back-end         | Java 21, Spring Boot 3.5.x, Spring Cloud Gateway      |
| Arquitetura      | Clean Architecture, SOLID, Design Patterns            |
| Persistência     | PostgreSQL 16, Spring Data JPA, Flyway, HikariCP      |
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

🚧 **Fundação do monorepo** (issue [#1](https://github.com/GuiMedeirox/havingfunwithjava/issues/1)) — backend parent POM, Maven Wrapper, PostgreSQL local e pipeline de CI. Implementação dos serviços por vir (a partir da issue [#2](https://github.com/GuiMedeirox/havingfunwithjava/issues/2)).

---

## Desenvolvimento

### Pré-requisitos

| Ferramenta   | Versão   | Nota                                                        |
| ------------ | -------- | ----------------------------------------------------------- |
| **JDK**      | 21 (LTS) | Spring Boot 4.x exige Java 17+; usamos 21.                  |
| **Docker**   | 24+      | Para o PostgreSQL local e, depois, a stack completa.        |
| **Node.js**  | 20+      | Apenas para o front-end (quando o `frontend/` existir).     |

O **Maven Wrapper** (`./mvnw`) já está incluído em `backend/` — não é necessário instalar o Maven globalmente.

### Subir o PostgreSQL local

Da raiz do repo:

```bash
docker compose up -d postgres          # sobe o banco
docker compose logs -f postgres        # acompanha os logs
docker compose down                    # para o container (mantém o volume)
docker compose down -v                 # para e APAGA os dados (cuidado)
```

O banco sobe acessível em `localhost:5432`, com usuário/senha/banco `havingfunwithjava`.

### Build do backend

```bash
cd backend
./mvnw -B verify                       # Windows: .\mvnw.cmd -B verify
```

> Por ora o backend só tem o parent POM (sem módulos de serviço), então o build é um no-op que valida apenas a integridade do POM. Assim que o `catalog-service` entrar (issue #2), os testes de cada serviço rodam neste mesmo comando.

### CI

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda `./mvnw -B verify` em push/PR para a `main`, com Java 21 (Temurin) e cache do Maven.

## Roadmap

O roadmap completo (26 issues) está no [issue tracker](https://github.com/GuiMedeirox/havingfunwithjava/issues). O caminho crítico para o fluxo e2e é:

```
#1 → #2 → #3 → #15 → #18 → #19 → #21 → #22 → #23
base   catalog orders Rabbit payment result consume front
```
