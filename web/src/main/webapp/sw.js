/* reppin service worker: precache the app shell, serve cache-first, no network dependence.
 * Bump CACHE_VERSION on every release so old caches are removed.
 * Add every file the app needs offline to SHELL (fonts, icons, new CSS). */
const CACHE_VERSION = 'reppin-shell-v2';
const SHELL = [
  './',
  'index.html',
  'manifest.webmanifest',
  'css/tokens.css',
  'css/app.css',
  'js/reppin.js',
  'icons/icon-192.png',
  'icons/icon-512.png',
  'js/start.js'
];

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE_VERSION).then((cache) => cache.addAll(SHELL)));
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE_VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const request = event.request;
  if (request.method !== 'GET' || new URL(request.url).origin !== self.location.origin) {
    return;
  }
  event.respondWith(
    caches.match(request, { ignoreSearch: true }).then((cached) => cached || fetch(request))
  );
});
