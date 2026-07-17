import { useEffect, useRef, useState } from 'react'

/**
 * Input de busca por nome com debounce.
 *
 * O componente controla um estado interno para o input responder
 * instantaneamente, mas só dispara `onChange` depois de `debounceMs`
 * (default 400ms) sem digitação — evita refetch a cada tecla.
 */
export default function SearchBar({
  value,
  onChange,
  placeholder = 'Buscar produtos por nome...',
  debounceMs = 400,
}: {
  value: string
  onChange: (term: string) => void
  placeholder?: string
  debounceMs?: number
}) {
  const [local, setLocal] = useState(value)
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null)

  // Sincroniza o estado interno se o valor externo mudar (ex.: limpar filtros).
  useEffect(() => {
    setLocal(value)
  }, [value])

  useEffect(() => {
    if (timer.current) clearTimeout(timer.current)
    timer.current = setTimeout(() => {
      onChange(local)
    }, debounceMs)
    return () => {
      if (timer.current) clearTimeout(timer.current)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [local, debounceMs])

  return (
    <label className="flex flex-1 flex-col gap-1 text-sm">
      <span className="font-medium text-slate-600">Busca</span>
      <input
        type="search"
        value={local}
        placeholder={placeholder}
        onChange={(e) => setLocal(e.target.value)}
        className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-800 shadow-sm placeholder:text-slate-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
      />
    </label>
  )
}
