# Screen Service Android TV application: project overview

## 1. Purpose and scope

This repository contains **Screen Service TV App** (`com.onscreensync.tvapp`), an Android TV digital-signage player for OnScreenSync. It is the display-side client: an operator assigns content in the remote service, while this application pairs the television to a device record, downloads the assigned screen definition, renders it full-screen, and reacts to live management events. The app supports Android 5.0/API 21 and later, targets API 34, and identifies the current release as version 2.0 (version code 3).

The application is intended to be appliance-like rather than an interactive consumer app. It exposes a Leanback launcher entry, does not require a touchscreen, suppresses navigation history for its display screens, exits the task when Back is pressed, and can launch after the TV boots. Internet and network-state access are declared, and clear-text traffic is currently allowed so that a dynamically supplied endpoint can be HTTP as well as HTTPS.

## 2. Feature inventory

### Device and operational features

- **Remote bootstrap configuration.** At startup the player downloads `config.json`. This makes all display API and SignalR route values server-controlled rather than compiled into the application.
- **Device-code activation.** An unpaired TV shows a short user code and verification URL. The operator completes activation on another device while the TV polls for tokens.
- **Persistent sign-in.** Access and refresh tokens survive process and TV restarts in local preferences. Authenticated REST calls receive a Bearer token automatically, and HTTP 401 responses can trigger token refresh and retry.
- **Device identity.** The player obtains and stores the backend device ID, display name, and tenant ID. The name is shown while content is being resolved and is also used for SignalR group membership.
- **Automatic startup.** `BootReceiver` launches `MainActivity` for `BOOT_COMPLETED`, `REBOOT`, and vendor `QUICKBOOT_POWERON` broadcasts.
- **Remote, real-time control through SignalR.** The app refreshes published content, refreshes device metadata, restarts, terminates, and receives upgrade/operator information events without polling the content endpoint continuously.
- **Resilience.** SignalR negotiation/connection failures are retried after five seconds, a manual keep-alive is sent every ten seconds, and the hub server timeout is ten minutes.
- **Content de-duplication.** A backend checksum is persisted. During the lifetime of a `ContentViewModel`, an unchanged publication is ignored after the first successful load to avoid unnecessary activity replacement and visible flicker.
- **Status and error displays.** The UI distinguishes missing devices, missing screen assignment, missing screen data, invalid layouts, activation/network errors, and empty playlists.

### Supported presentation types

The backend's `layout.templateKey` selects one of the following presentations:

| Template key | Functionality |
| --- | --- |
| `MediaOnly` | Full-screen hosted image (`mediaAsset.type == 1`), looping hosted video (`type == 2`), or an external URL in an embedded WebView. An external URL takes precedence over a media asset. |
| `MenuOnly` | Full-screen menu containing title, item name, description, currency, normal price, and optional discounted price. `Premium` shows menu/item images and styled rows; `Basic` and `Deluxe` use the basic table presentation. |
| `MenuOverlay` | Basic transparent/partially opaque menu over an image or looping video. |
| `Text` | Backend-provided HTML-compatible formatted text with configurable font, foreground color, and background color. |
| `CurrentDateTime` | A clock updated once per second using the backend-provided Java `SimpleDateFormat` pattern (with a default pattern); invalid patterns display `Invalid Format`. |
| `MediaPlaylist` | Circular playback of serialized image, video, and rich-text items. Images/text advance after the shared `HH:mm:ss` item duration (ten seconds when absent/invalid), while a video advances on completion. Unknown item/media types are skipped. |

Images are downloaded and cached through Picasso. Videos use Android `VideoView` and `MediaController`; standalone and overlay videos loop, whereas playlist videos deliberately play once. External media uses a WebView with JavaScript and built-in zoom enabled. Menu template properties control font, text/background color, and overlay opacity. Discounted menu prices display the original price struck through.

## 3. Architecture and source organization

The code follows a pragmatic **MVVM plus Repository** structure:

