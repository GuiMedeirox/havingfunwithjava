import apiClient from './client'
import type { Category } from '../types'

/**
 * Lista categorias (GET /catalog/categories).
 * Endpoint público, retorna array de categorias.
 */
export async function fetchCategories(): Promise<Category[]> {
  const { data } = await apiClient.get<Category[]>('/catalog/categories')
  return data
}
