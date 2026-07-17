import { Link } from 'react-router-dom'
import { useCart } from '../cart/CartContext'

/**
 * Barra de navegação topo.
 *
 * Mostra links para catálogo, meus pedidos, e o carrinho com badge da contagem
 * de itens. Fica presente em todas as páginas (renderizada no App).
 */
export default function Navbar() {
  const { count } = useCart()

  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
        <Link to="/" className="flex items-center gap-2 text-slate-900">
          <span className="text-lg font-extrabold tracking-tight">HFWJ</span>
          <span className="hidden text-sm text-slate-500 sm:inline">
            having fun with java
          </span>
        </Link>

        <nav className="flex items-center gap-2 sm:gap-4">
          <Link
            to="/orders"
            className="text-sm font-medium text-slate-600 transition hover:text-slate-900"
          >
            Meus pedidos
          </Link>
          <Link
            to="/cart"
            className="relative inline-flex items-center gap-1 rounded-lg bg-slate-100 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-200"
            aria-label={`Carrinho com ${count} itens`}
          >
            <svg
              className="h-5 w-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth={1.8}
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <circle cx="9" cy="21" r="1" />
              <circle cx="20" cy="21" r="1" />
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
            </svg>
            <span className="hidden sm:inline">Carrinho</span>
            {count > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-blue-600 px-1 text-xs font-bold text-white">
                {count}
              </span>
            )}
          </Link>
        </nav>
      </div>
    </header>
  )
}
