import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchProductById } from '../api/products'
import { fetchCategories } from '../api/categories'
import { formatPrice } from '../lib/format'
import Spinner from '../components/Spinner'
import { useCart } from '../cart/CartContext'

/**
 * Página de detalhe do produto (rota `/products/:id`).
 *
 * Busca o produto por id e também as categorias (para resolver o nome da
 * categoria a partir do categoryId). Botão "Adicionar ao carrinho" integra
 * com o useCart (adiciona e mostra feedback de adicionado).
 */
export default function ProductDetailPage() {
  const { add } = useCart()
  const [added, setAdded] = useState(false)
  const { id = '' } = useParams()

  const productQuery = useQuery({
    queryKey: ['product', id],
    queryFn: () => fetchProductById(id),
    enabled: Boolean(id),
    retry: 1,
  })

  // Categorias resolvidas em cache (já pré-carregadas pela listagem,
  // normalmente); usadas apenas para exibir o nome da categoria.
  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 5 * 60_000,
  })

  if (productQuery.isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50">
        <Spinner label="Carregando produto..." />
      </main>
    )
  }

  if (productQuery.isError || !productQuery.data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50 p-6">
        <div className="w-full max-w-md rounded-xl border border-red-200 bg-red-50 p-6 text-center">
          <p className="font-medium text-red-700">Produto não encontrado.</p>
          <p className="mt-1 text-sm text-red-600">
            {productQuery.error instanceof Error
              ? productQuery.error.message
              : 'O produto pode ter sido removido ou o id é inválido.'}
          </p>
          <Link
            to="/"
            className="mt-4 inline-block rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700"
          >
            ← Voltar ao catálogo
          </Link>
        </div>
      </main>
    )
  }

  const product = productQuery.data
  const categoryName =
    categoriesQuery.data?.find((c) => c.id === product.categoryId)?.name ??
    '—'

  return (
    <main className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-4">
          <Link
            to="/"
            className="text-sm font-medium text-blue-700 hover:text-blue-800"
          >
            ← Voltar ao catálogo
          </Link>
        </div>
      </header>

      <section className="mx-auto max-w-5xl px-4 py-8">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
          {/* Imagem placeholder */}
          <div className="flex aspect-square w-full items-center justify-center rounded-xl bg-gradient-to-br from-slate-100 to-slate-200">
            <svg
              className="h-24 w-24 text-slate-400"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={1.5}
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
              <path d="M3 6h18" />
              <path d="M16 10a4 4 0 0 1-8 0" />
            </svg>
          </div>

          {/* Detalhes */}
          <div className="flex flex-col">
            <span className="text-xs font-medium uppercase tracking-wide text-slate-400">
              {categoryName}
            </span>
            <h1 className="mt-1 text-2xl font-bold text-slate-900">
              {product.name}
            </h1>
            <p className="mt-4 text-3xl font-extrabold text-slate-900">
              {formatPrice(product.amount, product.currency)}
            </p>
            {product.description && (
              <div className="mt-6">
                <h2 className="text-sm font-semibold text-slate-700">
                  Descrição
                </h2>
                <p className="mt-1 whitespace-pre-line text-sm leading-relaxed text-slate-600">
                  {product.description}
                </p>
              </div>
            )}
            <div className="mt-8 flex flex-wrap items-center gap-3">
              <button
                type="button"
                className="rounded-lg bg-blue-600 px-6 py-3 text-base font-semibold text-white shadow-sm transition hover:bg-blue-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
                onClick={() => {
                  add(product)
                  setAdded(true)
                  setTimeout(() => setAdded(false), 2000)
                }}
              >
                Adicionar ao carrinho
              </button>
              {added && (
                <span className="text-sm font-medium text-green-700">
                  ✓ Adicionado!
                </span>
              )}
              <Link
                to="/cart"
                className="text-sm font-medium text-blue-700 hover:text-blue-800"
              >
                Ver carrinho →
              </Link>
            </div>
          </div>
        </div>
      </section>
    </main>
  )
}
