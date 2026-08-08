# OnScreenSync Samsung Tizen TV

A full-screen Samsung TV Web Application based on `doc/samsung-tizen-project-blueprint.md`. It bootstraps dynamic API routes, pairs a device, persists and refreshes credentials, resolves content, and renders media, menus, rich text, or clocks. Samsung AVPlay is used on a TV; browsers use an HTML video fallback for development.

## Requirements

- Node.js 20.19+ or 22.12+
- npm 11+
- Tizen Studio with the TV extensions, Samsung certificate profile, and a Tizen 5.5+ TV/emulator for `.wgt` packaging

## Develop and verify

```bash
npm ci
npm run dev        # http://localhost:5173
npm run check      # lint, unit tests, type-check, production build
```

`npm run build` writes a self-contained application to `dist/`, including `config.xml`. To create a signed widget, import this folder as an existing Tizen Web project, select your Samsung certificate profile, then use **Build Signed Package**. CLI users can run `tizen build-web -- .` and `tizen package -t wgt -s <profile> -- <build-output>`.

## Configuration and deployment

The fixed bootstrap URL is `https://www.onscreensync.com/config.json`; all operational routes are validated as HTTPS. Update the widget/application IDs in `config.xml` before Seller Office submission. `access origin="*"` is required for dynamically configured customer endpoints; narrow it when deployment origins are known. Never commit certificate files.

Enable **Autorun Last App** on managed TVs where supported, launch this app once, and validate power-cycle behaviour on every target model. Consumer Tizen does not guarantee boot auto-launch. Maintain a hardware matrix for codecs, AVPlay overlay behaviour, TLS, WebSockets, resolution, and firmware.

## Architecture

- `AppController` owns the explicit bootstrap/activation/content state flow.
- `ApiClient` is the single authenticated HTTP boundary and performs one shared refresh.
- `StorageService` isolates persistent state.
- `ContentRenderer` sanitizes rich text and maps templates to presentation components.
- `AvPlayAdapter` isolates Samsung APIs from browser-testable application code.
- `Router` owns screen replacement and media cleanup.

The SignalR service negotiates, registers the device, maintains keep-alives/reconnects, and dispatches supported remote commands. Backend-specific serialized playlist item mapping remains behind the renderer boundary until real API samples are available. Content remains available in storage for a future offline policy; credentials are never logged.

## Remote control

Enter activates the focused retry button. Return/Back exits through the Tizen application API. The interface uses TV-safe sizing, high contrast, visible focus, and no pointer-only actions.
