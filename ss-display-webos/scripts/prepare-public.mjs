import { cp, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import sharp from 'sharp';

/** Creates required webOS PNG launcher icons at build time without committing binary files. */
await rm('.generated-public', { recursive: true, force: true });
await mkdir('.generated-public', { recursive: true });
const metadata = JSON.parse(await readFile('appinfo.json', 'utf8'));
await writeFile('.generated-public/appinfo.json', `${JSON.stringify(metadata, null, 2)}\n`);
await cp('public/icon.svg', '.generated-public/icon.svg');
await cp('public/largeIcon.svg', '.generated-public/largeIcon.svg');
await sharp('public/icon.svg').resize(80, 80).png().toFile('.generated-public/icon.png');
await sharp('public/largeIcon.svg').resize(130, 130).png().toFile('.generated-public/largeIcon.png');
