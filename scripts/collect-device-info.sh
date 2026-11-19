#!/usr/bin/env bash
set -euo pipefail

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb is not installed or is not in PATH." >&2
  exit 1
fi

mapfile -t connected_devices < <(
  adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'
)

if [[ "${#connected_devices[@]}" -ne 1 ]]; then
  echo "ERROR: Connect exactly one Android device with USB debugging authorized." >&2
  echo "Detected authorized devices: ${#connected_devices[@]}" >&2
  exit 1
fi

serial="${connected_devices[0]}"
output_file="${1:-qwant-device-info.txt}"

get_prop() {
  local key="$1"
  local value
  value="$(adb -s "$serial" shell getprop "$key" | tr -d '\r')"
  printf '%s=%s\n' "$key" "$value"
}

{
  echo "# QwantSYS read-only device report"
  echo "# Generated locally; review before sharing."
  echo
  get_prop "ro.product.device"
  get_prop "ro.product.model"
  get_prop "ro.product.name"
  get_prop "ro.product.manufacturer"
  get_prop "ro.build.fingerprint"
  get_prop "ro.build.version.release"
  get_prop "ro.build.version.sdk"
  get_prop "ro.boot.hardware"
  get_prop "ro.boot.slot_suffix"
  get_prop "ro.boot.verifiedbootstate"
  get_prop "ro.vendor.build.fingerprint"

  echo
  echo "[/proc/partitions]"
  adb -s "$serial" shell cat /proc/partitions | tr -d '\r'
} > "$output_file"

echo "Saved read-only device report to: $output_file"
echo "No partition was written, erased, or modified."
echo "Review the report locally and remove any private identifiers before sharing."
