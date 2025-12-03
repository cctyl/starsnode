import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  base: './', // 使用相对路径，解决Android WebView文件加载问题
  build: {
    assetsDir: 'assets', // 资源目录
    outDir: '../android-h5/app/src/main/assets', // 直接输出到Android项目assets目录
    emptyOutDir: true, // 构建前清空输出目录
    target: 'es2015', // 降低目标版本，兼容Android WebView
    minify: 'esbuild', // 使用esbuild压缩，兼容性更好
    rollupOptions: {
      output: {
        manualChunks: undefined, // 禁用代码分割，确保所有JS在一个文件中
        format: 'iife', // 使用IIFE格式，避免ES模块问题
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  }
})
