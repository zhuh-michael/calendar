import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  root: 'src',
  plugins: [vue()],
  resolve: {
    alias: {
      '@kid': path.resolve(__dirname, 'src/views/kid'),
      '@parent': path.resolve(__dirname, 'src/views/parent'),
      '@shared': path.resolve(__dirname, 'src/shared'),
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    fs: {
      allow: [path.resolve(__dirname, 'src'), path.resolve(__dirname)]
    }
  },
  build: {
    outDir: '../dist',
    emptyOutDir: true,
    rollupOptions: {
      input: path.resolve(__dirname, 'src/index.html')
    }
  }
})


