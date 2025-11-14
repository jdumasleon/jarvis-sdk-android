#!/usr/bin/env bash

# =============================================================================
# Firebase App Distribution Helper
# -----------------------------------------------------------------------------
# Builds the Prod Compose release APK and uploads it to Firebase App Distribution
# while attaching the release notes stored in app/releasenotes.md.
# Release notes are automatically generated from git commits since the last tag.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
RELEASE_NOTES_FILE="${PROJECT_ROOT}/app/releasenotes.md"

# Function to generate release notes from git commits
generate_release_notes() {
  echo "📝 Generating release notes from git commits..."

  cd "${PROJECT_ROOT}"

  # Get the latest tag
  LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")

  # Generate release notes header
  cat > "${RELEASE_NOTES_FILE}" <<EOF
## Release Notes

EOF

  if [[ -z "${LATEST_TAG}" ]]; then
    # No tags exist, get all commits
    echo "ℹ️  No previous tags found. Generating notes from all commits."
    git log --pretty=format:"- %s" --no-merges >> "${RELEASE_NOTES_FILE}"
  else
    # Get commits since last tag
    echo "ℹ️  Generating notes from commits since ${LATEST_TAG}..."
    COMMIT_COUNT=$(git rev-list ${LATEST_TAG}..HEAD --count)

    if [[ ${COMMIT_COUNT} -eq 0 ]]; then
      echo "ℹ️  No new commits since ${LATEST_TAG}. Using latest commit."
      git log -1 --pretty=format:"- %s" --no-merges >> "${RELEASE_NOTES_FILE}"
    else
      git log ${LATEST_TAG}..HEAD --pretty=format:"- %s" --no-merges >> "${RELEASE_NOTES_FILE}"
    fi
  fi

  # Add a newline at the end
  echo "" >> "${RELEASE_NOTES_FILE}"

  echo "✅ Release notes generated at ${RELEASE_NOTES_FILE}"
  echo ""
  echo "Preview:"
  cat "${RELEASE_NOTES_FILE}"
  echo ""
}

# Generate release notes before distribution
generate_release_notes

if [[ ! -f "${RELEASE_NOTES_FILE}" ]]; then
  echo "❌ Release notes file not found at ${RELEASE_NOTES_FILE}"
  exit 1
fi

echo "📦 Building Prod Compose release..."
cd "${PROJECT_ROOT}"

./gradlew \
  assembleprodComposeRelease \
  appDistributionUploadprodComposeRelease \
  -PfirebaseAppDistributionReleaseNotesFile="${RELEASE_NOTES_FILE}"

echo "✅ Firebase distribution completed!"
