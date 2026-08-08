import { readFileSync } from 'node:fs';
import type { Plugin } from 'vite';
import { defineConfig } from 'vitest/config';

/** Emits Tizen's manifest beside the compiled web application. */
function tizenManifest(): Plugin {
  return {
    name: 'tizen-manifest',
    generateBundle() {
      this.emitFile({ type: 'asset', fileName: 'config.xml', source: readFileSync('config.xml') });
    },
  };
}

export default defineConfig({
  root: 'src',
  publicDir: '../public',
  base: './',
  build: { outDir: '../dist', emptyOutDir: true, target: 'es2018' },
  plugins: [tizenManifest()],
  test: { environment: 'jsdom', globals: true, include: ['../test/**/*.test.ts'] },
});
