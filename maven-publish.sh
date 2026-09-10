#!/usr/bin/env bash
#
# Release helper: bumps the project version (major/minor/patch), verifies the
# git state and publishes artifacts to Maven Central via mvn deploy.
#
# Usage:
#   ./maven-publish.sh              # interactive mode
#   ./maven-publish.sh patch        # non-interactive: patch|minor|major
#   ./maven-publish.sh patch --no-commit
#                                   # bump & deploy without git commit/tag
#
set -euo pipefail

POM="pom.xml"
REVISION_PROPERTY="revision"
SKIP_GIT=false
BUMP_KIND="${1:-}"

if [[ "${2:-}" == "--no-commit" ]]; then
  SKIP_GIT=true
fi

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

die() {
  echo "ERROR: $*" >&2
  exit 1
}

# Extracts the current version from the <revision> property in the root pom.
current_version() {
  local version
  version=$(sed -nE "s|^.*<${REVISION_PROPERTY}>([0-9]+\.[0-9]+\.[0-9]+)</${REVISION_PROPERTY}>.*$|\1|p" "$POM" | head -n1)
  [[ -n "$version" ]] || die "Cannot read <${REVISION_PROPERTY}> from $POM"
  echo "$version"
}

# Bumps a semver triple: major|minor|patch
bump_version() {
  local version=$1 kind=$2
  local major minor patch rest
  IFS='.-' read -r major minor patch rest <<<"$version"
  case "$kind" in
  major)
    major=$((major + 1))
    minor=0
    patch=0
    ;;
  minor)
    minor=$((minor + 1))
    patch=0
    ;;
  patch) patch=$((patch + 1)) ;;
  *) die "Unknown bump kind: '$kind' (expected major|minor|patch)" ;;
  esac
  echo "$major.$minor.$patch"
}

# ---------------------------------------------------------------------------
# Preconditions
# ---------------------------------------------------------------------------

GIT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || die "Not inside a git repository"
cd "$GIT_ROOT"

if [[ "$SKIP_GIT" == false ]]; then
  BRANCH="$(git rev-parse --abbrev-ref HEAD)"
  [[ "$BRANCH" == "main" || "$BRANCH" == "master" ]] || die "You are on branch '$BRANCH', switch to main first"

  [[ -z "$(git status --porcelain)" ]] || {
    echo "Uncommitted changes detected:"
    git status --short
    die "Commit or stash your changes before releasing"
  }
fi

# ---------------------------------------------------------------------------
# Version bump
# ---------------------------------------------------------------------------

CURRENT="$(current_version)"

if [[ -z "$BUMP_KIND" ]]; then
  echo "Current version: $CURRENT"
  echo
  echo "Which part to bump?"
  select BUMP_KIND in major minor patch; do
    [[ -n "$BUMP_KIND" ]] && break
  done
fi

NEW_VERSION="$(bump_version "$CURRENT" "$BUMP_KIND")"

echo
echo "Version: $CURRENT -> $NEW_VERSION"
read -r -p "Continue? [y/N] " confirm
[[ "$confirm" == "y" || "$confirm" == "Y" ]] || die "Aborted by user"

sed -i.bak "s|<${REVISION_PROPERTY}>${CURRENT}</${REVISION_PROPERTY}>|<${REVISION_PROPERTY}>${NEW_VERSION}</${REVISION_PROPERTY}>|" "$POM"
rm -f "$POM.bak"

# Sanity check: the property must be updated in exactly one place
UPDATED="$(current_version)"
[[ "$UPDATED" == "$NEW_VERSION" ]] || die "Failed to update version in $POM (still $UPDATED)"
grep -q "<${REVISION_PROPERTY}>${NEW_VERSION}</${REVISION_PROPERTY}>" "$POM" || die "Version replacement failed"

echo "Version updated in $POM"

# ---------------------------------------------------------------------------
# Commit + tag
# ---------------------------------------------------------------------------

if [[ "$SKIP_GIT" == false ]]; then
  git add "$POM"
  git commit -m "chore: release v${NEW_VERSION}"
fi

# ---------------------------------------------------------------------------
# Deploy
# ---------------------------------------------------------------------------

MVN="mvn"
# [[ -x "./mvnw" ]] && MVN="./mvnw"

echo
echo "Deploying v${NEW_VERSION}..."
"$MVN" clean deploy

if [[ "$SKIP_GIT" == false ]]; then
  git tag "v${NEW_VERSION}"
  echo
  echo "Tag v${NEW_VERSION} created."
  echo "Done! Do not forget to push: git push && git push --tags"
else
  echo
  echo "Done! Remember to commit the version bump in $POM."
fi
