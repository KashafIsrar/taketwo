import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Lets the frontend call '/api/...' during dev without CORS headaches;
      // the backend's CORS config (localhost:5173) is the belt-and-suspenders match.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
