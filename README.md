# QwantSYS — Qwantum System

**Status: experimental project / ROM not yet buildable.**

QwantSYS is a planned Android-based operating system for a specifically validated REDMAGIC device variant. The initial engineering target is the user's confirmed **REDMAGIC 7 / NX679J**, with hardware version **NX679J_V1AMB** and build **NX679J_EUCommon_V7.12** as read from the phone's About screen. This confirmation applies to that handset; it does not validate other REDMAGIC 7-family variants.

The first implementation is planned as an AOSP-derived ROM. Qwant components (Core, UI, AI, Memory, Quantum and Game) will be developed as separable system components.

> **Important:** this repository does not currently contain a bootable ROM, a complete device tree, vendor binaries, a kernel tree or a verified flashing package. Do not flash experimental images or files from unrelated device variants.

## Project goals

- Replace the manufacturer's Android system with a Qwant-branded AOSP-based ROM.
- Keep hardware support, boot-chain integration and recovery as explicit engineering work.
- Provide modular Qwant services and a custom system interface.
- Add local AI and classical quantum-circuit simulation as optional modules.
- Preserve Android application compatibility where the selected AOSP base and device implementation allow it.

“Quantum” describes software simulation and algorithms. The REDMAGIC phone does not contain a quantum processor or physical qubits.

## Repository map

- `docs/ARCHITECTURE.md` — system layers and component boundaries.
- `docs/PROJECT_STATUS.md` — verified status, blockers and acceptance criteria.
- `docs/DEVICE_BRINGUP_NX679J.md` — device identification and bring-up checklist.
- `docs/BUILD_WORKSTATION.md` — host build environment and resource planning.
- `device/qwant/nx679j/README.md` — device-tree requirements; not a completed device tree.
- `qwant/core/` — first minimal Qwant Core Android system app.

## Development branch

The initial foundation is being prepared on `qwantSYS-0.1.0-foundation`. The `main` branch is not intended to receive unverified device images.

## First milestone: 0.1.0 Foundation

1. Establish the repository structure and technical boundaries.
2. Verify the exact phone model, firmware fingerprint, partition layout and boot architecture.
3. Select an AOSP release compatible with that firmware baseline.
4. Obtain and audit the matching device configuration, kernel source and vendor components.
5. Build and boot a first test image only after the above items are verified.

## Safety and compatibility

AOSP source code alone is not sufficient to make a phone ROM. A working port also requires device-specific configuration, compatible kernel and hardware interfaces, firmware/vendor components, correct partition definitions and a tested recovery path. These items must be sourced and validated for the exact device.

See [the device bring-up checklist](docs/DEVICE_BRINGUP_NX679J.md) before attempting any low-level operation.


## Qwant Launcher prototype — 0.7.0

The launcher is an Android application prototype for REDMAGIC 7 (arm64-v8a), not a replacement ROM and not a system partition image. Build output is produced by GitHub Actions.

### Memory Core 0.7.0

- Persistent memory records are stored in the app-private SQLite database.
- Existing 0.6.0 notes are migrated once from SharedPreferences.
- Entries can be added, searched, viewed and individually deleted from the Cortana memory panel.
- The command `zapamiętaj: treść` saves an explicit memory; `szukaj pamięci: fraza` searches it.
- The local model receives up to five text-matched memory records relevant to the latest user message.
- Search is lexical/ranked text retrieval. It is not vector search, embeddings or semantic similarity.
- All records remain on the device in the app's private storage. Clearing app data or uninstalling the app deletes them.

See [Memory Core 0.7.0 technical notes](docs/MEMORY_CORE_0.7.0.md).


## Qwant Launcher prototype — 0.8.0

### Semantic Memory Core

- Adds a separate `multilingual-e5-small` Q8_0 GGUF embedding model (about 126 MiB), downloaded on demand; it is not bundled into the APK.
- Creates normalized 384-dimensional vectors using the local llama.cpp CPU runtime.
- Stores vectors in the app-private SQLite `memories.embedding` BLOB column. Schema migration preserves existing 0.7.0 memory records.
- Indexes older unembedded records after E5 is loaded; new entries are embedded automatically while the model is active.
- Searches with cosine similarity. The E5 model's required prefixes are applied: `passage: ` when storing records and `query: ` when searching.
- If E5 is not active or no vector match is available, the app falls back to lexical search.
- Qwen chat and E5 embeddings use separate model contexts but share the native runtime and are serialized by its mutex.
- All inference and storage remain on the phone after model download. No cloud inference endpoint is used by this feature.

