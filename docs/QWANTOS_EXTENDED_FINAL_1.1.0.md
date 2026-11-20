# QwantOS Extended Final Candidate 1.1.0

## Purpose

Extend the QwantOS Android Home Shell with reliable user-controlled data portability while retaining the unprivileged launcher architecture.

## Backup format

The exported JSON file is an envelope:
- `format`: `QWANTOS_BACKUP_FILE`
- `payload`: the serialized JSON payload as text
- `sha256`: SHA-256 of the exact UTF-8 payload string

The payload contains:
- format identifier and schema version;
- export timestamp;
- up to 500 local memory records, including optional 384D embedding vectors encoded as Base64;
- the last 40 chat messages;
- launcher settings: accent, compact layout, 24-hour clock and favorite app components.

The backup does not include Qwen/E5 GGUF model files, runtime state, Android credentials, system settings, or protected OS data.

## Restore behavior

- File size is limited to 8 MiB.
- Envelope format, SHA-256 and schema version are checked before import.
- Memory records are validated and merged by exact category/body pair.
- Invalid or malformed vector data is ignored; that entry remains eligible for later E5 indexing.
- Chat history is merged with current history, duplicate role/text pairs are removed, and the newest 40 entries remain.
- Included launcher settings are restored only after explicit user confirmation.
- Existing model files and runtime state are left unchanged.

## Privacy

Backup export/import uses Android's Storage Access Framework. No broad storage permission or background upload is used. The backup can contain private notes and conversations; the user chooses where to save it.

## Signing

CI supports a persistent release keystore through four repository secrets:
- `QWANT_RELEASE_KEYSTORE_BASE64`
- `QWANT_RELEASE_KEYSTORE_PASSWORD`
- `QWANT_RELEASE_KEY_ALIAS`
- `QWANT_RELEASE_KEY_PASSWORD`

When all values are present, the APK is signed with that key. Otherwise CI creates a temporary debug key and clearly reports the fallback. A keystore is a private credential: never commit it to the repository or share it in chat. A temporary debug-signed APK is suitable for testing, not for guaranteed in-place upgrades across separate workflow runs.

## Scope

This is still an Android Home launcher application. It does not have system privileges and does not replace the Android ROM, kernel, firmware, secure lock screen, notification shade or hardware drivers.
