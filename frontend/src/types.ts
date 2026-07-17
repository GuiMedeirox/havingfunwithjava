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

// ---------------------------------------------------------------
// Pedidos (orders-service via api-gateway)
// ---------------------------------------------------------------

/** Item de um pedido (snapshot do produto no momento da compra). */
export interface OrderItem {
  productId: string
  productName: string
  quantity: number
  unitPrice: string
  subtotal: string
  currency: string
}

/** Estado de um pedido no ciclo de pagamento. */
export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'PAYMENT_FAILED'
  | 'CANCELLED'

/** Pedido completo retornado pelo GET /orders/{id} e na lista do cliente. */
export interface Order {
  id: string
  customerId: string
  status: OrderStatus
  createdAt: string
  totalAmount: string
  currency: string
  items: OrderItem[]
}

/** Payload do POST /orders. */
export interface CreateOrderRequest {
  customerId: string
  items: {
    productId: string
    quantity: number
    expectedUnitPrice: string
    currency: string
  }[]
}

/** Item do carrinho (estado local do cliente). */
export interface CartItem {
  product: Product
  quantity: number
}
