import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { CartItem, Product } from '../types'

/**
 * Contexto do carrinho de compras.
 *
 * <p>Estado local do cliente, persistido em localStorage entre sessões. As páginas
 * consomem via {@link useCart}, que expõe itens, total (calculado), e operações
 * (adicionar, remover, ajustar quantidade, limpar).
 *
 * <p>O carrinho armazena o snapshot do {@link Product} (id, nome, preço, moeda)
 * no momento da adição. Se o catálogo reajustar o preço depois, o carrinho ainda
 * mostra o preço antigo até o checkout — quando o orders-service valida contra o
 * preço atual e rejeita divergência (proteção contra stale price).
 */

const STORAGE_KEY = 'hfwj.cart'

interface CartContextValue {
  /** Itens atuais no carrinho. */
  items: CartItem[]
  /** Número total de unidades (soma das quantidades). */
  count: number
  /** Valor total formatável: soma (product.amount × quantity) + moeda. */
  totalAmount: number
  /** Moeda do carrinho (assume uma só; primeira do carrinho, default 'BRL'). */
  currency: string
  /** Adiciona um produto (incrementa quantidade se já existir). */
  add: (product: Product, quantity?: number) => void
  /** Remove um produto pelo id. */
  remove: (productId: string) => void
  /** Ajusta a quantidade de um produto (remove se <= 0). */
  setQuantity: (productId: string, quantity: number) => void
  /** Limpa o carrinho. */
  clear: () => void
}

const CartContext = createContext<CartContextValue | null>(null)

function loadFromStorage(): CartItem[] {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(loadFromStorage)

  // Persiste no localStorage a cada mudança.
  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  }, [items])

  const add = useCallback((product: Product, quantity = 1) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.product.id === product.id)
      if (existing) {
        return prev.map((i) =>
          i.product.id === product.id
            ? { ...i, quantity: i.quantity + quantity }
            : i,
        )
      }
      return [...prev, { product, quantity }]
    })
  }, [])

  const remove = useCallback((productId: string) => {
    setItems((prev) => prev.filter((i) => i.product.id !== productId))
  }, [])

  const setQuantity = useCallback((productId: string, quantity: number) => {
    setItems((prev) => {
      if (quantity <= 0) {
        return prev.filter((i) => i.product.id !== productId)
      }
      return prev.map((i) =>
        i.product.id === productId ? { ...i, quantity } : i,
      )
    })
  }, [])

  const clear = useCallback(() => setItems([]), [])

  const value = useMemo<CartContextValue>(() => {
    const count = items.reduce((sum, i) => sum + i.quantity, 0)
    const totalAmount = items.reduce(
      (sum, i) => sum + Number(i.product.amount) * i.quantity,
      0,
    )
    const currency = items[0]?.product.currency ?? 'BRL'
    return { items, count, totalAmount, currency, add, remove, setQuantity, clear }
  }, [items, add, remove, setQuantity, clear])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

/**
 * Hook para acessar o carrinho. Deve ser usado dentro de {@link CartProvider}.
 */
export function useCart(): CartContextValue {
  const ctx = useContext(CartContext)
  if (!ctx) {
    throw new Error('useCart deve ser usado dentro de CartProvider')
  }
  return ctx
}
