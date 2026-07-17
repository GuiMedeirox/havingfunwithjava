import { useQuery } from '@tanstack/react-query'
import { fetchCatalogHealth, type HealthResponse } from '../api/health'

/**
 * Página placeholder que prova que o stack está vivo.
 *
 * Faz GET /catalog/health via api-gateway usando React Query + Axios e
 * exibe status / serviço / timestamp. Trata loading e erro graciosamente.
 *
 * Usa classes Tailwind de verdade (bg-blue-600, p-4, rounded-xl, etc.)
 * para demonstrar que o Tailwind está configurado e funcional.
 */
export default function HealthPage() {
  const query = useQuery<HealthResponse>({
    queryKey: ['catalog-health'],
    queryFn: fetchCatalogHealth,
    retry: 1,
    refetchOnWindowFocus: false,
  })

  const isUp = query.data?.status?.toUpperCase() === 'UP'

  return (
    <main className="min-h-screen bg-slate-50 flex items-center justify-center p-6">
      <div className="w-full max-w-lg">
        <header className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-slate-800">
            Having Fun With Java
          </h1>
          <p className="mt-2 text-slate-500">
            Frontend scaffold — React + Vite + TS + Tailwind + React Query
          </p>
        </header>

        <section className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
          <h2 className="text-lg font-semibold text-slate-700 mb-4">
            Status do api-gateway / catalog-service
          </h2>

          {query.isLoading && <LoadingState />}

          {query.isError && (
            <ErrorState
              message="Não foi possível alcançar o /catalog/health."
              detail={
                query.error instanceof Error
                  ? query.error.message
                  : String(query.error)
              }
              onRetry={() => query.refetch()}
            />
          )}

          {query.isSuccess && (
            <SuccessState data={query.data} isUp={isUp} />
          )}
        </section>

        <footer className="mt-6 text-center text-sm text-slate-400">
          <span className="inline-block bg-blue-600 text-white px-3 py-1 rounded-full">
            Tailwind ON
          </span>
        </footer>
      </div>
    </main>
  )
}

function LoadingState() {
  return (
    <div className="flex items-center gap-3 text-slate-500">
      <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
      Verificando /catalog/health...
    </div>
  )
}

function ErrorState({
  message,
  detail,
  onRetry,
}: {
  message: string
  detail: string
  onRetry: () => void
}) {
  return (
    <div className="space-y-3">
      <div className="flex items-center gap-3">
        <span className="inline-block h-3 w-3 rounded-full bg-red-500" />
        <p className="font-medium text-red-700">{message}</p>
      </div>
      <pre className="text-xs bg-red-50 text-red-800 rounded-md p-3 overflow-auto whitespace-pre-wrap break-all">
        {detail}
      </pre>
      <button
        type="button"
        onClick={onRetry}
        className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
      >
        Tentar novamente
      </button>
    </div>
  )
}

function SuccessState({ data, isUp }: { data: HealthResponse; isUp: boolean }) {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <span
          className={`inline-block h-3 w-3 rounded-full ${
            isUp ? 'bg-green-500' : 'bg-amber-500'
          }`}
        />
        <p className="font-semibold text-slate-800">
          {data.status ?? 'UNKNOWN'}
        </p>
      </div>

      <dl className="grid grid-cols-1 gap-2 text-sm">
        <Field label="Serviço" value={data.service ?? '—'} />
        <Field label="Timestamp" value={data.at ?? '—'} />
        <Field label="Endpoint" value="GET /catalog/health (via gateway)" />
      </dl>
    </div>
  )
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4 border-b border-slate-100 pb-2">
      <dt className="text-slate-500">{label}</dt>
      <dd className="text-slate-800 font-medium text-right break-all">
        {value}
      </dd>
    </div>
  )
}
