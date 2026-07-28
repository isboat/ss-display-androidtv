# Samsung Tizen TV replication plan

## 1. Objective

This document is a technical blueprint for reproducing the capabilities of the existing Screen Service Android TV player as a **Samsung Smart TV application running on Tizen**. The goal is functional parity—not a line-for-line port—because Android activities, fragments, Hilt, Retrofit, `VideoView`, boot receivers, and APKs do not exist on Tizen.

The recommended target is a **Tizen TV Web Application** packaged as a `.wgt`. Its interface is HTML/CSS, its application logic is JavaScript or TypeScript compiled to browser-compatible JavaScript, and Samsung Product APIs provide TV-specific media and device functionality. This approach matches the existing system well: the display is primarily a remotely configured full-screen renderer backed by HTTP, WebSockets/SignalR, timers, and persistent key/value state.

This plan preserves these behaviours:

- remote `config.json` bootstrap and environment-specific endpoints;
- device-code pairing and token polling;
- access-token persistence and refresh;
- device identity and content retrieval;
- full-screen images, video, external web content, menus, rich text, clocks, and playlists;
- checksum-based suppression of redundant publishes;
- SignalR negotiation, device group registration, live commands, keep-alive, and reconnect;
- Samsung remote-control navigation, error/status screens, and controlled exit/restart;
- deployable development, test, certificate, packaging, and release workflows.

## 2. Recommended Tizen technology stack

| Tizen implementation choice | Android counterpart | Why it is appropriate |
| --- | --- | --- |
| Tizen TV Web Application (`.wgt`) | Android application (`.aab`/APK) | Samsung's HTML/CSS/JavaScript application model is well suited to a signage renderer and is considerably more portable than a native Tizen C application. |
| TypeScript + a bundler such as Vite/Rollup/Webpack | Kotlin/JVM | TypeScript supplies models and compile-time checks; bundling produces JavaScript compatible with each supported TV browser engine and packages dependencies locally. |
| HTML screens/components + CSS Grid/Flexbox | Activities, fragments, XML layouts, View Binding | A single-page state machine can switch full-screen views without stale navigation history and can create reusable image/video/menu/text components. |
| Fetch API with a small `ApiClient` | Retrofit + OkHttp | Handles JSON REST calls, configurable full URLs, Bearer headers, timeouts, error mapping, and a single refresh-and-retry path. |
| `@microsoft/signalr` JavaScript client, bundled locally | Microsoft SignalR Java client | Implements the same hub protocol and WebSocket connection from JavaScript. Pin and test a version whose generated syntax is supported by the oldest target TV. |
| Samsung AVPlay (`webapis.avplay`) | Android `VideoView` | Samsung's TV-optimized playback API offers streaming, buffering, events, display rectangles, and better codec/DRM/device integration than relying only on an HTML `<video>` element. |
| `<img>` plus browser cache | Picasso | Loads hosted signage images; an optional Cache API/IndexedDB asset layer can add deterministic offline caching. |
| DOM/CSS menu and text renderer | Basic/Premium menu and text fragments | Supports fonts, colors, opacity, HTML sanitization, prices, strike-through discounts, icons, and responsive 16:9 positioning. |
| `localStorage` for small settings; IndexedDB for structured/offline data | `SharedPreferences` | Persists tokens, discovered routes, identity, checksum, and optionally the last valid content manifest. Sensitive-token risk must be accepted or mitigated because there is no direct equivalent of Android encrypted preferences in a normal TV Web App. |
| Tizen Application and TVInputDevice APIs | Android lifecycle and remote keys | Handles show/hide/terminate lifecycle and Samsung remote-control keys. |
| Jest/Vitest and jsdom; Playwright where browser-compatible; real-TV tests | Gradle unit/instrumentation tests | Separates pure state/API tests from Samsung API adapter tests and hardware validation. |

Do not load core libraries from a public CDN. Signage must still start when that CDN is unavailable, and TV Content Security Policy or certificate/network constraints can reject remote scripts. Bundle SignalR and all application code into the widget.

## 3. Platform constraints to decide before implementation

### 3.1 TV model years and Tizen versions

Samsung TV capabilities and web-engine versions differ by model year. Define the oldest supported model/Tizen version before choosing JavaScript syntax, CSS features, codecs, TLS settings, SignalR package version, and media formats. Transpile TypeScript to that engine, include only required polyfills, and test on both the Samsung TV Simulator/Emulator and physical oldest/newest devices.

