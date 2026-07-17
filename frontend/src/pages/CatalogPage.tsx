import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchProducts } from '../api/products'
import { fetchCategories } from '../api/categories'
import ProductList from '../components/ProductList'
import CategoryFilter from '../components/CategoryFilter'
import SearchBar from '../components/SearchBar'
import Pagination from '../components/Pagination'
import Spinner from '../components/Spinner'

const PAGE_SIZE = 12

/**
 * Página de listagem do catálogo (rota `/`).
 *
 * Estado de filtros/paginação fica na query string (?category=&q=&page=):
 *  - permite navegar de/volta preservando contexto
 *  - alimenta diretamente a chave do React Query (cache correto)
 *
 * Sempre envia page/size para o backend devolver o shape paginado
 * {items, totalItems, totalPages, page, size}.
 */
export default function CatalogPage() {
  const [params, setParams] = useSearchParams()
  const category = params.get('category') ?? undefined
  const q = params.get('q') ?? ''
  const page = Number(params.get('page') ?? '0') || 0

  const productsQuery = useQuery({
    queryKey: ['products', { category, q, page, size: PAGE_SIZE }],
    queryFn: () =>
      fetchProducts({ category, q: q || undefined, page, size: PAGE_SIZE }),
    placeholderData: (prev) => prev, // mantém a lista antiga visível ao refetchar
  })

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 5 * 60_000, // categorias mudam pouco
  })

  /** Atualiza um parâmetro da URL e reseta para página 0. */
  function setParam(key: string, value: string | undefined) {
    const next = new URLSearchParams(params)
    if (value === undefined || value === '') next.delete(key)
    else next.set(key, value)
    next.delete('page') // qualquer novo filtro volta pra página 0
    setParams(next, { replace: false })
  }

  const items = productsQuery.data?.items ?? []
  const totalPages = productsQuery.data?.totalPages ?? 0

  return (
    <main className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-6">
          <h1 className="text-2xl font-bold text-slate-800">Catálogo</h1>
          <p className="mt-1 text-sm text-slate-500">
            Navegue pelos produtos disponíveis.
          </p>
        </div>
      </header>

      <section className="mx-auto max-w-7xl px-4 py-6">
        {/* Filtros + busca */}
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end">
          <SearchBar
            value={q}
            onChange={(term) => setParam('q', term || undefined)}
          />
          <CategoryFilter
            categories={categoriesQuery.data ?? []}
            value={category}
            onChange={(id) => setParam('category', id)}
            disabled={categoriesQuery.isLoading}
          />
        </div>

        {/* Estados */}
        {productsQuery.isLoading && <LoadingGrid />}

        {productsQuery.isError && (
          <ErrorState
            detail={
              productsQuery.error instanceof Error
                ? productsQuery.error.message
                : String(productsQuery.error)
            }
            onRetry={() => productsQuery.refetch()}
          />
        )}

        {productsQuery.isSuccess && items.length === 0 && <EmptyState />}

        {productsQuery.isSuccess && items.length > 0 && (
          <>
            <ProductList products={items} />
            <Pagination
              page={page}
              totalPages={totalPages}
              onPageChange={(p) => setParam('page', String(p))}
            />
          </>
        )}
      </section>
    </main>
  )
}

/** Grid de skeletons enquanto carrega. */
function LoadingGrid() {
  return (
    <div>
      <div className="mb-3 inline-flex">
        <Spinner label="Carregando produtos..." />
      </div>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="overflow-hidden rounded-xl border border-slate-200 bg-white"
          >
            <div className="aspect-[4/3] w-full animate-pulse bg-slate-200" />
            <div className="space-y-2 p-4">
              <div className="h-4 w-3/4 animate-pulse rounded bg-slate-200" />
              <div className="h-3 w-full animate-pulse rounded bg-slate-100" />
              <div className="mt-2 h-5 w-1/3 animate-pulse rounded bg-slate-200" />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function ErrorState({
  detail,
  onRetry,
}: {
  detail: string
  onRetry: () => void
}) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-6">
      <div className="flex items-center gap-2">
        <span className="inline-block h-3 w-3 rounded-full bg-red-500" />
        <p className="font-medium text-red-700">
          Não foi possível carregar os produtos.
        </p>
      </div>
      <pre className="mt-3 overflow-auto whitespace-pre-wrap break-all rounded-md bg-white p-3 text-xs text-red-800">
        {detail}
      </pre>
      <button
        type="button"
        onClick={onRetry}
        className="mt-3 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700"
      >
        Tentar novamente
      </button>
    </div>
  )
}

function EmptyState() {
  return (
    <div className="rounded-xl border border-dashed border-slate-300 bg-white p-10 text-center">
      <p className="text-lg font-medium text-slate-700">
        Nenhum produto encontrado.
      </p>
      <p className="mt-1 text-sm text-slate-500">
        Tente ajustar a busca ou a categoria selecionada.
      </p>
    </div>
  )
}
