#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS_REPO_DEFAULT="/workspace/core"
TS_REPO="${1:-$TS_REPO_DEFAULT}"

VECTORS_DEST="$ROOT_DIR/manifesto-compiler/src/test/resources/vectors"
GOLDEN_DEST="$ROOT_DIR/manifesto-compiler/src/test/resources/golden"

VECTOR_FILES=(
  "evaluation.json"
  "evaluation-runtime-patch.json"
  "lowering.json"
  "lowering-patch-fragment.json"
  "lowering-runtime-patch.json"
)
OPTIONAL_VECTOR_FILES=(
  "ir-generator.json"
)
GOLDEN_FILES=(
  "compiler-e2e.json"
)

VECTOR_SOURCE_CANDIDATES=(
  "$TS_REPO/packages/compiler/vectors"
  "$TS_REPO/packages/compiler/src/__tests__/vectors"
  "$TS_REPO/packages/compiler/test/vectors"
  "$TS_REPO/packages/compiler/tests/vectors"
)
GOLDEN_SOURCE_CANDIDATES=(
  "$TS_REPO/packages/compiler/golden"
  "$TS_REPO/packages/compiler/src/__tests__/golden"
  "$TS_REPO/packages/compiler/test/golden"
  "$TS_REPO/packages/compiler/tests/golden"
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

copy_files() {
  local src_dir="$1"
  local dst_dir="$2"
  shift 2
  local file
  local copied=0
  local missing=0
  mkdir -p "$dst_dir"
  for file in "$@"; do
    if [[ -f "$src_dir/$file" ]]; then
      cp "$src_dir/$file" "$dst_dir/$file"
      echo "[sync-golden] copied: $file"
      copied=$((copied + 1))
    else
      echo "[sync-golden] missing in source: $src_dir/$file" >&2
      missing=$((missing + 1))
    fi
  done
  echo "[sync-golden] copied=$copied missing=$missing"
  if [[ "$missing" -gt 0 ]]; then
    return 2
  fi
}

echo "[sync-golden] ts_repo=$TS_REPO"

VECTOR_SOURCE="$(find_existing_dir "${VECTOR_SOURCE_CANDIDATES[@]}" || true)"
GOLDEN_SOURCE="$(find_existing_dir "${GOLDEN_SOURCE_CANDIDATES[@]}" || true)"

status=0
if [[ -n "$VECTOR_SOURCE" ]]; then
  echo "[sync-golden] vector_source=$VECTOR_SOURCE"
  copy_files "$VECTOR_SOURCE" "$VECTORS_DEST" "${VECTOR_FILES[@]}" || status=$?
  copy_files "$VECTOR_SOURCE" "$VECTORS_DEST" "${OPTIONAL_VECTOR_FILES[@]}" || true
else
  echo "[sync-golden] vector source not found. tried:" >&2
  printf '  - %s\n' "${VECTOR_SOURCE_CANDIDATES[@]}" >&2
  status=2
fi

if [[ -n "$GOLDEN_SOURCE" ]]; then
  echo "[sync-golden] golden_source=$GOLDEN_SOURCE"
  copy_files "$GOLDEN_SOURCE" "$GOLDEN_DEST" "${GOLDEN_FILES[@]}" || true
else
  echo "[sync-golden] golden source not found (optional). tried:" >&2
  printf '  - %s\n' "${GOLDEN_SOURCE_CANDIDATES[@]}" >&2
fi

if [[ "$status" -ne 0 ]]; then
  echo "[sync-golden] completed with missing files/sources" >&2
  exit "$status"
fi

echo "[sync-golden] done"
