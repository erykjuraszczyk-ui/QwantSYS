# QwantSYS — Qwantum System

**Status: experimental project / ROM not yet buildable.**

QwantSYS is a planned Android-based operating system for a specifically validated REDMAGIC device variant. The initial engineering target is the REDMAGIC 7 family, with **NX679J treated as a candidate device identifier only until it is verified on the actual handset and against its firmware**.

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
