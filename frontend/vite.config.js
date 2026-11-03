import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    // GitHub Pages serves this repository under /BugForge/; the other deployments
    // serve the app from the domain root.
    base: process.env.GITHUB_PAGES === 'true' ? '/BugForge/' : '/',
    plugins: [react(), tailwindcss()],
    server: {
      proxy: {
        '/api': env.VITE_API_PROXY || 'http://localhost:8080',
      },
    },
  };
});
