import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchOrderById } from '../api/orders'
import { formatPrice } from '../lib/format'
import OrderStatusBadge from '../components/OrderStatusBadge'
import Spinner from '../components/Spinner'

/**
 * Página de detalhe do pedido (rota `/orders/:id`).
 *
 * Mostra os itens, o total calculado e o status. O status é consultado com
 * refetch a cada 5s (polling) enquanto está PENDING_PAYMENT — refletindo o
 * avanço do processamento de pagamento em tempo quase real. Quando o status
 * chega a um estado terminal (PAID, CANCELLED), o polling para.
 */
export default function OrderDetailPage() {
  const { id = '' } = useParams()

  const orderQuery = useQuery({
    queryKey: ['order', id],
    queryFn: () => fetchOrderById(id),
    enabled: Boolean(id),
    // Polling: refetch a cada 5s enquanto o status ainda é PENDING_PAYMENT.
    // Estados terminais param o polling (refetchInterval returning false).
    refetchInterval: (query) =>
      query.state.data?.status === 'PENDING_PAYMENT' ? 5000 : false,
  })

  if (orderQuery.isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50">
        <Spinner label="Carregando pedido..." />
      </main>
    )
  }

  if (orderQuery.isError || !orderQuery.data) {
    return (
      <main className="mx-auto max-w-md px-4 py-16 text-center">
        <p className="font-medium text-red-700">Pedido não encontrado.</p>
        <Link
          to="/orders"
          className="mt-4 inline-block rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white"
        >
          ← Meus pedidos
        </Link>
      </main>
    )
  }

  const order = orderQuery.data

  return (
    <main className="mx-auto max-w-3xl px-4 py-8">
      <Link
        to="/orders"
        className="text-sm font-medium text-blue-700 hover:text-blue-800"
      >
        ← Meus pedidos
      </Link>

      <div className="mt-4 rounded-xl border border-slate-200 bg-white p-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="font-mono text-lg font-bold text-slate-900">
              Pedido {order.id.slice(0, 8)}…
            </h1>
            <p className="mt-1 text-sm text-slate-500">
              {new Date(order.createdAt).toLocaleString('pt-BR')}
            </p>
          </div>
          <OrderStatusBadge status={order.status} />
        </div>

        {order.status === 'PENDING_PAYMENT' && (
          <p className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
            Processando pagamento… esta página atualiza automaticamente.
          </p>
        )}

        <h2 className="mt-6 text-sm font-semibold text-slate-700">Itens</h2>
        <ul className="mt-2 divide-y divide-slate-100">
          {order.items.map((item) => (
            <li
              key={item.productId}
              className="flex items-center justify-between py-3 text-sm"
            >
              <div>
                <p className="font-medium text-slate-900">{item.productName}</p>
                <p className="text-slate-500">
                  {item.quantity} × {formatPrice(item.unitPrice, item.currency)}
                </p>
              </div>
              <span className="font-semibold text-slate-900">
                {formatPrice(item.subtotal, item.currency)}
              </span>
            </li>
          ))}
        </ul>

        <div className="mt-4 flex items-center justify-between border-t border-slate-200 pt-4 text-lg">
          <span className="text-slate-500">Total</span>
          <span className="font-extrabold text-slate-900">
            {formatPrice(order.totalAmount, order.currency)}
          </span>
        </div>
      </div>
    </main>
  )
}
