const CACHE='defense-v009';
const PAGE=new URL('./index.html',self.location.href).href;
const LEGACY=new URL('./siwan-siwoo-defense.html',self.location.href).href;
self.addEventListener('install',e=>e.waitUntil((async()=>{const c=await caches.open(CACHE);await c.add(new Request(PAGE,{cache:'reload'}));await self.skipWaiting()})()));
self.addEventListener('activate',e=>e.waitUntil((async()=>{for(const k of await caches.keys())if(k.startsWith('defense-')&&k!==CACHE)await caches.delete(k);await self.clients.claim()})()));
self.addEventListener('fetch',e=>{if(e.request.mode!=='navigate')return;const u=new URL(e.request.url);if(u.origin!==self.location.origin||![new URL(PAGE).pathname,new URL(LEGACY).pathname,new URL('./',PAGE).pathname].includes(u.pathname))return;e.respondWith((async()=>{try{const r=await fetch(new Request(PAGE,{cache:'no-store'}));if(!r.ok)throw Error('HTTP '+r.status);const c=await caches.open(CACHE);await c.put(PAGE,r.clone());return r}catch(err){return (await caches.open(CACHE)).match(PAGE).then(r=>r||Response.error())}})())});
