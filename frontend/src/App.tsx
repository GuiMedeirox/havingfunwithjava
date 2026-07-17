import { Routes, Route } from 'react-router-dom'
import CatalogPage from './pages/CatalogPage'
import ProductDetailPage from './pages/ProductDetailPage'
import HealthPage from './pages/HealthPage'

/**
 * Componente raiz.
 *
 * Define as rotas do app:
 *  - `/`            → catálogo (lista navegável com filtros, busca, paginação)
 *  - `/products/:id`→ detalhe do produto
 *  - `/health`      → checagem de saúde do api-gateway (scaffold original)
 */
export default function App() {
  return (
    <Routes>
      <Route path="/" element={<CatalogPage />} />
      <Route path="/products/:id" element={<ProductDetailPage />} />
      <Route path="/health" element={<HealthPage />} />
    </Routes>
  )
}