See [Semantic Memory Core 0.8.0 technical notes](docs/SEMANTIC_MEMORY_0.8.0.md).


## QwantOS Android Home Overlay — 0.9.0

0.9.0 advances the Android application prototype into a selectable QwantOS Home Shell. It is an Android launcher overlay, not a replacement Android ROM, boot image, system partition or firmware.

- The manifest labels the application as QwantOS and declares the HOME intent filter.
- The home screen offers an explicit activation action using Android's `RoleManager.ROLE_HOME` request on Android 10+; older supported versions fall back to Android Home settings.
- The launcher checks whether QwantOS currently holds the HOME role and displays the active/inactive state.
- Existing app drawer, favorites dock, device monitor, Qwant Control Center and Cortana interface are retained.
- Control Center opens supported Android Settings Panels or official Settings screens. It does not write protected system settings or impersonate privileged Quick Settings.
- Android continues to own lock screen, notifications, permission prompts, telephony, radio, verified boot and hardware drivers.
- No root, bootloader unlock, accessibility service, notification listener, overlay permission or firmware flashing is required.

### Activate on REDMAGIC 7

1. Install the QwantOS 0.9.0 APK.
2. Open QwantOS.
3. Select **Aktywuj QwantOS**.
4. Confirm QwantOS in the Android Home role chooser.
5. Press Home to open the QwantOS launcher. The system status card confirms whether QwantOS is the default Home app.

To switch back, open Android Settings → Apps → Default apps → Home app and select the REDMAGIC launcher.

See [QwantOS Android Home Overlay 0.9.0 technical design](docs/QWANTOS_ANDROID_OVERLAY_0.9.0.md).


## QwantOS System Diagnostics — 1.0.0

QwantOS 1.0.0 adds an on-device diagnostic report for the Android Home Shell prototype.

- The diagnostics screen reports device model, Android/API, ABI and whether QwantOS currently holds the Android HOME role.
- It distinguishes the native JNI library being available from the Qwen/E5 model files being present and the inference engines being loaded. These are separate states; a library being present does not prove inference has been tested on the handset.
- It reports local memory record counts and the semantic-embedding index counts without exposing note contents.
- It shows available RAM and app data storage using Android APIs.
- The report is generated locally. It is copied to the clipboard only after the user taps **Kopiuj raport**.
- No root, special system permission, telemetry endpoint or cloud upload is added.

### Version metadata

The Android Gradle configuration and the manual APK workflow both use versionCode `10` and versionName `1.0.0`. This keeps the package metadata aligned.

### Validation

GitHub Actions verifies compilation, JNI packaging and APK signing. It cannot verify the vendor-specific Home role flow or real model inference on a physical REDMAGIC 7. Those checks remain on-device acceptance tests.

See [QwantOS System Diagnostics 1.0.0](docs/QWANTOS_SYSTEM_DIAGNOSTICS_1.0.0.md).


## QwantOS Extended Final Candidate — 1.1.0

This release adds a user-initiated JSON backup and restore flow for local Cortana memory, chat history and Qwant launcher settings.

- Backup is saved through Android's document picker; the application does not silently write to shared storage.
- The backup envelope contains a SHA-256 checksum to detect accidental corruption; it is not a digital signature. Restore verifies the checksum and schema before importing data.
- Memory records are merged by category and body to avoid duplicates, with a hard cap of 500 records.
- Existing and imported chat history is merged, duplicate entries are removed, and only the newest 40 conversations are retained.
- Qwant launcher settings (accent, compact layout, 24-hour clock and favorites) are included.
- Downloaded GGUF model files are intentionally excluded; they can be downloaded again from Cortana's local-model panel.
- Backups are user-controlled files and may contain private notes and conversation text. Store them in a protected location.

### Build and signing

Version metadata is `versionCode 11` / `versionName 1.1.0`. The workflow verifies APK signature, zip alignment and package/version metadata.

For a persistent release signature, configure these GitHub Actions repository secrets:
`QWANT_RELEASE_KEYSTORE_BASE64`, `QWANT_RELEASE_KEYSTORE_PASSWORD`, `QWANT_RELEASE_KEY_ALIAS`, and `QWANT_RELEASE_KEY_PASSWORD`.
Without all four secrets, CI intentionally falls back to a temporary debug keystore. Such an APK is a test build and cannot be assumed to update an APK signed with another key. Do not delete the installed app until your data is backed up.

### Platform boundary

QwantOS remains an unprivileged Android Home launcher. It does not replace the vendor ROM, kernel, lock screen, radio stack, system notification shade or device firmware.
