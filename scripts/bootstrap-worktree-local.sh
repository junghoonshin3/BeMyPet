#!/usr/bin/env bash
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
DEFAULT_SOURCE_ROOT="$ROOT"

if [[ "$ROOT" == *"/.worktrees/"* ]]; then
  DEFAULT_SOURCE_ROOT="${ROOT%%/.worktrees/*}"
fi

SOURCE_ROOT="${BEMYPET_LOCAL_SOURCE_ROOT:-$DEFAULT_SOURCE_ROOT}"
FORCE_OVERWRITE="${1:-}"

copy_file() {
  local src="$1"
  local dest="$2"

  if [[ ! -f "$src" ]]; then
    echo "skip (source missing): $src"
    return 0
  fi

  mkdir -p "$(dirname "$dest")"

  if [[ -f "$dest" && "$FORCE_OVERWRITE" != "--force" ]]; then
    echo "skip (exists): $dest"
    return 0
  fi

  cp "$src" "$dest"
  echo "copied: $dest"
}

echo "workspace root: $ROOT"
echo "source root: $SOURCE_ROOT"

copy_file "$SOURCE_ROOT/secrets.dev.properties" "$ROOT/secrets.dev.properties"
copy_file "$SOURCE_ROOT/secrets.prod.properties" "$ROOT/secrets.prod.properties"
copy_file "$SOURCE_ROOT/secrets.properties" "$ROOT/secrets.properties"
copy_file "$SOURCE_ROOT/version.properties" "$ROOT/version.properties"
copy_file "$SOURCE_ROOT/app/src/dev/google-services.json" "$ROOT/app/src/dev/google-services.json"
copy_file "$SOURCE_ROOT/app/src/prod/google-services.json" "$ROOT/app/src/prod/google-services.json"

echo "done."
