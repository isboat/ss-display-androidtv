# LG webOS TV replication blueprint

## 1. Objective and recommended application type

This document describes how to reproduce the Screen Service Android TV digital-signage player as an application for **LG TVs running webOS TV**. It targets functional and operational parity rather than a source-code port: Android activities, fragments, Hilt, Retrofit, `VideoView`, BroadcastReceivers, APK/AAB packaging, and Android permissions do not exist on webOS.

The recommended implementation is a **packaged webOS TV web application**:

- HTML and CSS render the full-screen interface;
- TypeScript provides application logic and typed wire models;
- a bundler emits JavaScript compatible with the oldest supported webOS TV browser engine;
- the application is packaged and signed as an `.ipk` using LG webOS TV tooling;
- standard Web APIs handle HTTP, WebSockets, timers, images, and HTML5 media;
- webOS TV APIs and Luna service calls are isolated behind platform adapters where required.

A hosted web app is not recommended for the player shell. Packaging the shell, fonts, icons, SignalR client, and failure UI locally allows the TV to start and explain a network outage without depending on an external page or CDN. Backend-controlled content and endpoints can remain remote.

The replication must preserve:

- bootstrap from `https://www.onscreensync.com/config.json`;
- device-code activation and polling;
- persistent access/refresh tokens and 401 refresh/retry;
- device identity, tenant assignment, and content retrieval;
- media-only, menu-only, menu-overlay, rich-text, clock, and playlist layouts;
- content checksum de-duplication;
- SignalR negotiation, registration, commands, keep-alive, reconnect, and cleanup;
- full LG remote and Magic Remote usability without touch input;
- visible error/operator/upgrade states and unattended recovery;
- development, signing, packaging, physical-device testing, distribution, and fleet operations.

## 2. Android-to-webOS technology mapping

| Existing Android implementation | Recommended LG webOS implementation | Rationale |
| --- | --- | --- |
| Kotlin/JVM 17 | TypeScript transpiled to model-compatible JavaScript | Strong wire models and maintainable async logic while supporting older embedded browser engines. |
| APK/AAB application | Packaged web application (`.ipk`) with `appinfo.json` | Native distribution unit for ordinary webOS TV web apps. |
| Activities and Fragments | Single-page application, explicit state machine, reusable DOM renderer classes | Eliminates browser-history issues and centralizes renderer cleanup. |
| XML layouts and View Binding | Semantic HTML, CSS Grid/Flexbox, CSS custom properties | Produces responsive TV-safe 16:9 layouts without Android view dependencies. |
| MVVM ViewModels and repositories | Controllers/stores, API services, and platform adapters | Keeps state, transport, rendering, and LG-specific APIs separated and testable. |
| Retrofit + OkHttp | Fetch-based `ApiClient` | Provides JSON requests, timeouts, Bearer headers, refresh serialization, and retry/error mapping. |
| Hilt dependency injection | Constructor injection plus a small composition root | Avoids a heavy runtime container and makes platform dependencies replaceable in tests. |
| Kotlin coroutines and Flow | Promises, async/await, `AbortController`, events, and an observable store | Supports cancellation, state updates, activation polling, and serialized commands. |
| Microsoft SignalR Java client | Locally bundled `@microsoft/signalr` JavaScript client | Reuses the existing ASP.NET SignalR hub protocol and message names. |
| Picasso | `<img>` with browser caching; optional Cache API/IndexedDB asset manager | Standard image rendering with an optional bounded offline cache. |
| Android `VideoView` | HTML5 `<video>` with capability-tested codecs | The standard media path for packaged webOS web apps; lifecycle/listener handling must be explicit. |
| Android WebView | Controlled `<iframe>` or an application-owned external-content container | Allows approved external sources, subject to CSP, CORS/frame policy, and platform restrictions. |
| SharedPreferences | `localStorage` for small values; IndexedDB for structured content/cache | Persists configuration, tokens, identity, checksum, and optionally last-known-good content. |
| Android boot receiver | Deployment-specific auto-launch or LG commercial signage management | Consumer webOS apps cannot assume Android-style launch-on-boot privileges. |
| Leanback remote focus | Keyboard event handling plus explicit focus/spatial-navigation manager | Supports directional pad, OK, Back, media keys, and Magic Remote pointer interaction. |

Avoid downloading executable JavaScript at runtime. Bundle the application and SignalR library into the `.ipk`, pin dependency versions, commit the lock file, and generate production output without secrets or verbose network logs.

## 3. Decisions required before implementation

### 3.1 Consumer webOS TV versus commercial signage

Clarify the hardware and distribution channel first:

1. **Consumer LG webOS TV / LG Content Store:** ordinary public app constraints, LG review, user-driven installation, and no assumption of permanent kiosk privileges.
2. **LG commercial displays:** webOS Signage, SuperSign, or partner management facilities can have different SDKs, APIs, policies, and auto-start capabilities from consumer webOS TV.
3. **Hospitality/Pro:Centric:** a separate managed environment with its own deployment and integration model.
4. **Developer Mode:** temporary development/sideload workflow only; it is not a production fleet strategy and developer sessions/app availability can expire.

This blueprint uses the public packaged web-app model as the portable baseline. Any signage-only or privileged feature must remain behind an adapter and be validated against the exact commercial SDK/hardware agreement.

### 3.2 Supported model years and webOS versions

LG TV web engines, memory, media profiles, TLS support, WebSocket behavior, and APIs differ by model year. Establish the oldest supported platform before choosing TypeScript compilation targets, polyfills, CSS, SignalR version, encryption settings, and codecs.

Maintain a compatibility matrix with:

- LG model and market/region;
- consumer/commercial product family;
- model year, webOS version, firmware, and browser engine;
- screen resolution and device-pixel behavior;
- JavaScript/CSS features and storage quota;
- supported video container, video/audio codec, profile, level, bitrate, resolution, and frame rate;
- autoplay, looping, seek, buffering, and decoder-recovery behavior;
- WebSocket, DNS, proxy, TLS version, cipher, and certificate-chain behavior;
- memory growth over a 24–72 hour content/SignalR session;
- Developer Mode, store, managed installation, and auto-start behavior.

Transpile and polyfill only to the selected minimum. Test on real oldest/newest TVs; desktop Chromium and the simulator cannot prove decoder, memory, remote, lifecycle, or firmware behavior.

### 3.3 Auto-launch and kiosk operation

The Android `BootReceiver` has no general public webOS equivalent. Do not design a consumer application on the assumption that it may launch itself silently after every boot. Depending on product family, acceptable solutions may include:

- the TV's user-facing “recent/last app” behavior, where available;
- LG commercial signage auto-start settings or SuperSign management;
- hospitality/partner APIs and provisioning;
- an external fleet-management controller;
- installation instructions requiring the operator to launch the app.

