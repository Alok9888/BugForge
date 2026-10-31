import { mkdir, copyFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const destination = path.join(root, 'public', 'runtime');
await mkdir(destination, { recursive: true });
await Promise.all([
  copyFile(path.join(root, 'node_modules/react/umd/react.development.js'), path.join(destination, 'react.js')),
  copyFile(path.join(root, 'node_modules/react-dom/umd/react-dom.development.js'), path.join(destination, 'react-dom.js')),
  copyFile(path.join(root, 'node_modules/@babel/standalone/babel.min.js'), path.join(destination, 'babel.js')),
]);
