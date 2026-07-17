# Multi-stage: builda o SPA com Node/Vite e serve estático com nginx.
#
# Build:
#   docker build -t hfwj-frontend ./frontend
# Run:
#   docker run --rm -p 8081:80 -e NGINX_GATEWAY=http://host.docker.internal:8080 hfwj-frontend
#
# Nota: o front fala com o api-gateway pela VITE_API_URL embutida no build.
# Para apontar para outro gateway sem rebuild, use a substitution do nginx
# (ver comentário abaixo) ou rebuild a imagem com --build-arg.

# ─── Stage 1: build ──────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Instala deps primeiro (cache melhor com camada separada).
COPY package.json package-lock.json* ./
RUN npm install

# Copia o fonte e builda.
COPY . .
# VITE_API_URL default já é http://localhost:8080 no client.ts;
# passe --build-arg VITE_API_URL=... para sobrescrever no build.
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# ─── Stage 2: serve ──────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Remove config default e instala a nossa (SPA fallback).
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia o build estático para o document root do nginx.
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

# healthcheck simples: o nginx responde 200 no index.
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