1. **Activities/fragments (View):** activities own navigation and screen composition; reusable fragments render image, video, external web content, basic/premium menus, and text information. Android View Binding provides type-safe access to XML views.
2. **ViewModels:** `MainViewModel`, `CodeActivationViewModel`, `ContentViewModel`, and `PlaylistViewModel` retain flow state and run asynchronous work in `viewModelScope`.
3. **Repositories:** `ContentRepository`, `DeviceRepository`, `AuthRepository`, and `SignalRRepository` isolate remote operations and return Kotlin `Result` values.
4. **API/data models:** Retrofit interfaces describe REST requests; Gson annotations map wire JSON to Kotlin models; `Parcelable` models carry content between activities/fragments.
5. **Infrastructure:** Hilt modules create singleton storage, OkHttp, Retrofit, API interfaces, repositories, and the real-time manager.

`StateFlow` represents lasting state such as a code, URL, device name, or empty-playlist flag. `SharedFlow` represents one-shot navigation, error, content, and playlist-item events. Coroutines move network work to `Dispatchers.IO` and return observations to lifecycle-aware collectors (`repeatOnLifecycle(STARTED)`).

Most content activities are `noHistory`, so the bootstrap/router screens do not remain as stale Back-stack entries. Content is passed to a specialized activity through Intent extras; layout `templateProperties` are copied by key/value, allowing server-defined visual options without a new fixed Intent schema.

## 4. End-to-end application flows

### 4.1 Installation, launcher, and TV boot flow

`TvApp` initializes the Hilt component. The Leanback launcher opens `MainActivity`. Independently, `BootReceiver` starts the same activity in a new task after supported boot broadcasts. `MainActivity` displays the splash view, starts configuration loading, and requests the boot-completed permission on Android M+ (although `RECEIVE_BOOT_COMPLETED` is a manifest permission, not normally a runtime permission).

### 4.2 Bootstrap and route selection

1. Retrofit starts with `https://www.onscreensync.com/` and requests `GET config.json`.
2. The `display-api` object is decoded and its base URL, content/device/token URLs, and three SignalR management URLs are persisted.
3. When loading succeeds, the splash waits three seconds and calls `startRun()`.
4. With no locally stored access token, the app opens activation. With a token, it refreshes local device information and opens `ContentActivity`.
5. Configuration failures remain on the splash and produce a Toast.

The custom `BaseUrlInterceptor` replaces the scheme, host, and port of every request with the stored `base-endpoint`; the encoded path from each configured full URL remains part of the Retrofit request. Consequently, endpoint paths come from bootstrap configuration and environments can change hosts without rebuilding. A previously stored base URL can also affect the next bootstrap request.

### 4.3 First-time device activation flow

1. `CodeActivationActivity` calls the configured device-code URL with `{clientId: "clientid", grantType: "user_code"}`.
2. The response supplies `deviceCode`, operator-facing `userCode`, `verificationUrl`, expiry/interval metadata, device name, and client ID. The TV shows the URL and user code and stores the name.
3. The operator visits the URL on a phone/computer, signs in if required, and enters/approves the code (the browser-side work is outside this repository).
4. The TV polls the configured token URL at the server-provided interval, sending the response client ID/device code and grant type `urn:ietf:params:oauth:grant-type:access_token`.
5. HTTP 428 means authorization is still pending, so polling continues, up to 100 attempts. Other HTTP errors open the error screen; transport failures do likewise.
6. A successful response's access and refresh tokens are persisted. The activity requests current device details, then routes to content.

The response's `expiresIn` value is modeled but polling currently stops by retry count rather than explicitly comparing the expiry timestamp.

### 4.4 Existing-device authentication and refresh flow

`AuthInterceptor` adds the current access token as `Authorization: Bearer …` to OkHttp requests. `TokenAuthenticator` handles a 401 synchronously: it prevents duplicate concurrent refreshes, POSTs to the configured refresh URL using the refresh token in the Authorization header, saves returned tokens, and retries the failed request. If there is no refresh token or refresh fails, it gives up. `AuthRepository` exposes the same refresh operation for the older `SignalrHubConnectionBuilder` path.

