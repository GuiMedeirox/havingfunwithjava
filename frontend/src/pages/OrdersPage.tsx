import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchOrders } from '../api/orders'
import { formatPrice } from '../lib/format'
import OrderStatusBadge from '../components/OrderStatusBadge'
import Spinner from '../components/Spinner'

/**
 * Página "Meus pedidos" (rota `/orders`).
 *
 * Lista os pedidos de um cliente. Como não há auth real, o cliente informa seu
 * UUID (mock). Cada pedido é um card com status, total e link para o detalhe.
 */
export default function OrdersPage() {
  const [customerId, setCustomerId] = useState(
    () => window.localStorage.getItem('hfwj.customerId') ?? '',
  )

  const ordersQuery = useQuery({
    queryKey: ['orders', customerId],
    queryFn: () => fetchOrders(customerId),
    enabled: Boolean(customerId.trim()),
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (customerId.trim()) {
      window.localStorage.setItem('hfwj.customerId', customerId.trim())
      ordersQuery.refetch()
    }
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">Meus pedidos</h1>

      <form onSubmit={handleSearch} className="mb-6 flex gap-2">
        <input
          type="text"
          value={customerId}
          onChange={(e) => setCustomerId(e.target.value)}
          placeholder="Seu ID de cliente (UUID)"
          className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <button
          type="submit"
          className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-900"
        >
          Buscar
        </button>
      </form>

      {!customerId.trim() && (
        <p className="text-slate-500">
          Informe seu ID de cliente acima para ver seus pedidos.
        </p>
      )}

      {ordersQuery.isLoading && <Spinner label="Carregando pedidos..." />}

      {ordersQuery.isError && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Erro ao carregar pedidos. Verifique se o ID está correto.
        </div>
      )}

      {ordersQuery.data && ordersQuery.data.length === 0 && (
        <p className="text-slate-500">
          Você ainda não tem pedidos.{' '}
          <Link to="/" className="text-blue-700 hover:underline">
            Explorar catálogo →
          </Link>
        </p>
      )}

      {ordersQuery.data && ordersQuery.data.length > 0 && (
        <ul className="space-y-3">
          {ordersQuery.data.map((order) => (
            <li key={order.id}>
              <Link
                to={`/orders/${order.id}`}
                className="block rounded-xl border border-slate-200 bg-white p-4 transition hover:border-blue-300 hover:shadow-sm"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-mono text-xs text-slate-400">
                      {order.id.slice(0, 8)}…
                    </p>
                    <p className="mt-1 text-sm text-slate-500">
                      {new Date(order.createdAt).toLocaleString('pt-BR')}
                    </p>
                  </div>
                  <OrderStatusBadge status={order.status} />
                </div>
                <div className="mt-3 flex items-center justify-between">
                  <span className="text-sm text-slate-600">
                    {order.items.length}{' '}
                    {order.items.length === 1 ? 'item' : 'itens'}
                  </span>
                  <span className="font-bold text-slate-900">
                    {formatPrice(order.totalAmount, order.currency)}
                  </span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
