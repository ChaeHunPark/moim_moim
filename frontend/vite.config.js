import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // '/api'로 시작하는 요청을 백엔드 서버(8080)로 전달
      '/api': {
        target: 'http://localhost:8080', 
        changeOrigin: true,
        // 백엔드 주소 자체에 /api가 없다면 아래 주석을 해제하세요.
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
