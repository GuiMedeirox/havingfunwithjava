import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { useCart } from '../cart/CartContext'
import { createOrder } from '../api/orders'
import { formatPrice } from '../lib/format'
import Spinner from '../components/Spinner'

/**
 * Página de checkout (rota `/checkout`).
 *
 * Coleta os dados do cliente (id mock + nome/endereço para UX) e envia o pedido
 * via POST /orders no gateway. Em caso de sucesso, limpa o carrinho e redireciona
 * para a página de detalhe do pedido criado (/orders/{id}).
 *
 * <p>NOTA sobre customerId: como não há auth real ainda (login/JWT virá numa issue
 * futura), o cliente informa um customerId livre. Em produção, este campo viria
 * do JWT e não seria editável.
 */
export default function CheckoutPage() {
  const navigate = useNavigate()
  const { items, totalAmount, currency, clear } = useCart()
  const [customerId, setCustomerId] = useState('')
  const [name, setName] = useState('')
  const [address, setAddress] = useState('')

  const mutation = useMutation({
    mutationFn: createOrder,
    onSuccess: (order) => {
      clear()
      navigate(`/orders/${order.id}`)
    },
  })

  // Carrinho vazio não tem o que checkout.
  if (items.length === 0) {
    return (
      <main className="mx-auto max-w-3xl px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-slate-900">
          Não há itens para finalizar
        </h1>
        <Link
          to="/"
          className="mt-6 inline-block rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition hover:bg-blue-700"
        >
          Ver produtos
        </Link>
      </main>
    )
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!customerId.trim()) return
    mutation.mutate({
      customerId: customerId.trim(),
      items: items.map(({ product, quantity }) => ({
        productId: product.id,
        quantity,
        expectedUnitPrice: product.amount,
        currency: product.currency,
      })),
    })
  }

  return (
    <main className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">Checkout</h1>

      <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
        {/* Formulário */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="customerId"
              className="block text-sm font-medium text-slate-700"
            >
              ID do cliente
            </label>
            <input
              id="customerId"
              type="text"
              required
              value={customerId}
              onChange={(e) => setCustomerId(e.target.value)}
              placeholder="UUID do cliente"
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <p className="mt-1 text-xs text-slate-400">
              Sem login ainda — informe um UUID qualquer (mock).
            </p>
          </div>

          <div>
            <label
              htmlFor="name"
              className="block text-sm font-medium text-slate-700"
            >
              Nome
            </label>
            <input
              id="name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <div>
            <label
              htmlFor="address"
              className="block text-sm font-medium text-slate-700"
            >
              Endereço de entrega
            </label>
            <textarea
              id="address"
              rows={3}
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          {mutation.isError && (
            <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {mutation.error instanceof Error
                ? mutation.error.message
                : 'Erro ao criar pedido. Verifique os dados e tente novamente.'}
            </div>
          )}

          <button
            type="submit"
            disabled={mutation.isPending}
            className="w-full rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {mutation.isPending ? 'Criando pedido...' : 'Confirmar pedido'}
          </button>
        </form>

        {/* Resumo do pedido */}
        <aside className="rounded-xl border border-slate-200 bg-white p-4">
          <h2 className="mb-3 text-sm font-semibold text-slate-700">
            Resumo do pedido
          </h2>
          <ul className="space-y-2 text-sm">
            {items.map(({ product, quantity }) => (
              <li
                key={product.id}
                className="flex justify-between text-slate-600"
              >
                <span>
                  {product.name} × {quantity}
                </span>
                <span>
                  {formatPrice(
                    (Number(product.amount) * quantity).toFixed(2),
                    product.currency,
                  )}
                </span>
              </li>
            ))}
          </ul>
          <div className="mt-4 border-t border-slate-200 pt-3 text-lg">
            <span className="text-slate-500">Total: </span>
            <span className="font-extrabold text-slate-900">
              {formatPrice(totalAmount.toFixed(2), currency)}
            </span>
          </div>
        </aside>
      </div>

      {mutation.isPending && (
        <div className="mt-6 flex items-center gap-2 text-sm text-slate-500">
          <Spinner />
          Enviando pedido para o servidor...
        </div>
      )}
    </main>
  )
}
