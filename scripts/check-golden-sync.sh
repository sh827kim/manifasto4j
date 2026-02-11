#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS_REPO_DEFAULT="/workspace/manifasto-ts-core"
TS_REPO="${1:-$TS_REPO_DEFAULT}"

VECTOR_DEST="$ROOT_DIR/manifesto-compiler/src/test/resources/vectors"

VECTOR_SOURCE_CANDIDATES=(
  "$TS_REPO/packages/compiler/vectors"
  "$TS_REPO/packages/compiler/src/__tests__/vectors"
  "$TS_REPO/packages/compiler/test/vectors"
  "$TS_REPO/packages/compiler/tests/vectors"
  "$TS_REPO/packages/compiler/__tests__/vectors"
)

VECTOR_FILES=(
  "evaluation.json"
  "evaluation-runtime-patch.json"
  "lowering.json"
  "lowering-patch-fragment.json"
  "lowering-runtime-patch.json"
)

find_existing_dir() {
  local dir
  for dir in "$@"; do
    if [[ -d "$dir" ]]; then
      echo "$dir"
      return 0
    fi
  done
  return 1
}

VECTOR_SOURCE="$(find_existing_dir "${VECTOR_SOURCE_CANDIDATES[@]}" || true)"

if [[ -z "$VECTOR_SOURCE" ]]; then
  echo "[check-golden-sync] vector source not found. tried:" >&2
  printf '  - %s\n' "${VECTOR_SOURCE_CANDIDATES[@]}" >&2
  exit 2
fi

if [[ ! -d "$VECTOR_DEST" ]]; then
  echo "[check-golden-sync] destination not found: $VECTOR_DEST" >&2
  exit 2
fi

out_of_sync=0
missing_sources=()
missing_destinations=()
mismatched_files=()
synced_files=()

for file in "${VECTOR_FILES[@]}"; do
  src="$VECTOR_SOURCE/$file"
  dst="$VECTOR_DEST/$file"
  if [[ ! -f "$src" ]]; then
    echo "[check-golden-sync] missing source file: $src" >&2
    missing_sources+=("$file")
    out_of_sync=1
    continue
  fi
  if [[ ! -f "$dst" ]]; then
    echo "[check-golden-sync] missing destination file: $dst" >&2
    missing_destinations+=("$file")
    out_of_sync=1
    continue
  fi
  if ! cmp -s "$src" "$dst"; then
    echo "[check-golden-sync] out-of-sync: $file" >&2
    mismatched_files+=("$file")
    out_of_sync=1
  else
    synced_files+=("$file")
  fi
done

if [[ "$out_of_sync" -ne 0 ]]; then
  echo "[check-golden-sync] summary: missing_source=${#missing_sources[@]} missing_destination=${#missing_destinations[@]} mismatched=${#mismatched_files[@]}" >&2
  if [[ "${#missing_sources[@]}" -gt 0 ]]; then
    printf '[check-golden-sync] missing_source_files: %s\n' "${missing_sources[*]}" >&2
  fi
  if [[ "${#missing_destinations[@]}" -gt 0 ]]; then
    printf '[check-golden-sync] missing_destination_files: %s\n' "${missing_destinations[*]}" >&2
  fi
  if [[ "${#mismatched_files[@]}" -gt 0 ]]; then
    printf '[check-golden-sync] mismatched_files: %s\n' "${mismatched_files[*]}" >&2
  fi
  echo "[check-golden-sync] vectors are not synchronized. run: ./scripts/sync-golden.sh \"$TS_REPO\"" >&2
  exit 1
fi

echo "[check-golden-sync] vectors are synchronized (${#synced_files[@]} files)"