Auto-launch is a deployment acceptance criterion, not a JavaScript feature. Obtain written confirmation from LG or the commercial integration partner for the chosen devices and distribution model.

## 4. Proposed repository structure

Create a separate webOS project/repository so Android Gradle output and webOS `.ipk` output have independent build, signing, dependency, and release lifecycles:

```text
ss-display-webos/
├── appinfo.json
├── icon.png
├── largeIcon.png
├── package.json
├── package-lock.json
├── tsconfig.json
├── vite.config.ts
├── scripts/
│   ├── build-webos.mjs
│   └── validate-package.mjs
├── src/
│   ├── index.html
│   ├── main.ts
│   ├── application/
│   │   ├── AppController.ts
│   │   ├── AppRouter.ts
│   │   ├── AppState.ts
│   │   └── CompositionRoot.ts
│   ├── api/
│   │   ├── ApiClient.ts
│   │   ├── EndpointConfig.ts
│   │   ├── BootstrapApi.ts
│   │   ├── ActivationApi.ts
│   │   ├── DeviceApi.ts
│   │   ├── ContentApi.ts
│   │   └── SignalRRegistrationApi.ts
│   ├── auth/
│   │   ├── AuthSession.ts
│   │   └── ActivationPoller.ts
│   ├── realtime/
│   │   ├── SignalRService.ts
│   │   └── CommandDispatcher.ts
│   ├── content/
│   │   ├── ContentCoordinator.ts
│   │   ├── LayoutFactory.ts
│   │   └── PlaylistController.ts
│   ├── renderers/
│   │   ├── MediaOnlyRenderer.ts
│   │   ├── MenuOnlyRenderer.ts
│   │   ├── MenuOverlayRenderer.ts
│   │   ├── TextRenderer.ts
│   │   ├── ClockRenderer.ts
│   │   └── PlaylistRenderer.ts
│   ├── media/
│   │   ├── HtmlVideoPlayer.ts
│   │   ├── ImageLoader.ts
│   │   └── ExternalContent.ts
│   ├── platform/
│   │   ├── WebOsLifecycle.ts
│   │   ├── WebOsServiceBridge.ts
│   │   ├── RemoteControl.ts
│   │   ├── DeviceMetadata.ts
│   │   └── Storage.ts
│   ├── screens/
│   │   ├── SplashScreen.ts
│   │   ├── ActivationScreen.ts
│   │   ├── StatusScreen.ts
│   │   └── ErrorScreen.ts
│   ├── models/
│   ├── security/
│   │   ├── HtmlSanitizer.ts
│   │   └── UrlPolicy.ts
│   ├── telemetry/
│   ├── styles/
│   └── utils/
├── test/
└── dist/                         # generated; never hand-edit
```

Keep all `webOS`, `PalmSystem`, and Luna/service calls in `platform/`. Pure application code then runs in desktop unit tests with fake lifecycle, remote, storage, video, clock, and service adapters.

## 5. Architecture and application state machine

Use a single HTML document with explicit state rather than emulating Android activities through browser pages:

```text
STARTING
   |
BOOTSTRAPPING -----> OFFLINE/ERROR
   |
ACTIVATING <-------> ACTIVATION_ERROR
   |
RESOLVING_DEVICE_CONTENT
   |
DISPLAYING <-------> CONTENT_STATUS
```

### Component responsibilities

- **`AppController`:** application lifecycle, state transitions, startup, foreground/resume validation, and controlled exit/reload.
- **`AppRouter`:** mounts exactly one screen/renderer into the root, owns focus entry, and avoids browser history.
- **`ApiClient`:** trusted-origin policy, JSON, timeouts, Bearer auth, one token refresh, cancellation, and normalized errors.
- **`AuthSession`:** token persistence and a single-flight refresh promise.
- **`ContentCoordinator`:** device/content fetch, schema checks, checksum rules, renderer swap, and last-known-good fallback.
- **`SignalRService`:** negotiation, hub lifetime, registration, keep-alive, reconnect, and graceful removal.
- **`CommandDispatcher`:** validates and serializes remote messages.
- **`LayoutFactory`:** allow-listed mapping from backend `templateKey` to a renderer.
- **`PlaylistController`:** index, one timer, video completion, preload, cancellation, and loop guard.
- **Platform adapters:** webOS lifecycle, remote keys, service bridge, device metadata, and persistence.

Every screen/renderer implements a lifecycle such as `mount(container, model)`, `suspend()`, `resume()`, and `destroy()`. `destroy()` must cancel timers/fetches, pause and unload video, remove event listeners, revoke object URLs, release large DOM/image references, and cancel external-content work. SignalR belongs to application scope, not a transient router screen.

## 6. End-to-end flows

### 6.1 Cold launch, lifecycle, and bootstrap

1. Load a local `index.html`, stylesheet, bundled script, logo, and splash. A network failure must never produce an empty white page.
2. Initialize global error/rejection reporting, lifecycle events, remote handling, storage migrations, and a watchdog before starting network requests.
3. Request `GET https://www.onscreensync.com/config.json` directly from the canonical URL. Do not reproduce Android's dynamic host interceptor, which can let stale stored configuration alter a future bootstrap request.
4. Validate the `display-api` object: required fields, HTTPS/WSS policy, URL syntax, allow-listed hosts, and no embedded credentials. Write the whole validated config plus schema version/timestamp atomically.
5. On bootstrap failure, use last-known config only if policy permits and it has not exceeded its maximum age. Show network/retry state and use capped exponential backoff with jitter.
6. If there is no token, enter activation. Otherwise fetch device information then content. If refresh proves the session invalid, clear credentials and return to activation without clearing last-known content until replacement content is valid.
7. Establish SignalR after authoritative device identity is available. It may start alongside content loading, but both flows must share the same current auth session.

Listen for `visibilitychange`, page/application lifecycle callbacks supported by the target webOS versions, connectivity changes where dependable, and unload/close signals. When hidden, pause video and nonessential animation/timers; when restored, revalidate clock, content freshness, video, network, and hub state. Cleanup callbacks are best-effort during power loss, so the backend must expire stale SignalR registrations.

### 6.2 First-time device activation

Replicate the existing backend contract while improving cancellation and expiry behavior:

1. `POST device-code-url` with JSON containing `clientId: "clientid"` and `grantType: "user_code"`.
2. Validate and display the returned `verificationUrl`, `userCode`, optional `deviceName`, `interval`, and `expiresIn`. Use high-contrast, television-distance typography and keep URL/code selectable only if it does not interfere with remote focus.
3. Optionally render a locally generated QR code, but never replace the human-readable URL and code.
4. Poll `device-token-request-url` sequentially—never overlap polls—with the returned client/device code and the backend's existing grant string.
5. Treat HTTP 428 as authorization pending, respect the server interval and expiry, support a future `slow_down` error, and abort immediately when the activation screen is destroyed.
6. On success, atomically persist access and refresh tokens, fetch identity, connect SignalR, fetch content, and enter display state.
7. On denial, expiry, malformed response, TLS/DNS/offline failure, or an unexpected status, show an actionable Retry/Generate New Code screen usable with the LG remote.

