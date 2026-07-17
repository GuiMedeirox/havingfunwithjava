import { Link } from 'react-router-dom'
import { useCart } from '../cart/CartContext'
import { formatPrice } from '../lib/format'

/**
 * Página do carrinho (rota `/cart`).
 *
 * Lista os itens, permite remover e ajustar quantidade, recalcula o total em
 * tempo real (via useCart). Carrinho vazio mostra CTA de volta ao catálogo.
 */
export default function CartPage() {
  const { items, setQuantity, remove, totalAmount, currency, clear } = useCart()

  if (items.length === 0) {
    return (
      <main className="mx-auto max-w-3xl px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-slate-900">Seu carrinho está vazio</h1>
        <p className="mt-2 text-slate-600">Que tal explorar o catálogo?</p>
        <Link
          to="/"
          className="mt-6 inline-block rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition hover:bg-blue-700"
        >
          Ver produtos
        </Link>
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Carrinho</h1>
        <button
          type="button"
          onClick={clear}
          className="text-sm font-medium text-slate-500 transition hover:text-red-600"
        >
          Limpar
        </button>
      </div>

      <ul className="divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white">
        {items.map(({ product, quantity }) => (
          <li
            key={product.id}
            className="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex-1">
              <Link
                to={`/products/${product.id}`}
                className="font-medium text-slate-900 hover:text-blue-700"
              >
                {product.name}
              </Link>
              <p className="mt-1 text-sm text-slate-500">
                {formatPrice(product.amount, product.currency)} cada
              </p>
            </div>

            <div className="flex items-center gap-3">
              <div className="flex items-center rounded-lg border border-slate-300">
                <button
                  type="button"
                  className="px-3 py-1 text-slate-600 transition hover:bg-slate-100"
                  onClick={() => setQuantity(product.id, quantity - 1)}
                  aria-label="Diminuir quantidade"
                >
                  −
                </button>
                <input
                  type="number"
                  min={1}
                  value={quantity}
                  onChange={(e) =>
                    setQuantity(
                      product.id,
                      Math.max(1, Number(e.target.value) || 1),
                    )
                  }
                  className="w-12 border-x border-slate-300 py-1 text-center text-sm focus:outline-none"
                />
                <button
                  type="button"
                  className="px-3 py-1 text-slate-600 transition hover:bg-slate-100"
                  onClick={() => setQuantity(product.id, quantity + 1)}
                  aria-label="Aumentar quantidade"
                >
                  +
                </button>
              </div>

              <span className="w-24 text-right font-semibold text-slate-900">
                {formatPrice(
                  (Number(product.amount) * quantity).toFixed(2),
                  product.currency,
                )}
              </span>

              <button
                type="button"
                onClick={() => remove(product.id)}
                className="text-sm text-slate-400 transition hover:text-red-600"
                aria-label={`Remover ${product.name}`}
              >
                Remover
              </button>
            </div>
          </li>
        ))}
      </ul>

      <div className="mt-6 flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="text-lg">
          <span className="text-slate-500">Total: </span>
          <span className="font-extrabold text-slate-900">
            {formatPrice(totalAmount.toFixed(2), currency)}
          </span>
        </div>
        <Link
          to="/checkout"
          className="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white shadow-sm transition hover:bg-blue-700"
        >
          Finalizar pedido →
        </Link>
      </div>
    </main>
  )
}
