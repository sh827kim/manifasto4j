#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TS_REPO_DEFAULT="/workspace/manifasto-ts-core"
TS_REPO="${1:-$TS_REPO_DEFAULT}"

echo "[recover-golden-sync] ts_repo=$TS_REPO"
"$ROOT_DIR/scripts/sync-golden.sh" "$TS_REPO"
CHECK_GOLDEN_SYNC_REQUIRE_SOURCE=1 "$ROOT_DIR/scripts/check-golden-sync.sh" "$TS_REPO"
echo "[recover-golden-sync] recovery and strict verification completed"