Use distinct configuration/storage fields for `device-token-request-url` and `device-refresh-token-request-url`. Do not copy the Android constant collision that currently stores both under one preference key.

### 6.3 Authentication, authorization, and token refresh

All REST calls go through one `ApiClient.request()` function:

- create URLs from validated configured endpoints, with query values built through `URL`/`URLSearchParams` or an equivalent compatible helper;
- attach `Accept: application/json` and JSON `Content-Type` where needed;
- attach `Authorization: Bearer <accessToken>` only for allow-listed trusted API origins;
- enforce request timeouts through `AbortController` or a compatibility abstraction;
- normalize HTTP, backend-code, JSON, timeout, abort, DNS, and offline failures;
- on the first 401, await one shared refresh operation so concurrent requests do not rotate the refresh token multiple times;
- call `device-refresh-token-request-url` using the backend-required refresh Authorization/body contract;
- atomically replace both tokens and retry the original request at most once;
- on refresh rejection, disconnect SignalR, clear invalid tokens, and route to activation;
- never log pairing codes, tokens, Authorization headers, secrets, or unredacted response bodies in production.

Do not send authorization on media, image, external-content, redirect, or hub origins merely because a token exists. Each trusted origin and credential type must be explicit.

### 6.4 Device identity and content resolution

1. `GET device-info-url` and persist backend `id`, `deviceName`, and `tenantId`. These values—not an LG hardware identifier—remain the application identity and SignalR grouping source.
2. `GET content-data-url`, then validate maximum size and the complete payload before replacing active content.
3. Accept the existing fields: display identity, `layout`, `menu`, `mediaAsset`, `textEditorData`, `externalMediaSource`, `checksum`, and `playlistData`.
4. Compare the checksum with the currently rendered checksum. Suppress a duplicate only if a valid renderer is active; an app restart/offline recovery must still render cached content even if its checksum equals the stored value.
5. Map only `MenuOverlay`, `MenuOnly`, `MediaOnly`, `Text`, `CurrentDateTime`, and `MediaPlaylist`. Unknown/null templates show a diagnostic status rather than silently retaining misleading old content.
6. Copy only recognized layout properties into a typed style model. Validate colors, clamp opacity, allow-list fonts, and validate date format.
7. Prepare the new renderer off-screen when practical, then atomically swap and destroy the old renderer to minimize flicker and decoder overlap.
8. Persist the checksum only after successful validation and renderer activation.

Keep the current JSON wire model so Android, Samsung Tizen, and LG webOS can use the same content control plane.

### 6.5 SignalR negotiation, hub connection, and registration

Use the locally bundled Microsoft SignalR JavaScript client. Confirm its emitted syntax and transport behavior on the oldest supported webOS engine.

1. `POST signalr-negotiation-url?deviceId=<encoded backend device id>` using the REST access token.
2. Validate the returned hub `url` and `accessToken` against trusted WSS/HTTPS origins.
3. Register `ReceiveChangeMessage` and close/reconnect handlers before calling `start()`.
4. Build a connection with `HubConnectionBuilder`, `accessTokenFactory`, and explicit reconnect delays. Prefer WebSockets. Use `skipNegotiation: true` only when the returned URL/token are for a direct WebSocket-capable SignalR endpoint and the transport is forced to WebSockets; otherwise allow the SignalR client negotiation stage.
5. After connecting, obtain `connection.connectionId` and `POST signalr-add-connection-url` with URL-encoded `deviceId`, `deviceName`, and `connectionId`. Store the current connection ID for best-effort cleanup.
6. If the backend still requires it, invoke `ManualKeepAlive` every ten seconds only in connected state. Maintain exactly one cancellable keep-alive timer.
7. On reconnect, re-negotiate if the negotiated hub token/URL may have expired and always register the new connection ID. Ensure only one connection attempt/retry loop exists.
8. On graceful exit/restart, bound the time allowed to `POST signalr-remove-connection-url`, stop the hub, then proceed. The server must remove stale entries by heartbeat/TTL because TVs can lose power.

Treat received strings as untrusted data. Enforce JSON size, required string fields, allowed message types, and device/tenant targeting. Queue command handling so publish, restart, and terminate cannot race. Debounce burst publishes and abort obsolete content fetches.

| `messageType` | Required webOS behavior |
| --- | --- |
| `device.info.update` | Refetch identity; update visible metadata and connection registration if grouping data changed. |
| `content.publish` | Fetch and validate content; rerender only when needed. |
| `app.restart` | Clean up and reload the packaged page, or use an approved application-manager method if available to the distribution profile. This restarts the player, not the TV. |
| `app.terminate` | Clean up and call the supported webOS exit path; do not create an automatic relaunch loop. |
| `app.upgrade.info` | Show a timed, accessible black upgrade banner. It must not sideload an arbitrary `.ipk`. |
| `operator.info` | Show a timed, accessible red operator banner. |

Implement the two banner commands fully; the Android method is currently a placeholder. Escape plain text, constrain length/duration, define replacement/queue rules, and preserve content playback behind the banner.

### 6.6 LG remote and Magic Remote interaction

Build a deterministic focus manager instead of relying on desktop tab order:

- arrow keys move among Retry, Generate Code, and other controls using explicit neighbor rules;
- OK/Enter activates the focused control;
- Back closes the top banner/modal first, otherwise performs the LG-required application exit behavior;
- media keys are optional for signage and should be handled only if exposed intentionally;
- pointer/click events from Magic Remote may mirror OK, but all functionality must remain D-pad accessible;
- suppress browser scrolling, selection, context menus, and accidental focus movement on display-only screens;
- restore logical focus after a modal or state change and render an obvious focus ring.

Key codes and Back handling can vary by webOS version and review requirements. Centralize them in `RemoteControl`, use documented APIs/constants for the target versions, and test standard and Magic Remotes on physical TVs.

### 6.7 Exit, reload, suspend, and failure recovery

Use the documented webOS application-exit mechanism available to the selected app type/version (commonly exposed through the webOS runtime environment), behind `WebOsLifecycle.exit()`. Do not scatter calls to proprietary globals across renderers.

On controlled reload/exit:

1. block new commands;
2. cancel content/activation requests and timers;
3. stop/unload video and external content;
4. attempt SignalR remove registration within a short timeout;
5. stop SignalR;
6. flush bounded telemetry where possible;
7. reload or exit.

