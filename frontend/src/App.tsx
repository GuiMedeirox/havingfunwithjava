import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import CatalogPage from './pages/CatalogPage'
import ProductDetailPage from './pages/ProductDetailPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrdersPage from './pages/OrdersPage'
import OrderDetailPage from './pages/OrderDetailPage'
import HealthPage from './pages/HealthPage'

/**
 * Componente raiz.
 *
 * Define as rotas do app:
 *  - `/`            → catálogo (lista navegável com filtros, busca, paginação)
 *  - `/products/:id`→ detalhe do produto (com botão adicionar ao carrinho)
 *  - `/cart`        → carrinho de compras (editar, remover, total)
 *  - `/checkout`    → checkout (dados do cliente + POST /orders)
 *  - `/orders`      → meus pedidos (lista por cliente)
 *  - `/orders/:id`  → detalhe do pedido (com status em polling)
 *  - `/health`      → checagem de saúde do api-gateway (scaffold original)
 */
export default function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <Routes>
        <Route path="/" element={<CatalogPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/health" element={<HealthPage />} />
      </Routes>
    </div>
  )
}