There is no explicit logout UI. A missing token activates the device; invalid credentials eventually surface through failed API calls/authentication. Tokens are stored in ordinary private `SharedPreferences`, not encrypted storage.

### 4.5 Content resolution and rendering flow

1. `ContentActivity` asks `ContentViewModel` to load current content and simultaneously initializes SignalR.
2. The configured content endpoint returns display/tenant identity, layout, optional menu, media, external source, text, checksum, and playlist.
3. If this is not a duplicate checksum within the active ViewModel, `ContentActivity` reads `layout.templateKey` and builds the relevant Intent.
4. Menu metadata/items and media models are parcelled; text/external URLs and playlist values are extras; every layout property becomes an extra.
5. The router starts the specialized activity and finishes itself. That activity composes fragments or native views and renders continuously.

For a missing/unknown template key, the error screen asks for a corrected layout and republish. Repository failures are currently represented by generic exception messages such as `Failed to load content data: 404`; `displayNotFoundMessage` expects symbolic codes (`no_such_device`, `no_screen_id`, `no_screen_data_found`), so those named branches only work if that exact string reaches it. The informational screen displays the resolved message and device name.

### 4.6 Real-time SignalR flow

SignalR is used because a signage display must respond quickly to dashboard changes without repeatedly polling, wasting bandwidth, or delaying a publish. The active implementation is the Hilt singleton `SignalRManager` plus `SignalRRepository`:

1. The manager POSTs to the negotiation endpoint with the device ID and REST Bearer authentication.
2. The backend returns the actual hub `url` and short-lived `accessToken`.
3. Microsoft SignalR's Java client opens a **WebSocket-only** `HubConnection`, authenticating with that negotiation token.
4. After connection, the app POSTs its device ID, device name, and SignalR connection ID to the add-connection endpoint. This lets the service target/group the correct display.
5. The client subscribes to hub method `ReceiveChangeMessage`; its single string argument is parsed as `SignalrReceivedMessage`.
6. Every ten seconds the client invokes hub method `ManualKeepAlive`, helping long-running TV connections survive idle-network/power-management behavior.
7. On an unexpected close it attempts backend removal and negotiates again after five seconds. Explicit Back/restart disconnects, removes group registration, stops the hub, waits two seconds, and exits or restarts.

Supported received `messageType` values are:

| Message | Current behavior |
| --- | --- |
| `device.info.update` | Refetch and persist device name, ID, and tenant. |
| `content.publish` | Refetch content; checksum suppression prevents a redundant same-session rerender. |
| `app.restart` | Disconnect, clear the task, and relaunch `MainActivity`. |
| `app.terminate` | Run the Back/exit flow. |
| `app.upgrade.info` | Calls the upgrade-information Toast hook with black styling. |
| `operator.info` | Calls the operator-information Toast hook with red styling. |

The current `showToastMessage` body is a placeholder, so the last two messages are recognized but do not yet produce visible UI. `SignalrHubConnectionBuilder` is a second, legacy connection implementation retained in the tree; current activity injection uses `SignalRManager`, not this builder.

### 4.7 Playlist flow

The content router passes `playlistData.itemsSerialized` and `itemDuration` to `PlaylistActivity` (`items` is passed but not consumed). `PlaylistViewModel` validates the array, loops its index, emits one item, and schedules timers. Each item's `key` selects JSON decoding:

- `AssetItemModel` becomes a `MediaAssetDataModel`; images use the shared timer and videos advance from `VideoView` completion.
- `TextAssetItemModel` becomes `TextADInformationAsset`; HTML, font, text color, and background are applied and the shared timer advances it.

An empty array shows a long Toast. Unsupported keys display a Toast and immediately advance. The duration parser accepts exactly `HH:mm:ss`; malformed/missing values fall back to ten seconds.

## 5. Backend HTTP endpoint catalogue

Except for bootstrap, concrete routes are intentionally **not hard-coded**: `config.json` returns them. In the table below, “configured URL” means the corresponding property under `display-api`. The shared Retrofit/OkHttp client applies dynamic host substitution, JSON conversion, BODY-level HTTP logging, Bearer authentication when a token exists, and 401 token refresh.