Install global `error` and `unhandledrejection` handlers. After redacted reporting, transition to a local recovery screen or perform a rate-limited reload. Persist a reload-crash counter/window so a deterministic failure does not cause an endless flashing loop.

## 7. Renderer parity specification

### 7.1 `MediaOnly`

Preserve `externalMediaSource` precedence over `mediaAsset`.

#### Image (`mediaAsset.type == 1`)

- render with `<img>` and an explicit `object-fit: contain` or `cover` content policy;
- wait for decode/load before swapping to avoid a flash;
- constrain memory by releasing prior image references;
- define load timeout, retry, placeholder, and last-known-good behavior;
- test JPEG, PNG, WebP, orientation metadata, very large dimensions, and cache eviction on target TVs.

#### Video (`mediaAsset.type == 2`)

- use one owned HTML5 `<video>` element and `src` rather than uncontrolled nested players;
- set `autoplay`, `playsinline` where relevant, and signage-appropriate controls (normally none);
- listen for `loadedmetadata`, `canplay`, `playing`, `waiting`, `stalled`, `error`, `ended`, and time progress;
- loop standalone/overlay video, but include an `ended` fallback that resets/reloads if firmware looping is unreliable;
- apply a buffering watchdog and bounded recovery sequence;
- remove `src`, call `load()`, and remove listeners when destroyed to release the hardware decoder;
- validate every approved codec/container/audio profile on every supported model year.

Autoplay policies for packaged TV apps differ from desktop browsers but still require hardware testing. Content audio policy must be explicit; unattended signage commonly starts muted unless audio is intended and approved.

#### External source

An Android WebView cannot be replicated transparently. Prefer a backend-owned embeddable page in a constrained `<iframe>`. The remote site must allow framing and the widget CSP must allow it; `X-Frame-Options` and CSP `frame-ancestors` can block rendering. Apply the narrowest feasible iframe `sandbox`/`allow`, forbid untrusted navigation/popups, and provide timeout/failure UI. If secure embedding cannot be guaranteed, remove this content type from webOS capability negotiation instead of weakening the entire app.

### 7.2 `MenuOnly`

Build DOM rows/cards from menu title, currency, item name, description, icon, normal price, and optional discount price:

- `Premium` includes menu/item imagery and premium row styling;
- `Basic` provides the basic tabular/card presentation;
- `Deluxe` may initially map to Basic for current parity, but keep its factory key distinct;
- strike through only the original price and expose the discounted price clearly;
- apply validated `textFont`, `textColor`, and `backgroundColor` through CSS custom properties;
- provide deterministic overflow behavior: columns, pagination, scaling bounds, or timed pages—not browser scrollbars;
- respect overscan-safe areas and test 720p, 1080p, and 4K rendering.

### 7.3 `MenuOverlay`

Place the image/video layer below a Basic menu layer. Translate backend opacity into a clamped CSS alpha from 0 to 1 rather than reproducing Android's `opacity + 150` calculation. Verify that the webOS video compositing plane supports DOM overlay and opacity on every target model; hardware-decoded video can have z-order/compositing differences. If a model cannot overlay reliably, use an approved fallback layout or declare the combination unsupported.

### 7.4 `Text`

Render `textEditorData` with configured font, foreground, and background. Never place backend HTML directly into unrestricted `innerHTML`. Bundle a sanitizer compatible with the oldest engine and allow only necessary tags/attributes/protocols. Remove scripts, event handlers, forms, frames, CSS escape vectors, remote executable resources, and unsafe URLs. Bundle licensed fonts locally with fallbacks.

### 7.5 `CurrentDateTime`

Android receives a Java `SimpleDateFormat` pattern, which JavaScript `Intl.DateTimeFormat` does not accept directly. Choose one contract strategy:

1. implement and test a small allow-listed formatter for the Java pattern tokens the backend emits; or
2. migrate all players to structured fields for locale, timezone, date fields, time fields, and 12/24-hour mode.

Update on a wall-clock second boundary to prevent interval drift. Use the TV timezone unless content specifies an allowed IANA timezone. Render `Invalid Format` for an invalid pattern and report a redacted diagnostic.

### 7.6 `MediaPlaylist`

Use `playlistData.itemsSerialized` and preserve the current item keys:

- `AssetItemModel` parses to `MediaAssetDataModel`; images advance by timer and videos by `ended`;
- `TextAssetItemModel` parses to a sanitized rich-text asset and advances by timer;
- unknown/malformed entries are logged and skipped with a full-cycle guard so an all-invalid playlist cannot spin forever.

Parse exactly `HH:mm:ss`, validate ranges/overflow, and use 10 seconds when absent/invalid. Keep one cancellable item timer. Pause its remaining time or restart consistently across hide/show. Preload the next image and cautiously preload video metadata only when memory/decoder limits allow. A transition must release the old video decoder before starting another. Empty/no-valid playlists show actionable guidance and continue listening for `content.publish`.

## 8. Backend endpoint compatibility

The existing service can support webOS without platform-specific endpoints if it adds the browser-origin, TLS, and WebSocket behavior described below.

| Endpoint/configuration field | Method, data, and purpose |
| --- | --- |
| Bootstrap | `GET https://www.onscreensync.com/config.json`; returns `display-api` endpoint configuration. |
| `device-code-url` | `POST` JSON `clientId`, `grantType`; returns device/user code, verification URL, expiry, polling interval, device name, and client ID. |
| `device-token-request-url` | `POST` client/device/grant JSON; returns access/refresh token, expiry, scope, and token type. |
| `device-refresh-token-request-url` | `POST` the backend refresh-grant body and refresh Bearer header; returns replacement tokens. |
| `device-info-url` | Authenticated `GET`; returns backend device name, ID, and tenant ID. |
| `content-data-url` | Authenticated `GET`; returns layout, menu, media, external source, text, playlist, and checksum. |
| `signalr-negotiation-url` | Authenticated `POST` with encoded `deviceId`; returns hub URL/token. |
| `signalr-add-connection-url` | Authenticated `POST` with encoded device ID/name and hub connection ID; registers/groups the live player. |
| `signalr-remove-connection-url` | Authenticated `POST` with encoded device ID/name and connection ID; best-effort unregister. |
| SignalR hub | Connect using the negotiated URL/token; receive `ReceiveChangeMessage`; invoke `ManualKeepAlive` if required. |

### Required backend changes/verification for a web client