Maintain a compatibility matrix containing at least: model, model year, Tizen version, web engine, supported resolution, codec/container/audio combinations, AVPlay behaviours, WebSocket stability, TLS compatibility, memory limits, and firmware version.

### 3.2 Distribution mode

Choose one of the following early because privileges, signing, installation, and auto-launch differ:

1. **Public Samsung TV Seller Office application:** normal consumer distribution and review; must comply with store UX, privacy, security, and remote-control requirements.
2. **Partner/enterprise distribution:** appropriate for managed signage fleets and may provide capabilities not available to public apps, subject to Samsung approval and certificates.
3. **Developer-mode sideload:** suitable only for development/testing; widgets are signed and installed from Tizen Studio/CLI to a TV whose Developer Mode points to the workstation.

### 3.3 Startup after power-on

Android's exported boot receiver cannot be ported. A normal Tizen Web App cannot assume it may silently launch itself at every boot. Consumer TVs can expose **Autorun Last App**, but that is a user/device setting and behaviour varies by model/region. Guaranteed kiosk boot/auto-launch may require Samsung hospitality/signage hardware, MagicINFO, an enterprise/partner arrangement, or external device management.

Treat automatic launch as a deployment capability with a tested fallback, not as an ordinary Web API call. At minimum, document how installers enable Autorun Last App and ensure the Screen Service player is the last running app before shutdown. Confirm final requirements with Samsung for the chosen distribution channel.

## 4. Proposed project structure

Create a separate repository or top-level project rather than mixing `.wgt` output into the Android Gradle module. A suitable layout is:

```text
ss-display-tizen/
├── config.xml                    # Tizen widget ID, profile, privileges, CSP
├── package.json
├── tsconfig.json
├── vite.config.ts                # or equivalent bundler configuration
├── public/
│   ├── icon.png
│   └── fonts/
├── src/
│   ├── index.html
│   ├── main.ts
│   ├── app/
│   │   ├── AppController.ts      # top-level lifecycle/state machine
│   │   ├── Router.ts             # replaces Activity navigation
│   │   └── AppState.ts
│   ├── api/
│   │   ├── ApiClient.ts          # Fetch, auth, refresh, timeout, retry
│   │   ├── EndpointConfig.ts
│   │   ├── AuthApi.ts
│   │   ├── DeviceApi.ts
│   │   ├── ContentApi.ts
│   │   └── SignalRRegistrationApi.ts
│   ├── realtime/
│   │   ├── SignalRService.ts
│   │   └── MessageDispatcher.ts
│   ├── media/
│   │   ├── AvPlayAdapter.ts
│   │   ├── ImageRenderer.ts
│   │   └── PlaylistController.ts
│   ├── screens/
│   │   ├── SplashScreen.ts
│   │   ├── ActivationScreen.ts
│   │   ├── StatusScreen.ts
│   │   ├── ErrorScreen.ts
│   │   └── ContentScreen.ts
│   ├── renderers/
│   │   ├── MediaOnlyRenderer.ts
│   │   ├── MenuOnlyRenderer.ts
│   │   ├── MenuOverlayRenderer.ts
│   │   ├── TextRenderer.ts
│   │   ├── ClockRenderer.ts
│   │   └── PlaylistRenderer.ts
│   ├── platform/
│   │   ├── TizenLifecycle.ts
│   │   ├── RemoteControl.ts
│   │   ├── DeviceInfo.ts
│   │   └── Storage.ts
│   ├── models/
│   ├── styles/
│   └── utils/
└── test/
```

Use interfaces around `webapis`, `tizen`, storage, timers, and networking. Browser test doubles can then exercise most logic without a TV, while the production adapters contain Samsung-specific calls.

## 5. Application state machine

Unlike Android's activity-per-layout navigation, implement a single document with explicit states:

```text
BOOTSTRAP -> ACTIVATION -> RESOLVING_CONTENT -> DISPLAYING
     |            |              |                |
     +----------> ERROR/STATUS <------------------+
```

`AppController` owns the active state and abort/cleanup handles. `Router.show(state)` hides or destroys the current screen, mounts the next screen into one root element, and focuses its default control. On every transition it must stop AVPlay, clear playlist/clock timers, abort obsolete fetches, and remove DOM/event listeners. SignalR should be owned at application scope rather than by the short-lived content router, avoiding the existing Android pattern where a singleton callback captures a finished activity.