| Operation / configuration key | Method and request | Response and use | Authentication |
| --- | --- | --- | --- |
| Bootstrap | `GET https://www.onscreensync.com/config.json` | `{ "display-api": {…} }`; discovers every operational URL plus optional `messages.app-title` (modeled but not displayed). | None on first install; the interceptor may attach an existing token. |
| Device code / `device-code-url` | `POST <configured URL>`; JSON `clientId`, `grantType` | Device/user codes, verification URL, expiry, polling interval, device name, client ID; begins pairing. | Normally none (an existing token could be auto-attached). |
| Device token / `device-token-request-url` | `POST <configured URL>`; JSON `clientId`, `clientSecret`, `deviceCode`, `grantType` | Access token, refresh token, expiry, scope, token type; polled during activation. | Normally none. |
| Refresh token / `device-refresh-token-request-url` | `POST <configured URL>`; refresh grant JSON; header `Authorization: Bearer <refreshToken>` | Replacement access/refresh tokens; retries a 401 request or refreshes legacy SignalR negotiation. | Refresh token. |
| Device info / `device-info-url` | `GET <configured URL>` | `deviceName`, `tenantId`, `id`; persists identity used in the UI and SignalR registration. | Access-token Bearer header. The API method also supplies the header explicitly. |
| Content / `content-data-url` | `GET <configured URL>` | Full assigned display payload: identity, layout/properties, menu/items, media, external source, text, checksum, playlist. | Access-token Bearer header. |
| SignalR negotiate / `signalr-negotiation-url` | `POST <configured URL>?deviceId=<id>` | Hub `url` and hub `accessToken`; establishes the WebSocket. | REST access-token Bearer header. |
| SignalR add connection / `signalr-add-connection-url` | `POST <configured URL>?deviceId=<id>&deviceName=<name>&connectionId=<hub-id>` | Empty marker model; records/group-registers the live connection and saves its ID locally. | REST access-token Bearer header. |
| SignalR remove connection / `signalr-remove-connection-url` | `POST <configured URL>?deviceId=<id>&deviceName=<name>&connectionId=<hub-id>` | Empty marker model; unregisters a closed/exiting connection. | REST access-token Bearer header. |

SignalR's subsequent WebSocket handshake/hub traffic is not a Retrofit endpoint: it uses the negotiated URL/token. The client listens for `ReceiveChangeMessage` and sends `ManualKeepAlive` hub invocations.

### Important endpoint/configuration nuance

`DisplayApiConfigConstants.DEVICE_TOKEN_REQUEST_URL` and `DEVICE_REFRESH_TOKEN_REQUEST_URL` currently have the **same preference string value** (`"DEVICE_REFRESH_TOKEN_REQUEST_URL"`). Bootstrap writes the token-request URL and then overwrites it with the refresh URL. As implemented, activation polling therefore reads the last stored value (normally the refresh URL), rather than maintaining two independent preference entries. This documentation describes both backend contracts while calling out the current client behavior for maintainers.

## 6. Technologies and why they are used

