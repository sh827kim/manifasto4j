#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS_REPO="${1:-${TS_CORE_REPO:-}}"

echo "[recover-golden-sync] ts_repo=$TS_REPO"
if [[ -z "$TS_REPO" ]]; then
  echo "[recover-golden-sync] TS core repo is not configured. set -PtsCoreRepo or TS_CORE_REPO." >&2
  exit 2
fi
"$ROOT_DIR/scripts/sync-golden.sh" "$TS_REPO"
CHECK_GOLDEN_SYNC_REQUIRE_SOURCE=1 "$ROOT_DIR/scripts/check-golden-sync.sh" "$TS_REPO"
echo "[recover-golden-sync] recovery and strict verification completed"
