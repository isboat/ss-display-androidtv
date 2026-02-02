# AI Agent Onboarding: Screen Service TV App

This document provides a comprehensive mental model of the system's architecture, data flow, and critical components for any AI agent or developer onboarding into the codebase.

## 1. Project Overview
The **Screen Service TV App** is an Android TV application designed to act as a digital signage player. It connects to the Screen Sync central server to fetch configurations, authenticates via OAuth2 Device Flow, and displays dynamic content (menus, videos, playlists) controlled in real-time via SignalR.

## 2. Core Architecture & Stack
*   **Language**: Kotlin
*   **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern.
*   **DI**: Hilt (`@AndroidEntryPoint`, `@HiltViewModel`).
*   **Networking**: Retrofit + OkHttp.
*   **Real-time Synchronization**: Microsoft SignalR (Java client).
*   **Persistence**: `SharedPreferences` via `LocalStorageService`.
*   **UI**: ViewBinding with support for standard `AppCompatActivity`.

## 3. The Lifecycle of the App
Execution flow follows this order:

1.  **`TvApp.kt`**: Hilt Application class. Initializes the dependency injection container.
2.  **`MainActivity.kt`**: The entry point. Triggers `MainViewModel.loadApiConfig()`, which fetches a JSON configuration containing all backend service URLs (Negotiation, Token, Content, etc.).
3.  **Authentication Flow (`CodeActivationActivity`)**:
    *   Initiated if no access token is found.
    *   Uses **OAuth2 Device Flow**: Displays a `user_code` and `verification_url`.
    *   Polls the server (`CodeActivationViewModel.startPollingStatus`) until the user authorizes the device.
4.  **Content Routing (`ContentActivity`)**:
    *   Fetches the assigned "Layout" for the specific device ID.
    *   Acts as a router, launching specialized activities based on the `templateKey` (e.g., `PlaylistActivity`, `MenuOnlyActivity`, `MediaOnlyActivity`).
5.  **Real-time Synchronization**: `ContentActivity` (and `SignalRManager`) listens for remote commands from the dashboard (e.g., `content.publish`, `app.restart`).

## 4. Key Components
*   **`SignalRManager.kt` / `SignalRRepository.kt`**: Manages the persistent WebSocket connection and keeps the device in sync with the dashboard.
*   **`AuthRepository.kt`**: Centralizes token management and refresh logic.
*   **`LocalStorageService.kt`**: The source of truth for all environment variables (URLs) and authentication tokens.
*   **`SignalrHubConnectionBuilder.kt`**: Utility for building SignalR connections with integrated 401/Token refresh handling.

## 5. Coding Patterns & Constraints
*   **Coroutines/Flow**: Used for UI-state management (`StateFlow`) and background tasks.
*   **Result Pattern**: Repositories return `Result<T>` to ensure explicit success/failure handling.
*   **Navigation**: Many Activities use `noHistory="true"` in the `AndroidManifest.xml` to prevent users from navigating back into stale content states.
*   **Themes**: The app requires `Theme.AppCompat` (or descendants) because it uses `AppCompatActivity`.

## 6. How to Navigate the Code
*   **To find Layout handling**: Search `ContentActivity.kt` for `templateKey`.
*   **To find API endpoints**: Check `DisplayApiConfigConstants.kt` and `MainViewModel.loadApiConfig()`.
*   **To debug connection issues**: Examine `SignalRManager.kt` and `SignalrHubConnectionBuilder.kt`.

## 7. Known Nuances
*   **Keep-Alive**: Implements `manualKeepAlive` for SignalR to prevent TV network stacks from dropping the connection.
*   **Boot Persistence**: `BootReceiver.kt` ensures the app starts automatically when the TV powers on.
