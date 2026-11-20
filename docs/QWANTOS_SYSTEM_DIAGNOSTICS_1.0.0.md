# QwantOS System Diagnostics 1.0.0

## Purpose

Provide a read-only, on-device status report for the QwantOS Android Home Shell prototype. The report is intended to help distinguish packaging, runtime and model-file problems during testing on the REDMAGIC 7.

## Report sections

- **Device:** model, device codename, Android release, API level, primary ABI and logical processor count.
- **Home role:** whether QwantOS currently holds Android's HOME role.
- **Native runtime:** whether the llama.cpp JNI library can be loaded, plus the current Qwen and E5 engine loaded flags.
- **Model files:** whether the Qwen 0.5B and multilingual E5 GGUF files exist in app-private storage and meet the configured minimum-size checks.
- **Memory:** number of local memory records, embedded records and records awaiting embedding.
- **Resources:** available/total RAM reported by Android and free bytes on the data partition.

## Privacy

The diagnostic report does not include note contents or chat history. It is generated on-device and is not transmitted. The clipboard is written only after the user explicitly selects **Kopiuj raport**.

## Interpretation

These indicators are deliberately separate:

1. JNI availability means the application could load its native shared library.
2. Model-file presence means a local file exists and passes a size threshold.
3. Runtime loaded means the corresponding native model-loading call returned success.

None of these checks by itself proves the quality, stability or speed of inference. Physical-device testing is still required.

## Scope

This is a normal Android application feature and uses public Android APIs plus app-private files and SQLite data. It does not require root, special permissions, hidden APIs, bootloader changes or firmware modifications.
