import type { Category } from '../types'

/**
 * Dropdown de filtro por categoria.
 *
 * Lista as categorias recebidas; o valor "" representa "Todas".
 * Quando o usuário troca, dispara `onChange` com o id da categoria
 * (ou undefined para limpar o filtro).
 */
export default function CategoryFilter({
  categories,
  value,
  onChange,
  disabled,
}: {
  categories: Category[]
  value?: string
  onChange: (categoryId: string | undefined) => void
  disabled?: boolean
}) {
  return (
    <label className="flex flex-col gap-1 text-sm">
      <span className="font-medium text-slate-600">Categoria</span>
      <select
        className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-800 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-slate-100"
        value={value ?? ''}
        disabled={disabled}
        onChange={(e) => {
          const v = e.target.value
          onChange(v === '' ? undefined : v)
        }}
      >
        <option value="">Todas as categorias</option>
        {categories.map((c) => (
          <option key={c.id} value={c.id}>
            {c.name}
          </option>
        ))}
      </select>
    </label>
  )
}
