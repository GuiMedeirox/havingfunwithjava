import { Link } from 'react-router-dom'
import type { Product } from '../types'
import { formatPrice } from '../lib/format'

/**
 * Card de produto no grid de listagem.
 *
 * Mostra placeholder de imagem (gradiente + ícone SVG), nome, preço
 * formatado em BRL, e envolve tudo num Link para /products/{id}.
 */
export default function ProductCard({ product }: { product: Product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="group flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
    >
      <ImagePlaceholder name={product.name} />
      <div className="flex flex-1 flex-col gap-1 p-4">
        <h3 className="line-clamp-2 text-sm font-semibold text-slate-800 group-hover:text-blue-700">
          {product.name}
        </h3>
        {product.description && (
          <p className="line-clamp-2 text-xs text-slate-500">
            {product.description}
          </p>
        )}
        <p className="mt-auto pt-2 text-lg font-bold text-slate-900">
          {formatPrice(product.amount, product.currency)}
        </p>
      </div>
    </Link>
  )
}

/** Placeholder de imagem: div com gradiente e um ícone de sacola SVG. */
function ImagePlaceholder({ name }: { name: string }) {
  return (
    <div
      className="flex aspect-[4/3] w-full items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200"
      aria-label={`Imagem de ${name}`}
      role="img"
    >
      <svg
        className="h-12 w-12 text-slate-400"
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
  )
}
