# Screen Service Android TV Display

Screen Service Android TV Display is the OnScreenSync digital-signage player for Android TV. It pairs a television with a backend-managed device, retrieves the screen assigned to that device, renders its media or layout full-screen, and responds to real-time publishing and management commands through SignalR.

This repository also contains standalone implementation blueprints for creating equivalent players on Samsung Tizen TV and LG webOS TV.

## Features

- Remote bootstrap configuration from `https://www.onscreensync.com/config.json`.
- Device-code activation using a verification URL and user code.
- Persistent access and refresh tokens with automatic Bearer authentication and HTTP 401 refresh/retry.
- Backend-managed device name, device ID, and tenant identity.
- Full-screen image, looping video, and external web content.
- Basic, Deluxe, and Premium menu presentations with pricing, discounts, icons, colors, fonts, and media overlays.
- Rich-text/HTML information screens.
- Configurable date and time displays updated once per second.
- Circular image, video, and rich-text playlists.
- Checksum-based suppression of redundant content reloads.
- SignalR WebSocket negotiation, device-group registration, remote commands, reconnect, and manual keep-alive.
- Automatic launch after supported Android boot broadcasts.
- Android TV Leanback launcher support with no touchscreen requirement.

## Supported content layouts

The backend selects a presentation through `layout.templateKey`:

| Template key | Presentation |
| --- | --- |
| `MediaOnly` | A full-screen image, looping video, or external URL. |
| `MenuOnly` | A Basic, Deluxe, or Premium full-screen menu. |
| `MenuOverlay` | A Basic menu over an image or looping video. |
| `Text` | Backend-provided rich text with configurable colors and font. |
| `CurrentDateTime` | A live clock using a backend-provided format. |
| `MediaPlaylist` | A repeating playlist of images, videos, and rich-text items. |

## Runtime flow

1. `MainActivity` downloads the display API configuration and stores the operational endpoint URLs.
2. If the TV has no access token, `CodeActivationActivity` displays a user code and verification URL, then polls until the operator authorizes the device.
3. The player retrieves and stores the backend device identity.
4. `ContentActivity` downloads the assigned content and routes it to the activity/renderer matching its layout key.
5. The SignalR client negotiates a WebSocket connection, registers the connection against the device, and listens for remote commands.
6. A `content.publish` command retrieves and displays changed content. Other commands can update device information, restart the player, terminate it, or deliver operator/upgrade information.

See [the detailed Android project overview](doc/project-overview.md) for complete flows, endpoint contracts, technologies, persistence behavior, and implementation observations.

## Technology stack

- Kotlin 1.9.22 and Java/JVM 17
- Android SDK 34, with minimum API 21
- AndroidX Leanback, AppCompat, Lifecycle, and ConstraintLayout
- MVVM with repository-based data access
- Kotlin coroutines, `StateFlow`, and `SharedFlow`
- Hilt dependency injection
- Retrofit, OkHttp, and Gson
- Microsoft SignalR Java client
- Picasso image loading
- Android `VideoView`, WebView, and XML layouts with View Binding
- `SharedPreferences` persistence
- Gradle Kotlin DSL and Android Gradle Plugin 8.2.2

## Repository layout

```text
.
├── README.md
├── doc/
│   ├── project-overview.md
│   ├── samsung-tizen-project-blueprint.md
│   └── lg-webos-project-blueprint.md
├── playstore-images/              # Store listing artwork
└── src/                           # Android Gradle project
    ├── app/
    │   ├── build.gradle.kts
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/onscreensync/tvapp/
    │       └── res/
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── gradlew
```

Important Android packages under `src/app/src/main/java/com/onscreensync/tvapp/` include:

| Package/directory | Responsibility |
| --- | --- |
| Top-level activities | Startup, activation, content routing, and specialized full-screen presentations. |
| `apirequests/` | Retrofit REST endpoint declarations and request bodies. |
| `apiresponses/` | Bootstrap, activation, token, device, and content response models. |
| `datamodels/` | Layout, menu, media, playlist, text, and SignalR message models. |
| `di/` | Hilt application and network dependency providers. |
| `fragments/` | Reusable image, video, web, menu, and text renderers. |
| `network/` | Authorization, dynamic endpoint, and token-refresh handling. |
| `repository/` | Authentication, device, content, and SignalR operations. |
| `services/` | Local persistent storage. |
| `signalR/` | Hub connection management and registration API contracts. |
| `viewmodels/` | Lifecycle-aware startup, activation, content, and playlist state. |

## Documentation

