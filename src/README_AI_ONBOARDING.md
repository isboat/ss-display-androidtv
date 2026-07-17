# AI Agent Onboarding: Screen Service TV App

This document provides a comprehensive technical mental model of the system's architecture, data flow, and critical components for any AI agent or developer onboarding into the codebase.

## 1. Project Overview
The **Screen Service TV App** is a specialized Android TV application acting as a digital signage player. It orchestrates content delivery by:
1. Fetching dynamic backend configuration via a bootstrap API.
2. Authenticating via OAuth2 Device Flow (pairing code).
3. Displaying multi-media layouts (videos, images, menus, playlists, clocks).
4. Synchronizing state and content updates in real-time via persistent WebSockets (SignalR).

## 2. Core Architecture & Stack
*   **Language**: Kotlin
*   **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern.
*   **Dependency Injection**: Hilt (`@AndroidEntryPoint`, `@HiltViewModel`). Modules are located in `com.onscreensync.tvapp.di` (`AppModule`, `NetworkModule`).
*   **Networking**: Retrofit + OkHttp with custom interceptors for authentication and dynamic base URLs.
*   **Real-time Synchronization**: Microsoft SignalR Java client.
*   **Persistence**: `SharedPreferences` managed via `com.onscreensync.tvapp.services.LocalStorageService`.
*   **UI**: ViewBinding + Fragment-based modularity for rendering different content types within specialized Activities.

## 3. The Lifecycle of the App
Execution flow follows this specific sequence:

1.  **`TvApp.kt`**: The Hilt Application class. Initializes the DI container and global application state.
2.  **`MainActivity.kt` (Bootstrapping)**:
    *   Triggers `MainViewModel.loadApiConfig()`.
    *   Fetches the "Display API Config" which contains all operational URLs (SignalR, Token, Device Info).
    *   Validates the existence of an `accessToken`.
3.  **Authentication (`CodeActivationActivity`)**:
    *   Triggered if no token exists.
    *   Uses **OAuth2 Device Flow**: Displays a `user_code` and `verification_url` on screen.
    *   The `CodeActivationViewModel` polls the token endpoint until the user authorizes the device on a separate web browser.
4.  **Routing (`ContentActivity`)**:
    *   Fetches the assigned "Layout" for the device via `ContentRepository`.
    *   Inspects the `templateKey` and redirects to specialized Activities:
        *   `PlaylistActivity`: For media loops/playlists.
        *   `MenuOnlyActivity` / `MenuOverlayActivity`: For digital menus.
        *   `MediaOnlyActivity`: For full-screen image/video.
        *   `TextEditorActivity`: For informational text/HTML assets.
        *   `CurrentDateTimeActivity`: For clock and date displays.
5.  **Real-time Sync**: `ContentActivity` (via `SignalRManager`) maintains a persistent connection to listen for remote commands from the dashboard like `content.publish` (triggering a refresh of data) or `app.restart`.

## 4. Key Packages & Components
### Network Layer (`com.onscreensync.tvapp.network`)
*   **`AuthInterceptor.kt`**: Injects `Authorization: Bearer <token>` into every request automatically.
*   **`BaseUrlInterceptor.kt`**: Dynamically overrides the Retrofit base URL using the config fetched during bootstrap, allowing for backend environment flexibility.
*   **`TokenAuthenticator.kt`**: Intercepts `401 Unauthorized` responses and performs a synchronous token refresh using the `refresh_token` before retrying the original request.

### Repositories (`com.onscreensync.tvapp.repository`)
*   **`AuthRepository.kt`**: Manages token lifecycle, refresh logic, and authentication state.
*   **`ContentRepository.kt`**: Fetches the layout structure, media assets, and playlist data.
*   **`DeviceRepository.kt`**: Handles device-specific metadata like name, unique ID, and tenant association.
*   **`SignalRRepository.kt`**: Manages SignalR hub negotiation and registration to communication groups.

### Real-time Communication (`com.onscreensync.tvapp.signalR`)
*   **`SignalRManager.kt`**: High-level manager injected into Activities to handle connection state, message parsing (`SignalrReceivedMessage`), and dispatching.
*   **SignalR Message Types**:
    *   `content.publish`: Triggers a data refresh when content is updated.
    *   `app.restart` / `app.terminate`: Remote lifecycle management.
    *   `device.info.update`: Updates local device name and configuration.
*   **`SignalrHubConnectionBuilder.kt`**: Handles the underlying `HubConnection` setup, including manual keep-alives and 401 recovery logic.

### UI Modularity (`com.onscreensync.tvapp.fragments`)
Activities host specialized fragments to render assets, promoting reuse:
*   `VideoMediaFragment`, `ImageMediaFragment`, `ExternalMediaFragment`: Handle playback and display.
*   `BasicMenuFragment`, `PremiumMenuFragment`: Handle different digital menu presentation styles.
*   `TextADInformationFragment`: Renders HTML-based text content.

## 5. Coding Patterns & Constraints
*   **Coroutines & Flow**: Used extensively. `StateFlow` handles UI state, while `SharedFlow` is used for one-time events like navigation or error toasts.
*   **Result Pattern**: Repositories typically return `Result<T>` to force explicit handling of success and failure cases.
*   **Navigation**: Many content activities use `noHistory="true"` in the `AndroidManifest.xml` to prevent users from navigating back into stale content or pairing screens.
*   **Themes**: The app relies on `Theme.AppCompat` (or descendants) since it utilizes `AppCompatActivity`. **Important**: Using a pure `Theme.Leanback` for these activities will cause a crash.

## 6. How to Navigate the Code
*   **To find Layout handling**: Search `ContentActivity.kt` for the `templateKey` logic in `handleContentData()`.
*   **To find API endpoints**: Check `DisplayApiConfigConstants.kt` and the `MainViewModel.loadApiConfig()` method.
*   **To debug connection issues**: Examine `SignalRManager.kt` and `SignalrHubConnectionBuilder.kt`.
*   **Constants**: Centralized in `Constants.kt` and `DisplayApiConfigConstants.kt`.

## 7. Known Nuances
*   **SignalR Manual Keep-Alive**: The app sends a `ManualKeepAlive` message every 10 seconds to prevent aggressive TV network power-saving from closing the WebSocket.
*   **Boot Persistence**: `BootReceiver.kt` listens for `BOOT_COMPLETED` and `QUICKBOOT_POWERON` to ensure the app launches automatically when the TV is turned on.
*   **Checksum Verification**: `ContentViewModel` verifies data checksums to avoid redundant UI reloads and flickering when content is republished without actual data changes.
*   **Picasso**: Used for image loading and caching across various fragments.
