/**
 * Tipos das entidades expostas pelo catalog-service via api-gateway.
 *
 * O backend é Java/Spring; aqui definimos interfaces TS que casam com o
 * JSON retornado. Não há compartilhamento de tipos com o backend — mantemos
 * este arquivo como fonte canônica do ponto de vista do front.
 */

/** Produto. `amount` vem como string do backend (ex.: "4500.00"). */
export interface Product {
  id: string
  name: string
  description?: string
  amount: string
  currency: string
  categoryId?: string
  active: boolean
}

/** Categoria. */
export interface Category {
  id: string
  name: string
  slug: string
  parentId?: string | null
}

/**
 * Resposta paginada do catalog-service.
 *
 * O backend devolve este objeto SEMPRE que a requisição inclui algum
 * parâmetro de paginação/filtro (page, size, category, q). Sem parâmetros
 * ele devolve um array puro — por isso o front SEMPRE envia page/size para
 * obter este shape de forma consistente.
 */
export interface PagedProducts {
  items: Product[]
  totalItems: number
  totalPages: number
  page: number
  size: number
}