| Document | Audience and purpose |
| --- | --- |
| [Android project overview](doc/project-overview.md) | Maintainers of this application. Documents features, architecture, all application flows, backend endpoints, SignalR, technology choices, and known implementation nuances. |
| [Samsung Tizen project blueprint](doc/samsung-tizen-project-blueprint.md) | Samsung TV implementers. A standalone Tizen Web Application blueprint with complete wire contracts, AVPlay/SignalR guidance, packaging, security, and test criteria. It does not require access to the Android source. |
| [LG webOS project blueprint](doc/lg-webos-project-blueprint.md) | LG TV implementers. A standalone packaged webOS application blueprint with complete wire contracts, media/SignalR guidance, distribution, security, and test criteria. It does not require access to the Android source. |

## Prerequisites

- JDK 17
- Android SDK Platform 34 and Build Tools 34.0.0
- An `ANDROID_HOME` or `ANDROID_SDK_ROOT` environment variable pointing to the installed SDK, or `sdk.dir` in `src/local.properties`
- Network access for the first Gradle dependency download
- Android Studio with Android TV tooling, or the Gradle wrapper for command-line builds
- An Android TV device/emulator for runtime validation

Do not commit `local.properties`, signing keys, service credentials, or production tokens.

## Build and verification

The Gradle project is under `src/`. The wrapper file is not executable in every checkout, so the portable examples invoke it through Bash:

```bash
cd src

# Run local unit-test tasks
bash ./gradlew test

# Build a debug APK
bash ./gradlew assembleDebug

# Run Android lint
bash ./gradlew lint
```

Build artifacts are generated under `src/app/build/outputs/`. Install the debug build with Android Debug Bridge after connecting a TV or emulator:

```bash
adb install -r src/app/build/outputs/apk/debug/app-debug.apk
```

If running the command from inside `src/`, remove the initial `src/` path component.

## Development notes

- The Retrofit bootstrap base URL is `https://www.onscreensync.com/`; operational paths and the display API base are supplied by `config.json` and persisted locally.
- Authenticated OkHttp requests receive an access-token Bearer header. A token authenticator uses the stored refresh token after a 401 and retries the request when refresh succeeds.
- Media type `1` means image and type `2` means video.
- SignalR listens for the hub target `ReceiveChangeMessage` and sends `ManualKeepAlive` every ten seconds while connected.
- Most display activities use `noHistory` because stale router/presentation screens should not accumulate in the TV Back stack.
- The application currently stores tokens in private, unencrypted `SharedPreferences` and enables clear-text traffic and BODY-level HTTP logging. Review these settings before a production security release.
- `DEVICE_TOKEN_REQUEST_URL` and `DEVICE_REFRESH_TOKEN_REQUEST_URL` currently share one preference string constant. The detailed overview and platform blueprints call out this behavior; new implementations should use separate keys.
- The manifest references `ServiceUnavailableActivity`, but this repository snapshot does not contain its Kotlin activity class.

## Real-time commands

The application recognizes these SignalR message types:

| Message type | Action |
| --- | --- |
| `device.info.update` | Refresh stored device identity. |
| `content.publish` | Retrieve current content and render it if its checksum changed. |
| `app.restart` | Disconnect and relaunch the application from its bootstrap screen. |
| `app.terminate` | Disconnect and close the application task. |
| `app.upgrade.info` | Route upgrade information to the notification hook. |
| `operator.info` | Route operator information to the notification hook. |

The current notification hook for the final two messages is a placeholder and should be completed before relying on those commands for operator-visible alerts.

## Contributing

1. Review [the project overview](doc/project-overview.md) before changing authentication, endpoint configuration, routing, or SignalR behavior.
2. Keep API models aligned with the backend wire contract and update the relevant documentation when it changes.
3. Prefer lifecycle-aware coroutines and flows for asynchronous Android work.
4. Keep reusable presentation logic in fragments or focused renderer components.
5. Do not wrap imports in `try`/`catch` blocks.
6. Run unit tests, a debug build, and lint before submitting changes when an Android SDK is available.
7. Test visible signage changes on an Android TV device/emulator, including remote behavior and long-running media playback.

## Cross-platform development

The Samsung and LG blueprints are self-contained specifications rather than summaries. Each includes bootstrap and authentication payloads, TypeScript wire models, layout inputs, playlist serialization, SignalR contracts, local-state requirements, startup pseudocode, packaging guidance, security requirements, and physical-TV acceptance tests. A platform team can build against mocks from its blueprint without access to this Android source tree.

Backend and deployment owners must still confirm environment-specific concerns such as production endpoint origins, formal error envelopes, CORS, TLS compatibility, media profiles, SignalR proxy configuration, store/enterprise distribution, and guaranteed auto-launch capabilities.
