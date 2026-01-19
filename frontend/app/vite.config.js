import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@kid': path.resolve(__dirname, '../kid/src'),
      '@parent': path.resolve(__dirname, '../parent/src'),
      '@shared': path.resolve(__dirname, '../shared'),
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    fs: {
      // allow serving files from the project root and sibling frontend folders
      allow: [path.resolve(__dirname, '..')]
    }
  }
})