## 6. Detailed application flows

### 6.1 Launch, lifecycle, and bootstrap

1. `index.html` loads the bundled entry script and displays a local splash immediately; startup must not depend on network imagery.
2. Register Tizen lifecycle handlers and remote keys before beginning asynchronous work.
3. Read the last endpoint configuration from storage only as a fallback; request `GET https://www.onscreensync.com/config.json` from the canonical host so stale dynamic base data cannot rewrite the bootstrap host.
4. Validate that `display-api` contains valid HTTPS URLs for every required operation. Store config atomically only after validation.
5. If bootstrap fails, optionally use a non-expired last-known configuration, show an offline/retry status, and retry with capped exponential backoff plus jitter.
6. If no access token exists, show activation. Otherwise retrieve device identity, then resolve content. If credentials are rejected and refresh cannot recover, clear the invalid tokens and return to activation.

Handle Tizen visibility/lifecycle events: pause video and nonessential timers when hidden, resume/revalidate when shown, and remove the backend SignalR connection plus close AVPlay on termination. Do not assume a termination callback always completes during power loss; the backend must also expire stale connections.

### 6.2 Device-code activation

Replicate the current contract:

1. `POST device-code-url` with JSON `{ "clientId": "clientid", "grantType": "user_code" }`.
2. Render `verificationUrl`, `userCode`, and optional device name in large, high-contrast text suitable for viewing at TV distance. A QR code may be added if the backend permits it, but keep the literal URL/code for accessibility and recovery.
3. Poll `device-token-request-url` using the response `clientId`, `deviceCode`, client-secret placeholder, and current grant string. Use `interval`, respect `expiresIn`, cancel polling when leaving the screen, and treat HTTP 428 as pending.
4. Slow down if the service returns an equivalent of `slow_down`; do not launch overlapping poll requests.
5. On success, atomically store access/refresh tokens, retrieve device information, start SignalR, and load content.
6. Show actionable error/retry UI for expiry, denial, invalid client, service failure, DNS/TLS failure, or loss of connectivity.

Correct the Android preference-key collision in the Tizen implementation: `device-token-request-url` and `device-refresh-token-request-url` must be distinct fields.

### 6.3 Authenticated HTTP and refresh

Build one `ApiClient.request()` wrapper:

- resolve full configured URL without rewriting it from stale storage;
- set `Accept: application/json` and JSON `Content-Type` where applicable;
- add `Authorization: Bearer <accessToken>` only to trusted configured origins;
- use `AbortController` timeouts;
- parse successful and backend error bodies into typed results;
- on the first 401, enter a shared single-flight refresh promise so concurrent calls make only one refresh request;
- send the refresh token exactly as the backend requires, persist both replacement tokens, then retry the original request once;
- if refresh fails, clear tokens, disconnect SignalR, and route to activation;
- never log tokens, authorization headers, pairing codes, or unredacted response bodies in production.

If the backend supports it, evolve toward standards-based OAuth device authorization (`authorization_pending`, `slow_down`, and expiry semantics) and avoid placeholder secrets in a distributable client.

### 6.4 Device and content resolution

1. `GET device-info-url`; persist `id`, `deviceName`, and `tenantId`.
2. `GET content-data-url`; validate the payload before rendering.
3. Compare `checksum` with the active checksum. Ignore a repeat publish only when a valid renderer is already active. Retain last-known content for an optional offline mode.
4. Map `layout.templateKey` to a renderer. Unknown/missing keys go to a status screen instead of leaving old content ambiguously active.
5. Apply only allow-listed template properties (`textFont`, `textColor`, `backgroundColor`, `backgroundOpacity`, and supported clock format). Clamp opacity and validate colors/fonts.
6. Construct the new renderer off-screen where possible, then swap it into the root to reduce flashes.

Keep the backend `ContentDataApiResponse` wire shape unchanged so one control plane can serve both Android and Tizen players.

### 6.5 SignalR connection and command processing

SignalR should retain the existing two-stage design because it gives the backend control over the actual hub URL/token and lets it address a specific TV:

