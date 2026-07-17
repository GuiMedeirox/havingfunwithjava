/**
 * Spinner reutilizável (border animado). Tamanho configurável via className.
 */
export default function Spinner({
  label,
  className = 'h-6 w-6',
}: {
  label?: string
  className?: string
}) {
  return (
    <span className="inline-flex items-center gap-3 text-slate-500">
      <span
        className={`inline-block animate-spin rounded-full border-2 border-slate-300 border-t-blue-600 ${className}`}
        role="status"
        aria-label={label ?? 'Carregando'}
      />
      {label && <span className="text-sm">{label}</span>}
    </span>
  )
}