- Publish an OpenAPI specification plus JSON Schemas for bootstrap, content, errors, activation, and SignalR messages.
- Return formal error codes such as `no_such_device`, `no_screen_id`, and `no_screen_data_found` in a stable JSON envelope; do not require parsing an exception string.
- Configure CORS for the effective packaged webOS application origin, including OPTIONS preflight, methods, `Authorization`, and `Content-Type`. Determine the actual Origin header on each supported webOS generation rather than broadly allowing every origin.
- Do not combine wildcard origins with credentials. Bearer auth should not require cookies unless intentionally designed.
- Ensure all proxies/load balancers preserve WebSocket upgrade, idle timeout, and SignalR affinity requirements.
- Serve complete certificate chains and TLS/cipher suites compatible with the oldest supported TVs while remaining secure.
- Allow required API/image/media/frame/connect origins in application CSP and package policy.
- Set correct media MIME types, byte-range support, caching headers, and cross-origin behavior required by TV playback.
- Rate-limit pairing, refresh, negotiate, and registration endpoints without blocking legitimate reconnect fleets.

Prefer HTTPS/WSS exclusively. Do not reproduce Android's cleartext traffic allowance.

## 9. webOS package metadata, APIs, and services

### 9.1 `appinfo.json`

Define and validate at least the fields required by the selected webOS TV SDK/tool version, including:

- stable reverse-domain application `id`;
- semantic application `version`;
- `vendor`, `title`, and `type` for a web app;
- `main` entry document;
- icons and optional large icon/splash assets;
- supported resolution settings where applicable;
- required permissions only when an invoked API/service actually requires them.

Do not copy a broad sample manifest. Metadata and permitted fields vary across webOS versions and consumer/commercial profiles. Validate with current LG tooling and on the minimum target TV.

### 9.2 Luna service calls

webOS exposes selected system capabilities over Luna service APIs, often called through an LG-provided JavaScript bridge. Availability and permission differ by platform version and app trust level. Build a `WebOsServiceBridge` that:

- allow-lists service URI/method pairs;
- normalizes success/error/cancellation/timeout;
- retains and cancels subscription handles;
- degrades gracefully when a service is unavailable;
- never treats undocumented or private services as production contracts.

Use service calls only when required for documented lifecycle, network, system, or product metadata features. The backend device-code record remains authoritative. Do not use a hardware ID as a credential, assume it is available to public apps, or block activation if LG device metadata is unavailable.

### 9.3 Content Security Policy and network access

Ship a restrictive CSP tailored to the final hosts:

- `default-src 'self'`;
- narrow `connect-src` for bootstrap/API/SignalR HTTPS and WSS hosts;
- narrow `img-src`, `media-src`, `font-src`, and `frame-src`;
- no `unsafe-eval`; avoid `unsafe-inline` by bundling scripts and using stylesheets/nonces where supported;
- no public CDN dependency.

Dynamic tenant/environment hosts complicate a static CSP. Prefer a known host allow-list or shared controlled domains. Do not solve configuration flexibility by allowing every network origin.

## 10. Persistence, offline mode, and asset caching

### Parity storage

Persist small values in versioned `localStorage` records:

- validated endpoint configuration and fetch timestamp;
- access/refresh tokens;
- backend device name, ID, and tenant ID;
- active/last SignalR connection ID;
- last successfully rendered checksum;
- crash/reload guard and schema/application version.

Use IndexedDB for last-known-good content and optional bounded image/text cache. Keep tokens out of URLs and IndexedDB content records. Local web storage is application-private in normal operation but is not equivalent to a hardware-backed secret store; rely on short access-token lifetime, refresh rotation/revocation, server-side device controls, and package/platform security.

### Offline behavior

1. Always start the local shell.
2. If bootstrap/content is unavailable, render the last fully validated content if it is within policy and its required assets can load.
3. Show a subtle offline indicator only if product requirements allow it; avoid replacing usable signage with a transient network error.
4. Retry with capped exponential backoff/jitter and react quickly to confirmed network restoration.
5. Never overwrite last-known-good data with a partial or invalid response.
6. Cache only bounded assets and enforce version, maximum bytes/items, least-recently-used eviction, and tenant isolation.
7. Do not claim offline video support unless the platform permits managed download/storage and content licensing; ordinary browser cache is not a guaranteed offline video package.

Test localStorage/IndexedDB quotas, persistence across app/TV upgrade and power cycles, private-data removal on uninstall/reset, and low-storage eviction on real devices.

## 11. Security and privacy blueprint

- Enforce HTTPS/WSS and validate every bootstrap/configured URL against scheme, origin, redirect, and credential policies.
- Restrict Authorization headers to explicit trusted API origins and prevent leakage through cross-origin redirect.
- Use single-flight refresh, rotate refresh tokens, revoke removed devices, and authorize tenant/device on every backend request.
- Sanitize rich text and operator messages; validate message size, enum, URL, format, CSS color/font, opacity, media type, and duration.
- Sandbox external frames and accept only approved origins. Refuse content that requires disabling the application security boundary.
- Use a strict CSP, locally bundled dependencies, locked versions, dependency scanning, reproducible builds, and production source-map policy.
- Redact logs and telemetry. Pairing codes, tokens, content containing private data, device identifiers, and authorization headers must not be logged.
- Never embed a confidential client secret in TypeScript or `.ipk`; packaged web applications are inspectable.
- Keep developer keys/signing material outside Git and build logs. Separate development, staging, and production signing/release credentials.
- Document user/tenant data collected for store privacy review and provide retention/deletion behavior.
- Apply a maximum content/message size and bounded queues to protect limited TV memory.

## 12. Reliability, watchdogs, and fleet observability

### Player reliability

- use one SignalR connection, one keep-alive timer, one active renderer, one playlist timer, and one active video decoder;
- cap every retry/backoff and add jitter to prevent a fleet reconnect storm;
- add network, render-start, buffering, video-progress, and content-load watchdogs;
- recover a failed renderer without discarding the last-known-good manifest;
- rate-limit full page reloads and retain crash-loop state;
- pause work while hidden and revalidate after long suspension;
- regularly release detached DOM, images, listeners, timers, and media resources;
- tolerate the absence of graceful cleanup on power/network loss.

### Redacted telemetry

Capture application version/build, LG model family, webOS/firmware version when permitted, session ID, bootstrap/API latency and status class, device connectivity state, active layout/checksum, renderer/media error category, buffering duration, SignalR state/reconnect count/last command, memory proxy metrics where available, and reload/crash guard state.

Backend fleet health should expose last REST contact, last SignalR heartbeat, active checksum/layout, player version, connection ID, and current error category. Expire SignalR group records by heartbeat/TTL. `app.upgrade.info` may instruct the operator, but a public web app must not download and install an arbitrary `.ipk`; upgrades follow LG Content Store or the approved managed distribution channel.

## 13. Development environment and local workflow

Use the current LG-supported webOS TV CLI/IDE workflow for the chosen SDK. LG tooling has evolved from the historical `ares-*` CLI/IDE to newer webOS tooling, so pin the toolchain version in developer/CI documentation and verify the current official recommendation.

Typical development stages are:

