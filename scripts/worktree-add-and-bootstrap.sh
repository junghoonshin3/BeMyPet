#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./scripts/worktree-add-and-bootstrap.sh <branch> [base=origin/develop] [path]

Examples:
  ./scripts/worktree-add-and-bootstrap.sh feature/foo
  ./scripts/worktree-add-and-bootstrap.sh feature/foo origin/develop
  ./scripts/worktree-add-and-bootstrap.sh feature/foo origin/develop .worktrees/feature-foo
EOF
}

if [[ $# -lt 1 ]]; then
  usage
  exit 1
fi

BRANCH="$1"
BASE="${2:-origin/develop}"

REPO_ROOT="$(git rev-parse --show-toplevel)"
SLUG="$(printf "%s" "$BRANCH" | tr '/' '-')"

if [[ -n "${3:-}" ]]; then
  case "$3" in
    /*) WORKTREE_PATH="$3" ;;
    *) WORKTREE_PATH="$REPO_ROOT/$3" ;;
  esac
else
  WORKTREE_PATH="$REPO_ROOT/.worktrees/$SLUG"
fi

if [[ -e "$WORKTREE_PATH" ]]; then
  echo "worktree path already exists: $WORKTREE_PATH"
  exit 1
fi

if git -C "$REPO_ROOT" show-ref --verify --quiet "refs/heads/$BRANCH"; then
  git -C "$REPO_ROOT" worktree add "$WORKTREE_PATH" "$BRANCH"
else
  git -C "$REPO_ROOT" worktree add -b "$BRANCH" "$WORKTREE_PATH" "$BASE"
fi

if [[ -x "$WORKTREE_PATH/scripts/bootstrap-worktree-local.sh" ]]; then
  "$WORKTREE_PATH/scripts/bootstrap-worktree-local.sh"
else
  echo "bootstrap script not found: $WORKTREE_PATH/scripts/bootstrap-worktree-local.sh"
  echo "You can copy local files manually."
fi

echo "ready: $WORKTREE_PATH"
