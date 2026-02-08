#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS_REPO_DEFAULT="/workspace/core"
TS_REPO="${1:-$TS_REPO_DEFAULT}"

VECTOR_SOURCE="$TS_REPO/packages/compiler/vectors"
VECTOR_DEST="$ROOT_DIR/manifesto-compiler/src/test/resources/vectors"

VECTOR_FILES=(
  "evaluation.json"
  "evaluation-runtime-patch.json"
  "lowering.json"
  "lowering-patch-fragment.json"
  "lowering-runtime-patch.json"
)

if [[ ! -d "$VECTOR_SOURCE" ]]; then
  echo "[check-golden-sync] source not found: $VECTOR_SOURCE" >&2
  exit 2
fi

if [[ ! -d "$VECTOR_DEST" ]]; then
  echo "[check-golden-sync] destination not found: $VECTOR_DEST" >&2
  exit 2
fi

out_of_sync=0
for file in "${VECTOR_FILES[@]}"; do
  src="$VECTOR_SOURCE/$file"
  dst="$VECTOR_DEST/$file"
  if [[ ! -f "$src" ]]; then
    echo "[check-golden-sync] missing source file: $src" >&2
    out_of_sync=1
    continue
  fi
  if [[ ! -f "$dst" ]]; then
    echo "[check-golden-sync] missing destination file: $dst" >&2
    out_of_sync=1
    continue
  fi
  if ! cmp -s "$src" "$dst"; then
    echo "[check-golden-sync] out-of-sync: $file" >&2
    out_of_sync=1
  fi
done

if [[ "$out_of_sync" -ne 0 ]]; then
  echo "[check-golden-sync] vectors are not synchronized. run: ./scripts/sync-golden.sh \"$TS_REPO\"" >&2
  exit 1
fi

echo "[check-golden-sync] vectors are synchronized"