1. `POST signalr-negotiation-url?deviceId=<encoded id>` with REST access-token authentication.
2. Create `new signalR.HubConnectionBuilder().withUrl(url, { accessTokenFactory, transport: WebSockets, skipNegotiation: true })` **only if** the returned URL is a direct SignalR WebSocket-capable hub endpoint. Otherwise allow the library's negotiation stage. Match this detail to the existing backend during integration testing.
3. Prefer WebSockets for low latency. If older target TVs or network proxies make WebSockets unreliable, test Server-Sent Events/long polling support instead of assuming transport parity.
4. Register `connection.on("ReceiveChangeMessage", handler)` before `start()` to avoid missing an early message.
5. After connection, obtain `connection.connectionId` and `POST signalr-add-connection-url` with URL-encoded `deviceId`, `deviceName`, and `connectionId`.
6. Send `ManualKeepAlive` every ten seconds only while connected if the backend still requires this application-level invocation. SignalR itself also provides keep-alive/timeout mechanisms; coordinate values with the server.
7. Use `withAutomaticReconnect()` with explicit backoff delays, and re-negotiate/re-register after every new connection ID. Ensure only one reconnect loop and one keep-alive timer exist.
8. On graceful shutdown, `POST signalr-remove-connection-url`, then stop the connection. Bound cleanup time so exit is not blocked indefinitely.

Parse each message as an untrusted JSON envelope and verify required types, device/tenant targeting, allowed message type, and maximum message size. Serialize command handling to prevent `content.publish` and `app.restart` races.

| SignalR message | Tizen behaviour |
| --- | --- |
| `device.info.update` | Refetch identity, update the on-screen name if relevant, and re-register the connection if grouping fields changed. |
| `content.publish` | Fetch content; rerender only when checksum/content changed. Debounce bursts and cancel an obsolete fetch. |
| `app.restart` | Perform cleanup and reload the Web App (`location.reload()`) or use the approved Tizen application relaunch approach. A page reload is not a TV reboot. |
| `app.terminate` | Clean up and call the supported Tizen application exit API; provide no silent relaunch loop. |
| `app.upgrade.info` | Show a visible, timed, accessible black information banner. |
| `operator.info` | Show a visible, timed, accessible red operator banner. |

Unlike the Android placeholder, both information messages should be fully implemented. Queue or replace banners predictably and escape message text.

### 6.6 Remote control and focus

Register only the Samsung remote keys the application needs. Handle at least:

- **Back/Return:** close a modal/banner first; otherwise confirm or perform cleanup and exit. Samsung certification rules for the Return/Exit behaviour must be followed.
- **Enter:** activate Retry or other focused controls.
- **Arrow keys:** move focus on activation/error/status controls; presentation screens normally have no interactive focus.
- **Media keys:** optional and normally suppressed for unattended signage; if supported, map them through the playlist/AVPlay controller.

Never require touch or pointer input. Make focus visibly obvious, keep pairing/retry controls operable with directional keys, and prevent the browser's default spatial scrolling from moving full-screen signage.

## 7. Renderer-by-renderer parity

### 7.1 MediaOnly

- If `externalMediaSource` exists, it retains precedence.
- Image (`type == 1`): use `<img>` with a deliberate `object-fit` policy (`contain` or `cover`) agreed with content authors, decode before showing, and display fallback status on load failure.
- Video (`type == 2`): open the URL with AVPlay, set the display rectangle to the intended region, register buffering/error/completion listeners, prepare asynchronously, and play. On completion seek/reopen as required for a seamless standalone loop.
- Unknown/null types produce a status message and telemetry rather than a blank display.

### 7.2 MenuOnly

Render title, currency, items, normal/discount prices, descriptions, and icons using semantic DOM nodes. `Premium` includes menu and item imagery/styled borders; `Basic` and the currently equivalent `Deluxe` use the basic renderer. Use CSS classes rather than inline style proliferation, apply server colors/fonts through validated CSS custom properties, and strike through only the original price when a discount exists.

Support long menus deterministically: fit/scaling rules, columns, pagination, or timed pages must be selected rather than allowing an off-screen browser scroll. Test overscan-safe margins and 720p/1080p/4K layouts.

### 7.3 MenuOverlay

Place the media renderer in a lower z-index layer and the basic menu above it. Convert backend opacity to a clamped 0–1 CSS alpha, rather than reproducing Android's `opacity + 150` alpha arithmetic. AVPlay is rendered in a native video plane on many TVs, so verify DOM overlay support and layering on each target model; Samsung AVPlay display mode/window configuration may be required.

