import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Proxy do dev server: evita CORS ao bater no api-gateway local.
    // Mantemos VITE_API_URL como fonte de verdade do baseURL do Axios,
    // mas o proxy abaixo é uma conveniência para quem rodar só o front.
    proxy: {
      '/catalog': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/actuator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
