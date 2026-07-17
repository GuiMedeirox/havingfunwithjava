# Frontend — Having Fun With Java

Scaffold do front-end do monorepo: **React + Vite + TypeScript + TailwindCSS + TanStack Query (React Query) + React Router + Axios**.

Este pacote estabelece o molde que as features seguintes (catálogo, carrinho, checkout) vão popular. A página inicial (`/`) é um placeholder que prova que o stack está vivo: faz um `GET /catalog/health` no api-gateway via React Query + Axios e exibe o status.

## Pré-requisitos

- Node.js 18+ (recomendado 20)
- npm

## Como rodar (desenvolvimento)

```bash
# 1. Instalar dependências
npm install

# 2. Configurar a URL do api-gateway (opcional — há default)
cp .env.example .env
# Edite .env se o gateway estiver em outro endereço.

# 3. Subir o dev server
npm run dev
```

O app sobe em `http://localhost:5173`.

> O `vite.config.ts` já configura um proxy para `/catalog` e `/actuator` apontando para `http://localhost:8080`, o que evita CORS em dev local — mas o `baseURL` do Axios segue a env var `VITE_API_URL` (default `http://localhost:8080`).

## Variável de ambiente

| Variável        | Default                 | Descrição                                      |
| --------------- | ----------------------- | ---------------------------------------------- |
| `VITE_API_URL`  | `http://localhost:8080` | URL base do api-gateway (usada pelo Axios)     |

Copie `.env.example` para `.env` e ajuste conforme necessário. **Não commite `.env`.**

## Scripts

| Script            | Descrição                                            |
| ----------------- | ---------------------------------------------------- |
| `npm run dev`     | Sobe o dev server do Vite (HMR)                      |
| `npm run build`   | Type-check (`tsc -b`) + build de produção (`vite build`) |
| `npm run preview` | Servir localmente o build de produção                |
| `npm run typecheck` | Apenas checagem de tipos                           |

## Build de produção

```bash
npm run build
```

Gera o diretório `dist/` com os arquivos estáticos otimizados.

## Docker

O `Dockerfile` é multi-stage: builda com Node/Vite e serve com nginx (com SPA fallback).

```bash
# Build da imagem (a partir da raiz do repo)
docker build -t hfwj-frontend ./frontend

# Run expondo na porta 8081
docker run --rm -p 8081:80 hfwj-frontend
# App disponível em http://localhost:8081
```

Para apontar o build para outro gateway, use build-arg:

```bash
docker build --build-arg VITE_API_URL=https://api.exemplo.com -t hfwj-frontend ./frontend
```

## Estrutura

```
frontend/
├── src/
│   ├── api/
│   │   ├── client.ts      # instância Axios + interceptor JWT (placeholder)
│   │   └── health.ts      # chamada GET /catalog/health
│   ├── pages/
│   │   └── HealthPage.tsx # página placeholder (usa React Query + Tailwind)
│   ├── App.tsx            # rotas (react-router)
│   ├── main.tsx           # entry: QueryClientProvider + BrowserRouter
│   ├── index.css          # diretivas Tailwind
│   └── vite-env.d.ts      # tipos de import.meta.env
├── Dockerfile             # build multi-stage (node -> nginx)
├── nginx.conf             # SPA fallback
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── postcss.config.js
└── .env.example
```

## Endpoint usado

- `GET /catalog/health` (via api-gateway em `:8080`) → repassa para o `/health` do catalog-service. Retorna JSON:
  ```json
  { "status": "UP", "service": "catalog-service", "at": "2024-..." }
  ```

## Notas

- O interceptor do Axios lê um token JWT do `localStorage` (chave `hfwj.access_token`) e anexa `Authorization: Bearer <token>` se existir. É um **placeholder** — ainda não há fluxo de login (issues futuras).
- Stack: React 18, Vite 5, TypeScript 5, Tailwind 3, React Query 5, React Router 6, Axios 1.
