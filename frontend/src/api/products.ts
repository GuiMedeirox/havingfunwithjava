import apiClient from './client'
import type { Product, PagedProducts } from '../types'

/** Filtros/paginação aceitos por fetchProducts. */
export interface ProductQuery {
  /** ID da categoria, ou vazio/undefined para todas. */
  category?: string
  /** Termo de busca por nome. */
  q?: string
  /** Página (base 0). */
  page?: number
  /** Tamanho da página. */
  size?: number
}

/**
 * Lista produtos paginados via api-gateway (GET /catalog/products).
 *
 * SEMPRE envia page/size para garantir o shape paginado (PagedProducts).
 * Sem esses parâmetros o backend devolve um array puro, que evitamos aqui
 * para manter o tipo consistente.
 *
 * Params:
 *  - category: filtra por categoria (UUID)
 *  - q: busca por nome
 *  - page/size: paginação (default 0 / 12)
 */
export async function fetchProducts(
  query: ProductQuery = {},
): Promise<PagedProducts> {
  const { category, q, page = 0, size = 12 } = query
  const { data } = await apiClient.get<PagedProducts>('/catalog/products', {
    params: {
      page,
      size,
      ...(category ? { category } : {}),
      ...(q ? { q } : {}),
    },
  })
  return data
}

/**
 * Busca um produto por id (GET /catalog/products/{id}).
 * Retorna 404 ProblemDetail se não existir (tratado pelas pages via React Query).
 */
export async function fetchProductById(id: string): Promise<Product> {
  const { data } = await apiClient.get<Product>(`/catalog/products/${id}`)
  return data
}