| Technology | Role and rationale |
| --- | --- |
| Kotlin 1.9.22 / JVM 17 | Primary implementation language; null safety, data classes, extensions, and concise coroutine support suit a network-driven Android client. |
| Android SDK (min 21, compile/target 34) | Native TV lifecycle, boot broadcasts, `VideoView`, WebView, preferences, activities/fragments, and broad Android TV hardware support. |
| AndroidX Leanback | Declares/supports the TV launcher experience and remote-oriented Android TV ecosystem. |
| AppCompat, Material, ConstraintLayout | Compatible activity/widgets, theming, and responsive XML layout primitives across supported API levels. |
| View Binding | Generates type-safe binding classes from XML and avoids repetitive/error-prone `findViewById` calls. |
| Lifecycle ViewModel/runtime/LiveData artifacts | Lifecycle-aware state ownership and collection; the implementation chiefly uses ViewModel and Kotlin Flow. |
| Kotlin coroutines, StateFlow, SharedFlow | Non-blocking network delays/polling, lifecycle-safe asynchronous work, durable UI state, and one-time events. |
| Hilt 2.51.1 + KAPT | Compile-time dependency injection and singleton lifecycle management, making network/storage/repository dependencies consistent and testable. |
| Retrofit 2.11 + Gson converters | Declarative typed HTTP interfaces and JSON-to-model mapping for dynamically configured REST calls. |
| OkHttp 4.12 | Transport, request interception, BODY logging, automatic authorization headers, host replacement, and 401 authentication retry. |
| Gson 2.10.1 | Direct parsing of SignalR string payloads and serialized playlist item values in addition to Retrofit conversion. |
| Microsoft SignalR Java client 7.0 | Authenticated, bidirectional WebSocket hub connectivity for immediate publish and remote-management commands, including connection IDs and typed hub methods. |
| RxJava 3 (transitive through SignalR) | Supplies the `Single` access-token provider and subscribable SignalR start/stop operations used by the Microsoft client. |
| Picasso 2.8 | Simple asynchronous remote image download, decoding, caching, and assignment for signage images/menu icons. |
| SharedPreferences | Lightweight persistent storage for tokens, discovered routes, device identity, connection ID, and content checksum. The Gradle file also includes DataStore, but this code currently uses `SharedPreferences`. |
| Android WebView | Displays remotely hosted external media/pages without leaving the signage application. |
| Android `VideoView` / `MediaController` | Native streamed video presentation, looping, completion callbacks, and basic playback controls without a heavier media framework. |
| WorkManager 2.9.1 | Declared as a dependency but not currently used; boot startup is implemented with a broadcast receiver instead. |
| Gradle Kotlin DSL / Android Gradle Plugin 8.2.2 | Reproducible Android build configuration and Kotlin-based dependency/build declarations. |
| Google Services Gradle plugin | Applied to the app and available for Google service configuration; no Firebase/Google runtime feature is visible in the current source. |

## 7. Persistence and data handled locally

`LocalStorageService` uses the private preference file `com.example.screen_service`. It stores access/refresh tokens plus arbitrary strings for discovered base/routes, device name/ID/tenant, last SignalR connection ID, and content checksum. Values survive app process death and reboot. The repository contains no local database, offline media download, or explicit cache eviction. Picasso may provide its own image caching, but content JSON and video files are not maintained as an offline signage package.

## 8. Security and operational observations

- API secrets are not embedded, but placeholder client values and grant strings are present in activation requests.
- Clear-text traffic is allowed and HTTP BODY logging is enabled for all build types; production logs can therefore expose content and token responses. Production deployments should prefer HTTPS and reduce logging.
- Tokens are private to the app sandbox but are not encrypted, and application backup is allowed.
- External WebView content can execute JavaScript; only trusted backend-provided URLs should be assigned.
- The app declares network-state permission but does not currently gate requests on an explicit connectivity check.
- Specialized display activities do not initialize a new SignalR manager callback. The connection is started in `ContentActivity`, immediately before that router finishes; because the manager is a singleton its connection scope can survive, but its callback captures the finished activity. This is relevant when maintaining long-lived refresh behavior.
- The manifest references `ServiceUnavailableActivity`, but no corresponding Kotlin source is present in this repository snapshot. Its layout exists, and it is not used by the visible navigation flow.

## 9. Build and project layout

- Android project root: `src/`
- Application module: `src/app/`
- Kotlin source: `src/app/src/main/java/com/onscreensync/tvapp/`
- XML resources: `src/app/src/main/res/`
- Dependency-injection modules: `di/`
- Retrofit contracts: `apirequests/` and `signalR/SignalRServerApiRequest.kt`
- Wire/domain models: `apiresponses/` and `datamodels/`
- Rendering components: top-level activities and `fragments/`
- Network/auth infrastructure: `network/`, `repository/`, and `signalR/`

From `src/`, the normal verification/build commands are `./gradlew test` and `./gradlew assembleDebug`. There are currently no checked-in unit/instrumentation test source directories, so build and lint checks provide the primary automated validation of this snapshot.
