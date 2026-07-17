/**
 * Paginação clássica com botões Anterior / Próxima.
 *
 * Recebe `page` (base 0) e `totalPages`. Desabilita "Anterior" na primeira
 * página e "Próxima" na última. Mostra o indicador "Página X de Y".
 */
export default function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}) {
  if (totalPages <= 1) return null

  const prev = page > 0 ? page - 1 : null
  const next = page + 1 < totalPages ? page + 1 : null

  return (
    <nav
      className="mt-8 flex items-center justify-center gap-4"
      aria-label="Paginação"
    >
      <button
        type="button"
        disabled={prev === null}
        onClick={() => prev !== null && onPageChange(prev)}
        className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
      >
        ← Anterior
      </button>
      <span className="text-sm text-slate-600">
        Página {page + 1} de {totalPages}
      </span>
      <button
        type="button"
        disabled={next === null}
        onClick={() => next !== null && onPageChange(next)}
        className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
      >
        Próxima →
      </button>
    </nav>
  )
}
