import { defineConfig } from 'vitest/config';
import { resolve } from 'node:path';

/** Produces a self-contained webOS package without CDN dependencies. */
export default defineConfig({
  root: 'src',
  publicDir: resolve(import.meta.dirname, 'public'),
  base: './',
  test: { root: import.meta.dirname, environment: 'jsdom', globals: true, include: ['test/**/*.test.ts'] },
  build: {
    outDir: resolve(import.meta.dirname, 'dist'),
    emptyOutDir: true,
    target: ['chrome79'],
    rollupOptions: { input: resolve(import.meta.dirname, 'src/index.html') }
  }
});
