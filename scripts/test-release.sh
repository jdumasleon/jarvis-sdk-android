#!/bin/bash

# 🧪 Test Release Script
# This script helps test the release process locally before pushing to GitHub

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🚀 Jarvis SDK Release Test Script"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "$ROOT_DIR/gradle/libs.versions.toml" ]; then
    log_error "This script must be run from the project root or scripts directory"
    exit 1
fi

cd "$ROOT_DIR"

# Parse command line arguments
VERSION_TYPE=${1:-"patch"}
DRY_RUN=${2:-"true"}

if [ "$DRY_RUN" = "false" ]; then
    log_warning "DRY RUN DISABLED - This will make actual changes!"
    read -p "Are you sure you want to proceed? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Aborted by user"
        exit 0
    fi
fi

echo
log_info "Configuration:"
echo "  Version Type: $VERSION_TYPE"
echo "  Dry Run: $DRY_RUN"
echo

# Step 1: Check prerequisites
log_info "Step 1: Checking prerequisites..."

# Check Java
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log_success "Java found: $JAVA_VERSION"
else
    log_error "Java not found. Please install Java 18+"
    exit 1
fi

# Check Gradle
if [ -f "./gradlew" ]; then
    log_success "Gradle wrapper found"
else
    log_error "Gradle wrapper not found"
    exit 1
fi

# Check Git
if command -v git >/dev/null 2>&1; then
    log_success "Git found"
else
    log_error "Git not found"
    exit 1
fi

# Step 2: Get current version
log_info "Step 2: Getting current version..."

CURRENT_VERSION=$(grep -E "jarvisVersion\s*=" gradle/libs.versions.toml | sed -E 's/.*=\s*"([^"]+)".*/\1/')
if [ -z "$CURRENT_VERSION" ]; then
    log_error "Could not find current version in gradle/libs.versions.toml"
    exit 1
fi

log_success "Current version: $CURRENT_VERSION"

# Step 3: Calculate new version
log_info "Step 3: Calculating new version..."

IFS='.' read -r -a version_parts <<< "$CURRENT_VERSION"
major="${version_parts[0]}"
minor="${version_parts[1]}"
patch="${version_parts[2]}"

case $VERSION_TYPE in
    "major")
        major=$((major + 1))
        minor=0
        patch=0
        ;;
    "minor")
        minor=$((minor + 1))
        patch=0
        ;;
    "patch")
        patch=$((patch + 1))
        ;;
    *)
        log_error "Invalid version type: $VERSION_TYPE. Use: major, minor, or patch"
        exit 1
        ;;
esac

NEW_VERSION="$major.$minor.$patch"
log_success "New version will be: $NEW_VERSION"

# Step 4: Run tests and linting
log_info "Step 4: Running tests and linting..."

echo "  🔍 Running Kotlin lint..."
if ./gradlew ktlintCheck --quiet; then
    log_success "Kotlin lint passed"
else
    log_error "Kotlin lint failed"
    exit 1
fi

echo "  🔍 Running Detekt..."
if ./gradlew detekt --quiet; then
    log_success "Detekt passed"
else
    log_error "Detekt failed"
    exit 1
fi

echo "  🧪 Running tests..."
if ./gradlew test --quiet; then
    log_success "Tests passed"
else
    log_error "Tests failed"
    exit 1
fi

# Step 5: Build artifacts
log_info "Step 5: Building artifacts..."

echo "  🏗️ Building demo app..."
if ./gradlew :app:assembleProdComposeRelease --quiet; then
    log_success "Demo app built successfully"
else
    log_error "Demo app build failed"
    exit 1
fi

echo "  📦 Building SDK..."
if ./gradlew :jarvis:bundleProdComposeReleaseAar --quiet; then
    log_success "SDK built successfully"
else
    log_error "SDK build failed"
    exit 1
fi

# Step 6: Test publishing (local only)
log_info "Step 6: Testing local publishing..."

if ./gradlew :jarvis:publishReleasePublicationToLocalRepository --quiet; then
    log_success "Local publishing test passed"
else
    log_error "Local publishing test failed"
    exit 1
fi

# Step 7: Check publishing configuration
log_info "Step 7: Checking publishing configuration..."

if [ -f "publishing.properties" ]; then
    log_success "Publishing properties file exists"
    
    # Check required properties
    if grep -q "ossrh.username" publishing.properties; then
        log_success "OSSRH username configured"
    else
        log_warning "OSSRH username not configured"
    fi
    
    if grep -q "ossrh.password" publishing.properties; then
        log_success "OSSRH password configured"
    else
        log_warning "OSSRH password not configured"
    fi
    
    if grep -q "signing.key" publishing.properties; then
        log_success "Signing key configured"
    else
        log_warning "Signing key not configured"
    fi
else
    log_warning "Publishing properties file not found (will use environment variables)"
fi

# Step 8: Version update simulation
log_info "Step 8: Version update simulation..."

if [ "$DRY_RUN" = "true" ]; then
    log_info "Would update version from $CURRENT_VERSION to $NEW_VERSION"
    log_info "Would update gradle/libs.versions.toml"
else
    log_info "Updating version in gradle/libs.versions.toml..."
    sed -i.bak "s/jarvisVersion = \".*\"/jarvisVersion = \"$NEW_VERSION\"/" gradle/libs.versions.toml
    log_success "Version updated to $NEW_VERSION"
    
    # Restore backup if this is still a dry run
    if [ "$DRY_RUN" = "true" ]; then
        mv gradle/libs.versions.toml.bak gradle/libs.versions.toml
    fi
fi

# Step 9: Git status check
log_info "Step 9: Checking Git status..."

if [ -n "$(git status --porcelain)" ]; then
    log_warning "Working directory has uncommitted changes"
    git status --short
else
    log_success "Working directory is clean"
fi

# Step 10: Generate release notes preview
log_info "Step 10: Generating release notes preview..."

PREVIOUS_TAG=$(git describe --tags --abbrev=0 HEAD 2>/dev/null || echo "")
if [ -n "$PREVIOUS_TAG" ]; then
    log_success "Previous tag: $PREVIOUS_TAG"
    COMMITS=$(git log ${PREVIOUS_TAG}..HEAD --oneline --no-merges --format="- %s (%an)" | head -10)
else
    log_info "No previous tags found, showing last 10 commits"
    COMMITS=$(git log --oneline --no-merges --format="- %s (%an)" -10)
fi

echo
echo "📋 Release Notes Preview:"
echo "========================"
echo "## 🚀 What's Changed"
echo
echo "$COMMITS"
echo

# Step 11: Final summary
log_info "Step 11: Summary..."

echo
echo "🎯 Release Test Summary:"
echo "======================="
echo "  Current Version: $CURRENT_VERSION"
echo "  New Version: $NEW_VERSION"
echo "  Version Type: $VERSION_TYPE"
echo "  Dry Run: $DRY_RUN"
echo
log_success "All checks passed! 🎉"

if [ "$DRY_RUN" = "true" ]; then
    echo
    log_info "To perform actual release:"
    echo "  1. Create a PR with title: '${VERSION_TYPE^^} Your feature description'"
    echo "  2. Merge the PR to main branch"
    echo "  3. GitHub Actions will automatically:"
    echo "     - Create tag v$NEW_VERSION"
    echo "     - Build and upload artifacts"  
    echo "     - Create GitHub release"
    echo "     - Publish to Maven Central"
    echo "     - Publish to GitHub Packages"
fi

echo
log_success "Test completed successfully! 🚀"