1. Install Node.js version pinned by the project and the current LG webOS TV CLI/extension.
2. Install the **Developer Mode** app from LG Content Store on a physical consumer TV, sign in with the required LG developer account, enable Developer Mode/Key Server, and register the TV with the CLI using the TV address and generated key workflow.
3. Generate or hand-create the packaged web-app shell and valid `appinfo.json`.
4. Run the web build and desktop tests; serve in a desktop browser with fake platform adapters for rapid UI work.
5. Package `dist` into `.ipk` (historically `ares-package`), install it to the registered TV (`ares-install`), launch it (`ares-launch`), and inspect/debug it (`ares-inspect`) or use current equivalent commands.
6. Renew Developer Mode before its session expires; do not confuse this temporary installation path with production deployment.
7. Test through controlled local/staging backend configurations without putting credentials into the bundle.

Example command names are illustrative because current CLI syntax/tool branding can change:

```bash
npm ci
npm run lint
npm test
npm run build
ares-package dist
ares-install --device <tv-name> <generated-package>.ipk
ares-launch --device <tv-name> <application-id>
ares-inspect --device <tv-name> --app <application-id>
```

Record exact working commands, CLI/SDK versions, TV Developer Mode steps, certificate/signing configuration, and troubleshooting in the new project's README.

## 14. Packaging, distribution, and upgrade

### Consumer distribution

- create the required LG developer/seller accounts;
- register application identity and product metadata;
- produce a release `.ipk` using the approved tool/signing workflow;
- satisfy LG Content Store quality, UX, remote, media, security, privacy, country, and device compatibility review;
- provide icons, screenshots, descriptions, privacy/support URLs, and testing credentials/instructions;
- stage rollout by supported country/model where the channel permits;
- preserve application ID and storage migration compatibility across upgrades.

### Commercial signage distribution

Coordinate with LG/partner guidance for webOS Signage, SuperSign, hospitality, or enterprise deployment. Document provisioning, package trust/signing, auto-launch, remote upgrade, rollback, configuration injection, device reset, and replacement workflows. Do not assume consumer `.ipk` deployment commands apply unchanged.

### Versioning and rollback

Embed semantic version, build ID, backend contract version, and compatible storage schema. Migrate storage transactionally. A failed migration must retain or restore the prior last-known-good content. Define rollback limits because stores and managed systems may not permit arbitrary downgrade. Test upgrade from every supported deployed version while preserving pairing.

## 15. Testing strategy

### 15.1 Automated unit/component tests

- endpoint and configuration schema validation, malicious URL rejection, and expiry/fallback;
- activation pending, slow-down, denial, expiry, cancellation, retry, malformed JSON, and success;
- concurrent 401 single-flight refresh, token rotation, refresh failure, and one-retry limit;
- device/content schema validation and checksum activation rules;
- template mapping and every renderer's null/error/cleanup behavior;
- color/font/opacity/date/duration allow-list and parsing;
- HTML/message sanitization and external-frame policy;
- playlist timing, wrap, all-invalid guard, video completion, hide/show, and cancellation;
- SignalR validation, command serialization, publish debounce, reconnection, re-registration, and timer uniqueness;
- state transitions, focus restoration, Back behavior, crash guard, and offline last-known-good selection;
- platform adapter behavior through fakes without proprietary globals.

### 15.2 Desktop and emulator/simulator tests

- responsive 720p, 1080p, and 4K layouts with safe areas;
- D-pad keyboard simulation, focus graph, OK/Back, and banner/modal behavior;
- mock API/SignalR server covering latency, failure, malformed payloads, and command races;
- lifecycle visibility and offline/online transitions;
- image/text/menu/clock snapshots where stable;
- CSP/CORS behavior representative of the packaged application.

Desktop playback is not evidence of LG decoder compatibility.

### 15.3 Mandatory physical-TV tests

- oldest and newest models in every supported family/region;
- cold launch, warm launch, long suspend/resume, app close, TV standby, hard power cycle, and approved auto-launch;
- standard remote plus Magic Remote pointer, D-pad, OK, Back, and any media keys;
- every allowed JPEG/PNG/WebP and video container/codec/profile/level/resolution/frame-rate/bitrate/audio combination;
- autoplay/audio, buffering, looping, `ended`, decoder error, media URL expiry, z-order overlay, and decoder release;
- a 24–72 hour mixed playlist/SignalR soak with repeated content changes and memory observation;
- Wi-Fi/Ethernet removal, DNS failure, captive/no-internet network, router reboot, server restart, proxy idle close, malformed response, TLS/certificate failure, and recovery;
- access-token expiry during REST calls, refresh rotation, SignalR token expiry/reconnect, pairing revocation, and tenant/device removal;
- CORS/preflight and WSS through production load balancers;
- install, Developer Mode expiration, production upgrade retaining pairing, storage migration, uninstall/reinstall, factory reset, and rollback process;
- long menus/text, missing media, invalid playlist, status screens, operator/upgrade banners, and rapid publish bursts.

## 16. Definition of parity and acceptance criteria

Replication is complete only when:

1. A fresh LG TV installation can bootstrap, display a pairing URL/code, complete activation, persist credentials, and restore its session after restart.
2. The same backend payload renders all six layout keys with equivalent data, styling, timing, and error behavior.
3. Images, approved videos, menus, overlays, sanitized text, clocks, and playlists work across the declared model matrix.
4. All existing REST operations use correct authentication, refresh once on 401, and expose formal backend errors.
5. SignalR negotiates, registers, receives every supported command, keeps alive, reconnects/re-registers, and cleans up best-effort without duplicate connections/timers.
6. Every flow is operable with an LG remote, and Magic Remote support does not become a requirement.
7. Network/backend/media failures recover unattended while preserving valid last-known content.
8. Security review confirms origin restrictions, CSP, HTML sanitization, token/log handling, package integrity, and external-content policy.
9. A 24–72 hour physical-TV soak meets defined memory, recovery, and availability targets.
10. Installation, auto-launch expectations, update, rollback, and fleet monitoring are proven through the chosen consumer or commercial distribution channel.

## 17. Suggested implementation phases

1. **Discovery:** choose consumer/commercial profile, model matrix, distribution, auto-launch contract, media profiles, and current LG toolchain.
2. **Foundation:** webOS package, TypeScript build, adapters, state machine, remote focus, local shell, test/CI framework.
3. **Backend and identity:** canonical bootstrap, schema validation, activation, auth/refresh, device identity, formal errors.
4. **Static content:** image, menus, sanitized text, clock, styling rules, safe-area/overflow behavior.
5. **Media and playlists:** HTML5 player, codec matrix, overlay compositing, watchdogs, playlist, resource release.
6. **Real time:** SignalR negotiation, hub, registration, serialized commands, keep-alive, reconnect, visible banners.
7. **Hardening:** offline last-known-good, storage migration, telemetry, crash guard, security/privacy review, failure injection.
8. **Qualification:** physical model matrix, long soak, CORS/TLS/load balancer verification, auto-launch and managed operations.
9. **Distribution:** store/enterprise submission, signed release, staged rollout, support/rollback runbooks, fleet dashboard.

