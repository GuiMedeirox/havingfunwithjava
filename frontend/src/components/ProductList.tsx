import type { Product } from '../types'
import ProductCard from './ProductCard'

/**
 * Grid responsivo de cards de produto.
 * Mobile-first: 1 coluna no mobile, escalando até 4 colunas em telas grandes.
 */
export default function ProductList({ products }: { products: Product[] }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {products.map((p) => (
        <ProductCard key={p.id} product={p} />
      ))}
    </div>
  )
}
