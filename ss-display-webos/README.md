# OnScreenSync Display for LG webOS TV

A packaged, full-screen digital-signage player for LG webOS TV, implemented from [`doc/lg-webos-project-blueprint.md`](../doc/lg-webos-project-blueprint.md). Its local shell remains useful during outages while activation, content, and realtime updates use the OnScreenSync control plane.

## Technology

- TypeScript, Vite, Vitest, ESLint, DOMPurify, and Microsoft SignalR.
- Semantic HTML/CSS with a TV-safe 16:9 layout, large typography, high contrast, and LG remote support.
- Constructor injection and separate application, API, rendering, media, realtime, persistence, and platform boundaries.
- Locally bundled code only. All committed artwork is SVG source. The build generates LG-required PNG launcher icons into ignored output, so no binary artwork is committed.

Node.js 22 or newer is required. Production qualification requires physical oldest/newest supported LG models; a browser cannot validate codecs, memory pressure, remote behavior, or LG distribution policy.

## Develop and verify

```bash
npm ci
npm run dev
npm run check
```

The production bootstrap URL is fixed at `https://www.onscreensync.com/config.json`. `npm run check` runs lint, unit tests, TypeScript, a production bundle, and package validation.

## Package and install

```bash
npm run package:webos
# Configure Developer Mode and a device first:
ares-install --device <profile> artifacts/<package>.ipk
ares-launch --device <profile> com.onscreensync.display
```

`dist/`, `artifacts/`, credentials, and `.ipk` files are ignored. Signing belongs in a protected release environment; never commit LG signing profiles or private keys. Developer Mode is not production deployment. Confirm Content Store/commercial distribution, auto-launch, CORS, CSP, codecs, TLS, signing, and allowed binary store artwork with LG before release.

## Architecture

- `AppController` owns startup, activation, identity, content, and recovery state.
- `ApiClient` provides timeouts, trusted-origin authorization, and single-flight refresh.
- `SignalRService` owns one connection, registration, keep-alive, and commands.
- `ContentRenderer` and `HtmlVideoPlayer` own presentation and decoder cleanup.
- `StorageService`, `RemoteControl`, and `WebOsLifecycle` isolate platform behavior.

Runtime endpoints must be HTTPS. Rich text is sanitized, realtime message size is bounded, credentials are not logged, and authorization is sent only to configured origins. Supported layouts are `MediaOnly`, `MenuOnly`, `MenuOverlay`, `Text`, `CurrentDateTime`, and `MediaPlaylist`.

## CI

The repository workflows separately validate `main` pushes and pull requests. Main builds upload the unsigned application directory, including build-generated LG launcher icons. Both use `npm ci`, Node 22, least-privilege permissions, and concurrency cancellation.
