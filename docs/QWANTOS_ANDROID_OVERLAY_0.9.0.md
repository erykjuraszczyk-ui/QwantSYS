# QwantOS Android Home Overlay 0.9.0

## Goal

Make QwantSYS usable as a selectable Android system interface on the REDMAGIC 7 without replacing the underlying Android installation.

## Architecture

QwantOS 0.9.0 is an application-level Home shell:

`Android / REDMAGIC OS`
→ `Android HOME role`
→ `QwantOS MainActivity`
→ `Qwant favorites dock, app drawer, device dashboard, Control Center, Cortana AI`

The manifest's `MAIN + HOME + DEFAULT` intent filter makes the activity eligible to be selected as the Home application. On Android 10 and later, the app uses `RoleManager.ROLE_HOME` to request user approval. The app never silently changes the default launcher.

## What this release implements

- QwantOS product identity in the launcher label and home dashboard.
- A system-shell status panel showing whether QwantOS currently holds the HOME role.
- An explicit activation action using the Android role chooser, with a Settings fallback.
- A Control Center entry point for Android-supported settings panels and settings screens.
- Existing favorites, app drawer, Glance, hardware monitor and Cortana features retained.

## What "overlay" means here

This is a Home-screen replacement / launcher shell. It does not draw over other apps and does not replace Android's system UI. The following remain controlled by Android/REDMAGIC OS:

- secure lock screen and biometrics;
- notification shade and privileged Quick Settings;
- permission dialogs and app sandbox;
- calls, mobile network, Wi-Fi and Bluetooth system services;
- verified boot, kernel, GPU drivers and firmware updates.

No root, bootloader unlock, device-owner provisioning, privileged signature, hidden API, `SYSTEM_ALERT_WINDOW`, notification-listener or accessibility permission is required.

## Compatibility and recovery

- Target device: REDMAGIC 7 NX679J, Android 13 / API 33.
- Minimum SDK remains API 26.
- The user can revert to the manufacturer's launcher through Android Settings → Apps → Default apps → Home app.
- If QwantOS is not selected, it continues to run as a normal launcher app and the dashboard reports that state.

## Validation boundary

CI can verify compilation, APK signing and packaging. Only on-device testing can confirm the actual REDMAGIC role chooser, default-home handoff, gesture navigation, recents behavior and vendor-specific launcher integration.
