import { Routes, Route } from 'react-router-dom'
import HealthPage from './pages/HealthPage'

/**
 * Componente raiz.
 *
 * Define as rotas do app. Por ora só existe `/` (HealthPage), mas a
 * estrutura já está pronta para as próximas features (catálogo, carrinho,
 * checkout).
 */
export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HealthPage />} />
    </Routes>
  )
}