## 18. Primary implementation references

Use current official documentation throughout development because LG changes supported platform versions, tools, APIs, Developer Mode, packaging, and store requirements:

- [LG webOS TV Developer documentation](https://webostv.developer.lge.com/)
- [LG webOS TV: Getting Started](https://webostv.developer.lge.com/develop/getting-started)
- [LG webOS TV: Web app development](https://webostv.developer.lge.com/develop/getting-started/build-your-first-web-app)
- [LG webOS TV: App metadata (`appinfo.json`)](https://webostv.developer.lge.com/develop/references/appinfo-json)
- [LG webOS TV: Media playback](https://webostv.developer.lge.com/develop/guides/multimedia/media-playback)
- [LG webOS TV: Developer Mode app](https://webostv.developer.lge.com/develop/getting-started/developer-mode-app)
- [LG webOS TV CLI](https://webostv.developer.lge.com/develop/tools/cli-installation)
- [Microsoft ASP.NET Core SignalR JavaScript client](https://learn.microsoft.com/aspnet/core/signalr/javascript-client)
- [MDN: Content Security Policy](https://developer.mozilla.org/docs/Web/HTTP/CSP)
- [MDN: HTMLMediaElement](https://developer.mozilla.org/docs/Web/API/HTMLMediaElement)

Validate each URL, command, metadata field, service URI, permission, API, codec, and packaging rule against the selected consumer or commercial SDK and minimum webOS version before coding. This is a comprehensive architecture and delivery blueprint; access to a feature on desktop, simulator, another webOS product, or an undocumented Luna service is not proof that LG permits or supports it on the target TVs.

## 19. Standalone implementation contract

This section makes the blueprint implementable **without access to the Android repository**. Earlier references to “current,” “existing,” or “Android” behavior mean the contracts reproduced below. An LG webOS team should be able to define its models, mock server, application states, and acceptance fixtures entirely from this section. Where the backend behavior is ambiguous, the ambiguity is explicitly identified rather than delegated to inspection of another codebase.

### 19.1 Fixed bootstrap location and configuration model

The only fixed production URL is:

```text
GET https://www.onscreensync.com/config.json
```

Expected JSON shape:

```json
{
  "display-api": {
    "base-endpoint": "https://display-api.example/",
    "device-code-url": "/device/code",
    "device-info-url": "/device/info",
    "device-token-request-url": "/device/token",
    "device-refresh-token-request-url": "/device/token/refresh",
    "content-data-url": "/display/content",
    "signalr-negotiation-url": "/signalr/negotiate",
    "signalr-add-connection-url": "/signalr/add",
    "signalr-remove-connection-url": "/signalr/remove",
    "messages": {
      "app-title": "Screen Service"
    }
  }
}
```

Endpoint values are nullable in the source contract but are operationally required for the corresponding feature. They can be absolute URLs or encoded route values understood by the deployed API. The new implementation must resolve a relative value against `base-endpoint`, preserve an absolute HTTPS value, reject invalid/downgrade URLs, and never let stored `base-endpoint` rewrite the canonical bootstrap request. Store token and refresh-token routes under separate keys.

### 19.2 Activation and token wire models

Device-code request:

```http
POST <device-code-url>
Content-Type: application/json

{"clientId":"clientid","grantType":"user_code"}
```

Device-code response:

```json
{
  "deviceCode": "opaque-device-code",
  "userCode": "ABCD-EFGH",
  "verificationUrl": "https://example.com/activate",
  "expiresIn": 900,
  "interval": 5,
  "deviceName": "Lobby TV",
  "clientId": "returned-client-id"
}
```

Activation token polling request:

```http
POST <device-token-request-url>
Content-Type: application/json

{
  "clientId": "<clientId from device-code response>",
  "clientSecret": "string",
  "deviceCode": "<deviceCode from device-code response>",
  "grantType": "urn:ietf:params:oauth:grant-type:access_token"
}
```

Poll sequentially every `interval` seconds. HTTP `428` means authorization pending. Stop on success, non-428 failure, cancellation, 100 attempts, or `expiresIn`, whichever happens first. Supporting OAuth-style `slow_down` is a recommended compatible enhancement, not a presently proven response.

Token response used by both initial authorization and refresh:

```json
{
  "accessToken": "access-token",
  "expiresIn": 3600,
  "scope": "display",
  "tokenType": "Bearer",
  "refreshToken": "refresh-token"
}
```

Refresh request contract:

```http
POST <device-refresh-token-request-url>
Authorization: Bearer <refreshToken>
Content-Type: application/json

{
  "clientId": "",
  "clientSecret": "string",
  "deviceCode": "",
  "grantType": "refresh_token"
}
```

All other protected REST requests use `Authorization: Bearer <accessToken>`. Retry an original request only once after refresh.

### 19.3 Device and content wire models

Device response:

```json
{
  "deviceName": "Lobby TV",
  "tenantId": "tenant-123",
  "id": "device-456"
}
```

Complete content envelope, shown with every supported field:

```json
{
  "id": "display-1",
  "tenantId": "tenant-123",
  "displayName": "Lobby Display",
  "layout": {
    "templateKey": "MediaOnly",
    "subType": "",
    "templateProperties": [
      {"key": "textColor", "value": "#FFFFFF", "label": "Text color"}
    ]
  },
  "menu": null,
  "mediaAsset": {
    "type": 1,
    "assetUrl": "https://media.example/image.jpg",
    "description": "Campaign image",
    "name": "Campaign"
  },
  "textEditorData": null,
  "externalMediaSource": null,
  "checksum": "opaque-content-checksum",
  "playlistData": null
}
```

Type definitions:

```ts
type TemplateKey =
  | "MenuOverlay" | "MenuOnly" | "MediaOnly"
  | "Text" | "CurrentDateTime" | "MediaPlaylist";

interface TemplateProperty { key: string | null; value: string | null; label: string | null; }
interface Layout { templateKey: TemplateKey | string | null; subType: string | null; templateProperties: TemplateProperty[] | null; }
interface MediaAsset { type: number; assetUrl: string | null; description: string | null; name: string | null; }
interface MenuItem {
  name: string | null; description: string | null; iconUrl: string | null;
  price: string | null; discountPrice: string | null;
  createdOn: string | null; updatedOn: string | null;
}
interface Menu {
  name: string | null; description: string | null; title: string | null;
  currency: string | null; iconUrl: string | null;
  createdOn: string | null; updatedOn: string | null; menuItems: MenuItem[] | null;
}
interface PlaylistItemSerialized { key: string | null; value: string | null; }
interface PlaylistData { itemDuration: string | null; items: unknown[] | null; itemsSerialized: PlaylistItemSerialized[] | null; }
interface ContentData {
  id: string | null; tenantId: string | null; displayName: string | null;
  layout: Layout | null; menu: Menu | null; mediaAsset: MediaAsset | null;
  textEditorData: string | null; externalMediaSource: string | null;
  checksum: string | null; playlistData: PlaylistData | null;
}
```

`mediaAsset.type` is `1` for image and `2` for video. Other values are unsupported. `externalMediaSource`, when non-empty in `MediaOnly`, takes precedence over `mediaAsset`.

### 19.4 Layout input matrix

| `templateKey` | Required/used content | `subType` | Recognized properties |
| --- | --- | --- | --- |
| `MediaOnly` | `externalMediaSource` or `mediaAsset` | Not used | None currently required. |
| `MenuOnly` | `menu.menuItems`; menu metadata | `Premium`, `Deluxe`, or `Basic`; unknown defaults to Basic | `textColor`, `textFont`, `backgroundColor`. |
| `MenuOverlay` | Menu plus image/video `mediaAsset` | Menu metadata carries it, but renderer is Basic | `textColor`, `textFont`, `backgroundOpacity`. |
| `Text` | `textEditorData` HTML | Not used | `textColor`, `textFont`, `backgroundColor`. |
| `CurrentDateTime` | No content asset | Date/time format; empty default is `EEE, d MMM yyyy HH:mm:ss` | `textColor`, `textFont`. |
| `MediaPlaylist` | `playlistData.itemsSerialized` and `itemDuration` | Not used | Per-text-item styling is embedded in serialized JSON. |

Missing layout or unknown `templateKey` must show: title `No Layout Key`, message `Layout Key is not set, update screen and republish`. Empty text falls back to `Error: No text found in the data, republish.` An empty playlist shows `No item in the playlist, please add items and republish.` These strings may be localized, but their states must remain distinct.

### 19.5 Menu rendering contract

Display the menu title when non-empty. For each item show name, description, and price. Prefix prices with `menu.currency` without inserting an extra separator. When `discountPrice` is non-empty, display `<currency><price> <currency><discountPrice>` and strike through only the first price. Premium additionally displays `menu.iconUrl` and each `item.iconUrl`; Basic/Deluxe do not require icons. Null arrays render the content-status state instead of throwing.

Fonts and colors are backend-provided strings but must pass the platform implementation's allow-list. The legacy opacity representation is not formally specified; accept a documented numeric range at the new boundary, clamp it, and add backend contract tests rather than reproducing platform-specific alpha arithmetic.

### 19.6 Playlist serialized values

Only `itemsSerialized` drives playback; the parallel `items` field is not needed.

Image/video entry:

```json
{
  "key": "AssetItemModel",
  "value": "{\"type\":1,\"assetUrl\":\"https://media.example/item.jpg\",\"description\":\"\",\"name\":\"Item\"}"
}
```

Text entry:

```json
{
  "key": "TextAssetItemModel",
  "value": "{\"name\":\"Notice\",\"description\":\"<b>Welcome</b>\",\"backgroundColor\":\"#000000\",\"textColor\":\"#FFFFFF\",\"textFont\":\"sans-serif\",\"playlistType\":0}"
}
```

`itemDuration` is `HH:mm:ss`; missing/malformed input defaults to `00:00:10`. Images and text advance on that timer. Video ignores the shared timer and advances on completion. Items loop circularly. Skip invalid/unknown items with a full-cycle guard.

### 19.7 SignalR REST and message contract

All three management operations are authenticated POSTs with query parameters and no required request body:

```text
POST <signalr-negotiation-url>?deviceId=<url-encoded device id>
  -> {"url":"https://hub.example/client/?...","accessToken":"hub-token"}

POST <signalr-add-connection-url>?deviceId=<...>&deviceName=<...>&connectionId=<...>
  -> successful empty object/body is sufficient

POST <signalr-remove-connection-url>?deviceId=<...>&deviceName=<...>&connectionId=<...>
  -> successful empty object/body is sufficient
```

The hub sends a single string argument through target method `ReceiveChangeMessage`. Parse that string as:

```ts
interface SignalRReceivedMessage {
  deviceId: string | null;
  tenantId: string | null;
  messageType: string | null;
  messageData: string | null;
  messageStatus: string | null;
}
```

Supported `messageType` values and behavior are fully specified in section 6.5. The client invokes hub method `ManualKeepAlive` with no arguments every ten seconds while connected. Server timeout target is ten minutes; reconnect begins after five seconds in the baseline behavior. New implementations should add jitter and prevent duplicate reconnect loops.

### 19.8 Required local state and deterministic startup pseudocode

Persist these independent keys (names are implementation choices): access token, refresh token, endpoint config, device name, device ID, tenant ID, last connection ID, active/last checksum, configuration schema/timestamp, and optional last-known-good content.

```ts
async function startPlayer(): Promise<void> {
  showLocalSplash();
  const config = await bootstrapWithValidatedFallback();
  if (!storage.accessToken) return showActivation(config);
  try {
    const device = await api.getDevice(config);
    storage.saveDevice(device);
    await Promise.allSettled([
      signalR.connect(config, device),
      content.loadValidateAndRender(config)
    ]);
  } catch (error) {
    if (isUnrecoverableAuthentication(error)) {
      await signalR.disconnect();
      storage.clearTokens();
      return showActivation(config);
    }
    showLastKnownContentOrStatus(error);
  }
}
```

### 19.9 Backend questions that must be resolved, not guessed

The models above are sufficient to build against mocks, but production integration must obtain definitive answers for:

- whether configured route values are relative, absolute, or mixed in each environment;
- exact CORS origin emitted by a packaged webOS `.ipk` application;
- formal non-success JSON envelope and mapping for `no_such_device`, `no_screen_id`, and `no_screen_data_found`;
- whether the negotiated URL is direct WebSocket and therefore valid with `skipNegotiation`;
- refresh-token rotation and whether a missing replacement refresh token retains or invalidates the old one;
- allowed media origins, MIME types, byte ranges, URL expiry, codecs, maximum payload/asset sizes, and offline rights;
- `backgroundOpacity` units/range and supported font/date format values;
- whether `deviceId`/`tenantId` in a SignalR envelope must exactly match locally stored identity;
- CORS, CSP domain allow-list, TLS minimums, reconnect rate limits, and stale-connection TTL;
- public-store versus enterprise deployment, auto-launch, telemetry/privacy, and upgrade policy.

Develop using contract fixtures for the documented happy/error cases while these environment decisions are finalized. None of these questions requires Android source access; they require backend and LG webOS deployment owners.
