import apiClient from './client'

/**
 * Shape da resposta de /catalog/health do api-gateway.
 * O gateway faz StripPrefix=1 e repassa /health do catalog-service,
 * que retorna algo como:
 *   { "status": "UP", "service": "catalog-service", "at": "2024-..." }
 *
 * Caso o endpoint mude ou use /actuator/health, os campos opcionais
 * mantêm o tipo permissivo o bastante para ambos.
 */
export interface HealthResponse {
  status: string
  service?: string
  at?: string
}

/**
 * Faz GET /catalog/health via api-gateway.
 * Se você preferir o actuator, troque para '/actuator/health'.
 */
export async function fetchCatalogHealth(): Promise<HealthResponse> {
  const { data } = await apiClient.get<HealthResponse>('/catalog/health')
  return data
}
