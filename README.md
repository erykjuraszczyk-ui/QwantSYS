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