### 7.4 Text

Render rich text, foreground/background colors, and font. Do **not** assign backend HTML directly with unrestricted `innerHTML`; sanitize it with a locally bundled, TV-compatible sanitizer and an allow-list of required tags/attributes. Bundle approved fonts where licensing permits and define fallbacks.

### 7.5 CurrentDateTime

The Android backend subtype is a Java `SimpleDateFormat` string, which is not directly compatible with JavaScript `Intl.DateTimeFormat`. Either:

1. keep a small allow-listed Java-pattern formatter for supported tokens; or
2. preferably evolve the shared contract to named/structured locale, date, time, and timezone options.

Update on the next second boundary, not merely every 1000 ms from an arbitrary start, to limit drift. Use TV local timezone unless the content contract explicitly specifies one. Invalid formats must render `Invalid Format` and produce diagnostic telemetry.

### 7.6 MediaPlaylist

Parse `itemsSerialized` keys exactly as today:

- `AssetItemModel`: image uses the configured timer; video advances on AVPlay completion;
- `TextAssetItemModel`: sanitized rich text uses the configured timer;
- unsupported or invalid items are logged and skipped with a loop guard.

Parse `itemDuration` as strict `HH:mm:ss`, reject negative/overflow values, and default to 10 seconds. Preload the next image and, where practical, prepare the next video without exceeding TV memory. Maintain one cancellable timer, advance circularly, clear stale AVPlay listeners between items, and show the existing empty-playlist guidance when there are no valid entries.

## 8. Backend endpoint compatibility

The Tizen player can reuse the existing backend without introducing platform-specific routes. All query components must be URL encoded.

| Endpoint | Tizen request and purpose |
| --- | --- |
| `GET https://www.onscreensync.com/config.json` | Bootstrap the `display-api` base and route values. Request directly from the canonical origin. |
| `POST device-code-url` | Obtain device/user codes, verification URL, polling interval, expiry, device name, and client ID. |
| `POST device-token-request-url` | Poll device authorization and receive access/refresh tokens. |
| `POST device-refresh-token-request-url` | Exchange refresh credentials for replacement tokens and retry one failed request. |
| `GET device-info-url` | Obtain and persist device name, tenant ID, and backend device ID. |
| `GET content-data-url` | Obtain layout/menu/media/text/playlist data and checksum. |
| `POST signalr-negotiation-url?deviceId=…` | Obtain hub URL and hub access token. |
| `POST signalr-add-connection-url?deviceId=…&deviceName=…&connectionId=…` | Register/group the current SignalR connection. |
| `POST signalr-remove-connection-url?deviceId=…&deviceName=…&connectionId=…` | Remove the current or last known connection registration. |

Before implementation, publish an OpenAPI specification for the REST API and JSON Schema for content and SignalR envelopes. The current client code reveals status assumptions but not a formal error-body contract. Define status/error codes—especially `no_such_device`, `no_screen_id`, and `no_screen_data_found`—so both platforms show the same correct status rather than trying to infer it from a generic HTTP exception.

### Cross-origin and transport requirements

A Tizen Web App is subject to Web security rules that do not affect Retrofit in the same way. The backend must therefore:

- allow the packaged application's effective origin under its CORS policy;
- allow `Authorization` and `Content-Type` request headers and required methods;
- answer OPTIONS preflight requests where generated;
- permit credentials only if actually used (Bearer auth normally does not require cookies);
- support WebSocket upgrade through every proxy/load balancer;
- serve a certificate chain and TLS/cipher versions compatible with all target TV model years;
- authorize bootstrap API, media, image, and hub domains in `config.xml` access/CSP rules.

Prefer HTTPS/WSS exclusively. Do not replicate Android's broad clear-text allowance.

## 9. Tizen widget configuration and privileges

Create the widget with the Samsung TV profile in Tizen Studio and configure:

- stable application/package IDs and version;
- TV profile metadata, name, icon, and 16:9 launch screen;
- network access declarations for the canonical and configured hosts (as constrained by the chosen Tizen version);
- Content Security Policy permitting only required `connect-src`, `img-src`, `media-src`, font, and style sources;
- the Internet/network privilege required by the target TV profile;
- Samsung product privileges only for APIs actually called, such as TV input-device registration or product information;
- minimum platform/version consistent with the compatibility matrix.

