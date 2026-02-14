import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // 将所有 /api 开头的请求代理到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // 可选：重写路径（如果前后端前缀不一致）
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});