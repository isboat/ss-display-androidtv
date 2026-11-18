# OnScreenSync TV players

This repository contains the native Android TV player and separate web players for Samsung Tizen and LG webOS. The players turn a registered television into a digital sign. An operator manages the screen centrally in OnScreenSync while the TV displays the assigned content full-screen and responds to updates.

## What the players do

- Discover the display endpoints, show an activation code for a new device, and keep the device signed in once an operator approves it.
- Fetch the device identity and assigned screen, render images, looping video, menus, rich text, or a clock, and rotate media in playlist layouts. Supported combinations include menu overlays and full-screen media. Playback capabilities depend on the platform, layout, and hardware.
- Listen for new screen assignments and supported remote commands. Common behavior includes refreshing content and updating identity information. Startup, restart, upgrade, and other lifecycle actions depend on the player and TV platform.

## How it is organised

- [src](../src) contains the native Android TV project. Its [detailed Android overview](../doc/project-overview.md) describes activation, identity, content rendering, and real-time communication.
- [ss-display-tizen](../ss-display-tizen) and [ss-display-webos](../ss-display-webos) are separate TypeScript apps with their own builds and tests. Each separates activation and API access from rendering, storage, and TV behavior. Tizen integrates Samsung AVPlay, while webOS uses an HTML video player and LG lifecycle support.
- [doc](../doc) contains the Samsung and LG implementation blueprints. Each player's README covers its platform requirements and packaging.

## What it relies on

- The **OnScreenSync website** publishes [bootstrap configuration](https://www.onscreensync.com/config.json). The **Display API** handles pairing, credentials, identity, and content. The **Management Dashboard** is where operators approve devices and assign or publish screens.
- **Microsoft SignalR** delivers real-time messages. The players also load media from the URLs in their assigned content.
- **AndroidX and Leanback** support the Android TV interface. Retrofit and OkHttp handle requests, Picasso loads images, and DataStore holds local preferences. The Android build uses Gradle and the Google Services plugin.
- The **Tizen and webOS** apps use TypeScript and Vite. Their platform tools are needed to produce installable packages and verify behavior on supported TVs.