Privilege URIs and availability vary between Tizen platform APIs and Samsung Product APIs. Obtain exact entries from the API reference for the selected minimum version rather than copying an unrestricted sample manifest. Avoid broad `*` network access in production. Access to a stable Samsung device identifier (for example a DUID through ProductInfo where supported) requires the corresponding product API/privilege; the existing backend device-code identity should remain authoritative, so hardware identity should be telemetry/diagnostic input rather than an authentication secret.

## 10. Storage, offline behaviour, and recovery

At parity level, store the same values as Android: endpoint config, access/refresh token, device name/ID/tenant, last connection ID, and checksum. Add schema/version fields and transactional helpers so a deployment upgrade can migrate or clear incompatible state.

Recommended recovery enhancements for unattended signage:

- retain the last validated content JSON and show it when content refresh temporarily fails;
- cache bounded image/text assets using Cache API or IndexedDB where supported;
- do not claim a video is offline-capable unless it has been explicitly downloaded under platform/storage/content-license constraints;
- retain timestamps and maximum ages for configuration/content;
- use exponential backoff with jitter and reset it after a healthy connection;
- provide a remote-safe reset gesture or managed command to clear pairing state;
- never erase last-known-good content merely because a refresh returned malformed JSON.

Test storage quotas and eviction on real models. `localStorage` operations are synchronous, so keep payloads small; use IndexedDB for manifests/assets.

## 11. Security requirements

- Enforce HTTPS/WSS and an origin allow-list. Reject bootstrap endpoints that downgrade transport or target an unexpected host unless explicitly provisioned.
- Keep refresh single-flight and restrict Bearer headers to trusted origins; never forward authorization when following an untrusted redirect.
- Sanitize backend HTML and message text; validate JSON sizes, enum values, URLs, colors, formats, durations, and opacity.
- Use a strict CSP without `unsafe-eval`; verify whether the selected SignalR bundle requires any relaxation and choose/build it accordingly.
- Redact production logs and add a compile-time debug flag. Do not expose codes/tokens in remote telemetry or screenshots.
- Treat local tokens as bearer credentials. Provide backend revocation, short access-token lifetimes, refresh rotation, device removal, and tenant/device checks on every endpoint.
- Sign production widgets using the correct Samsung author/distributor certificate workflow. Protect private keys outside source control and CI logs.
- Review external web media carefully. Embedding arbitrary pages in the main document is risky and CSP-hostile; prefer a controlled iframe with the narrowest feasible sandbox/allow policy, or disallow external sources that cannot meet it.

## 12. Observability and fleet operations

Add structured, redacted events for application version, TV model/Tizen version, bootstrap, activation state, API latency/status class, content checksum/layout, renderer/media errors, SignalR state/reconnect count, command processing, and memory/restart signals. Batch and retry telemetry so it never blocks rendering.

Define fleet health semantics on the backend: last API contact, last SignalR heartbeat, currently rendered checksum, player version, media failure, and connection ID. Because remove-connection is best-effort, expire group records by heartbeat/time-to-live. `app.upgrade.info` should only inform unless a signed/approved Samsung update channel exists; a Web App cannot self-install an arbitrary new widget like a downloaded executable.

## 13. Development, packaging, and deployment workflow

1. Install **Tizen Studio**, then use Package Manager to add the Samsung TV extensions/tools required for the chosen platform versions.
2. Create Samsung and Tizen developer accounts as required. Use Certificate Manager to create/import the correct author and distributor certificates. Back up the author certificate securely; changing it affects upgrade identity.
3. Create a TV Web Application project/profile and implement the platform adapters and static shell first.
4. For physical testing, put the TV and workstation on the same network, enable Developer Mode on the TV, enter the workstation IP, restart the TV if requested, connect it in Device Manager/SDB, sign, and install/run the widget.
5. Build TypeScript/assets, run tests and lint, stage only production files, validate `config.xml`, then use Tizen tooling to package a signed `.wgt`.
6. Test cold launch, resume, network loss, power cycle, token expiry, clock/timezone, media codecs, WebSocket proxy behaviour, remote keys, overscan, long-duration memory use, and firmware variations on real TVs.
7. Submit through Samsung TV Seller Office or the approved enterprise channel. Complete Samsung certification, content-rating/privacy declarations, screenshots, supported-country/model selections, and staged rollout procedures.

