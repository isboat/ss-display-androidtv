import { readFile, readdir } from 'node:fs/promises';
import { join } from 'node:path';

/** Validates the generated package before LG tooling signs it. */
const metadata = JSON.parse(await readFile('dist/appinfo.json', 'utf8'));
for (const field of ['id', 'version', 'type', 'main', 'title']) {
  if (!metadata[field]) throw new Error(`appinfo.json is missing ${field}`);
}
const files = await readdir('dist', { recursive: true });
const forbidden = files.filter(file => /\.(?:png|jpe?g|gif|webp|ipk)$/i.test(file));
if (forbidden.length) throw new Error(`Binary assets are not allowed: ${forbidden.join(', ')}`);
for (const required of [metadata.main, metadata.icon, metadata.largeIcon]) {
  await readFile(join('dist', required));
}
console.log(`Validated ${metadata.id} ${metadata.version} (${files.length} files).`);
