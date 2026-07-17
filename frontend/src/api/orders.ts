import apiClient from './client'
import type { Order, CreateOrderRequest } from '../types'

/**
 * API de pedidos (orders-service via api-gateway).
 *
 * O api-gateway ainda não tem rota /orders/** configurada; o front aponta para
 * /orders/** que será roteada quando o gateway ganhar essa rota. Em dev, pode-se
 * chamar diretamente o orders-service (8082) ajustando VITE_API_URL.
 */

/**
 * Cria um pedido (POST /orders).
 * Retorna o pedido recém-criado (com id, status PENDING_PAYMENT, total calculado).
 */
export async function createOrder(payload: CreateOrderRequest): Promise<Order> {
  const { data } = await apiClient.post<Order>('/orders', payload)
  return data
}

/**
 * Lista pedidos de um cliente (GET /orders?customerId=).
 * Em produção, o customerId virá do JWT (repassado pelo gateway); aqui aceita
 * via query param pois o orders-service funciona assim neste slice.
 */
export async function fetchOrders(customerId: string): Promise<Order[]> {
  const { data } = await apiClient.get<Order[]>('/orders', {
    params: { customerId },
  })
  return data
}

/**
 * Detalha um pedido (GET /orders/{id}).
 */
export async function fetchOrderById(id: string): Promise<Order> {
  const { data } = await apiClient.get<Order>(`/orders/${id}`)
  return data
}