Never ship a development distributor certificate, private key, source map containing secrets, debug endpoint, or BODY-level API logging.

## 14. Testing strategy and acceptance criteria

### Automated tests

- schema/config validation and rejection of malicious endpoint URLs;
- activation polling for pending, slow-down, expiry, denial, success, cancellation, and network errors;
- single-flight token refresh with concurrent 401 responses and one retry maximum;
- content mapping for every template, missing/null fields, and checksum behaviour;
- strict playlist duration parsing, invalid-item skip guard, timer cancellation, and wraparound;
- SignalR message validation, serialization, debounce, reconnect/re-registration, and timer uniqueness;
- rich-text sanitization, CSS property allow-listing, and URL/origin validation;
- renderer cleanup and application state transitions using fake Samsung adapters.

### Simulator/emulator tests

- remote focus/navigation and Back behaviour;
- responsive 720p/1080p/4K layouts and overscan areas;
- lifecycle hide/show and application reload;
- mocked REST/SignalR state changes and offline status.

### Required physical-TV soak tests

- all supported model-year endpoints of the compatibility matrix;
- image formats and every approved video codec/container/resolution/bitrate/audio combination;
- AVPlay overlay/z-order, completion, buffering, loop, and failure recovery;
- a 24–72 hour mixed playlist and SignalR session with memory/CPU observations;
- Wi-Fi/Ethernet disconnect, router reboot, DNS failure, TLS error, backend restart, WebSocket close, and TV standby/power cycle;
- token expiry during REST and SignalR operation;
- install, upgrade while retaining pairing, rollback policy, and factory/reset recovery;
- Autorun Last App or approved kiosk boot behaviour for every deployment model.

Parity is achieved when a paired Tizen TV can render every existing content template, reacts correctly to every supported SignalR message, recovers from transient failures unattended, is fully operable with the Samsung remote, and passes the selected Samsung distribution/certification route.

## 15. Suggested delivery phases

1. **Foundation:** supported-model matrix, Tizen project/certificates, TypeScript build, platform adapters, CI tests, splash/status screens.
2. **Backend/authentication:** bootstrap, validated endpoint config, activation, secure HTTP client, refresh, device identity.
3. **Static renderers:** image, menus, text, clock, styling validation and HTML sanitization.
4. **Media:** AVPlay adapter, video lifecycle, menu overlay/z-order, playlist and preloading.
5. **Real time:** SignalR negotiation, group registration, commands, keep-alive, reconnect, banner UX.
6. **Hardening:** offline last-known-good mode, telemetry, memory/error recovery, security review, physical-TV soak matrix.
7. **Distribution:** store/enterprise compliance, signed widget, staged device rollout, operational dashboards and rollback runbook.

## 16. Primary platform references

Use the current official documentation while implementing because Samsung changes tooling, privileges, model support, and certification requirements:

- [Samsung Smart TV: Installing the TV SDK](https://developer.samsung.com/smarttv/develop/getting-started/setting-up-sdk/installing-tv-sdk.html)
- [Samsung Smart TV: Creating TV applications](https://developer.samsung.com/smarttv/develop/getting-started/using-sdk/tv-applications.html)
- [Samsung Product API: AVPlay](https://developer.samsung.com/smarttv/develop/api-references/samsung-product-api-references/avplay-api.html)
- [Samsung Smart TV: Using AVPlay](https://developer.samsung.com/smarttv/develop/guides/multimedia/media-playback/using-avplay.html)
- [Samsung Product API: TVInputDevice](https://developer.samsung.com/smarttv/develop/api-references/samsung-product-api-references/tvinputdevice-api.html)
- [Samsung Smart TV: Managing certificates](https://developer.samsung.com/smarttv/develop/getting-started/setting-up-sdk/creating-certificates.html)
- [Tizen Web application fundamentals](https://docs.tizen.org/application/web/)
- [Microsoft SignalR JavaScript client](https://learn.microsoft.com/aspnet/core/signalr/javascript-client)

Validate links and the exact API/privilege requirements against the selected target version at implementation time. This document is an architecture plan; Samsung approval, device capabilities, and backend CORS/TLS behaviour must be proven rather than assumed.
