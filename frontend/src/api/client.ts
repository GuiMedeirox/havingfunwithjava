import axios from 'axios'

/**
 * Instância Axios canônica do front.
 *
 * O `baseURL` aponta para o api-gateway, configurável via env:
 *   VITE_API_URL (default: http://localhost:8080)
 *
 * O interceptor de requisição abaixo é um PLACEHOLDER para anexar o JWT:
 * quando existir um fluxo de login (issues futuras), o token será lido do
 * localStorage e enviado como `Authorization: Bearer <token>`. Por ora só
 * anexa o header se um token já existir em localStorage — funcional, mas
 * sem login real ainda.
 */
const TOKEN_STORAGE_KEY = 'hfwj.access_token'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = window.localStorage.getItem(TOKEN_STORAGE_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default apiClient
