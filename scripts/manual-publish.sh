#!/bin/bash

# 📦 Manual Publishing Script
# Publishes Jarvis Android SDK to Local, GitHub Packages, and Maven Central

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
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

log_step() {
    echo -e "${PURPLE}🎯 $1${NC}"
}

# Configuration
NEW_VERSION=${1:-}
SKIP_TESTS=${2:-false}
PUBLISH_TARGET=${3:-"all"}

show_usage() {
    echo "🚀 Manual Publishing Script"
    echo "=========================="
    echo
    echo "Usage: $0 <new_version> [skip_tests] [target]"
    echo
    echo "Parameters:"
    echo "  new_version   - Version to publish (e.g., 1.0.1)"
    echo "  skip_tests    - Skip tests and quality checks (true/false, default: false)"
    echo "  target        - Publishing target (local/github/maven/all, default: all)"
    echo
    echo "Examples:"
    echo "  $0 1.0.1                    # Full publish with tests"
    echo "  $0 1.0.1 true               # Skip tests, publish all"
    echo "  $0 1.0.1 false local        # Publish to local only"
    echo "  $0 1.0.1 true github        # Skip tests, publish to GitHub only"
    echo "  $0 1.0.1 false maven        # Publish to Maven Central only"
    echo
    echo "Targets:"
    echo "  local   - Local Maven repository (~/.m2/repository)"
    echo "  github  - GitHub Packages"
    echo "  maven   - Maven Central"
    echo "  all     - All repositories (default)"
}

if [ -z "$NEW_VERSION" ]; then
    show_usage
    exit 1
fi

# Validate version format
if ! [[ $NEW_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    log_error "Invalid version format. Use semantic versioning (e.g., 1.0.1)"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "$ROOT_DIR/gradle/libs.versions.toml" ]; then
    log_error "This script must be run from the project root or scripts directory"
    exit 1
fi

cd "$ROOT_DIR"

echo "🚀 Manual Publishing Script"
echo "=========================="
echo
log_info "Configuration:"
echo "  📦 Version: $NEW_VERSION"
echo "  🧪 Skip Tests: $SKIP_TESTS"
echo "  🎯 Target: $PUBLISH_TARGET"
echo "  📁 Working Directory: $ROOT_DIR"
echo

# Step 1: Pre-flight checks
log_step "Step 1: Pre-flight Checks"

# Check Git status
if [ -n "$(git status --porcelain)" ]; then
    log_warning "Working directory has uncommitted changes"
    git status --short
    echo
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Aborted by user"
        exit 0
    fi
fi

# Check current branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    log_warning "You're not on the main branch (current: $CURRENT_BRANCH)"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Aborted by user"
        exit 0
    fi
fi

# Check prerequisites
command -v java >/dev/null 2>&1 || { log_error "Java not found"; exit 1; }
command -v git >/dev/null 2>&1 || { log_error "Git not found"; exit 1; }
[ -f "./gradlew" ] || { log_error "Gradle wrapper not found"; exit 1; }

log_success "Pre-flight checks completed"

# Step 2: Update version
log_step "Step 2: Updating Version"

CURRENT_VERSION=$(grep -E "jarvisVersion\s*=" gradle/libs.versions.toml | sed -E 's/.*=\s*"([^"]+)".*/\1/')
log_info "Current version: $CURRENT_VERSION"
log_info "New version: $NEW_VERSION"

# Backup and update version
cp gradle/libs.versions.toml gradle/libs.versions.toml.bak
sed -i.tmp "s/jarvisVersion = \".*\"/jarvisVersion = \"$NEW_VERSION\"/" gradle/libs.versions.toml
rm gradle/libs.versions.toml.tmp

# Verify version update
UPDATED_VERSION=$(grep -E "jarvisVersion\s*=" gradle/libs.versions.toml | sed -E 's/.*=\s*"([^"]+)".*/\1/')
if [ "$UPDATED_VERSION" = "$NEW_VERSION" ]; then
    log_success "Version updated successfully"
else
    log_error "Failed to update version"
    exit 1
fi

# Step 3: Clean build
log_step "Step 3: Cleaning Previous Builds"
./gradlew clean --quiet
log_success "Clean completed"

# Step 4: Quality checks and tests
if [ "$SKIP_TESTS" != "true" ]; then
    log_step "Step 4: Running Quality Checks and Tests"
    
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
    
    echo "  🧪 Running unit tests..."
    if ./gradlew test --quiet; then
        log_success "Unit tests passed"
    else
        log_error "Unit tests failed"
        exit 1
    fi
    
    log_success "All quality checks and tests passed"
else
    log_warning "Skipping tests and quality checks"
fi

# Step 5: Build artifacts
log_step "Step 5: Building Artifacts"

echo "  🏗️ Building main SDK AAR..."
./gradlew :jarvis:bundleProdComposeReleaseAar --quiet

echo "  📄 Building main SDK sources JAR..."
./gradlew :jarvis:sourceProdComposeReleaseJar --quiet

echo "  📚 Building main SDK Javadoc JAR..."
./gradlew :jarvis:javaDocProdComposeReleaseJar --quiet

echo "  🔄 Building no-op SDK AAR..."
./gradlew :jarvis-noop:bundleProdComposeReleaseAar --quiet

echo "  📄 Building no-op SDK sources JAR..."
./gradlew :jarvis-noop:sourceProdComposeReleaseJar --quiet

echo "  📚 Building no-op SDK Javadoc JAR..."
./gradlew :jarvis-noop:javaDocProdComposeReleaseJar --quiet

log_success "All artifacts built successfully (main SDK + no-op SDK)"

# Step 6: Publishing
log_step "Step 6: Publishing Artifacts"

publish_local() {
    echo "  🏠 Publishing to local repository..."
    
    echo "    📦 Publishing main SDK..."
    if ./gradlew :jarvis:publishReleasePublicationToLocalRepository --quiet; then
        log_success "Main SDK published to local repository"
    else
        log_error "Failed to publish main SDK to local repository"
        return 1
    fi
    
    echo "    🔄 Publishing no-op SDK..."
    if ./gradlew :jarvis-noop:publishReleasePublicationToLocalRepository --quiet; then
        log_success "No-op SDK published to local repository"
    else
        log_error "Failed to publish no-op SDK to local repository"
        return 1
    fi
    
    # Verify local publication
    LOCAL_PATH_MAIN="$HOME/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/$NEW_VERSION"
    LOCAL_PATH_NOOP="$HOME/.m2/repository/io/github/jdumasleon/jarvis-android-sdk-noop/$NEW_VERSION"
    
    if [ -d "$LOCAL_PATH_MAIN" ]; then
        echo "    📁 Main SDK artifacts: $LOCAL_PATH_MAIN"
        ls -la "$LOCAL_PATH_MAIN" | head -5
    fi
    
    if [ -d "$LOCAL_PATH_NOOP" ]; then
        echo "    📁 No-op SDK artifacts: $LOCAL_PATH_NOOP"  
        ls -la "$LOCAL_PATH_NOOP" | head -5
    fi
}

publish_github() {
    echo "  📦 Publishing to GitHub Packages..."
    
    # Check GitHub credentials
    if [ -z "$GITHUB_TOKEN" ] && [ -z "$GITHUB_ACTOR" ]; then
        if [ -f "github.properties" ]; then
            log_info "Using github.properties for authentication"
        else
            log_warning "GitHub credentials not found in environment or github.properties"
        fi
    fi
    
    echo "    📦 Publishing main SDK..."
    if ./gradlew :jarvis:publishReleasePublicationToGitHubPackagesRepository --quiet; then
        log_success "Main SDK published to GitHub Packages"
    else
        log_error "Failed to publish main SDK to GitHub Packages"
        return 1
    fi
    
    echo "    🔄 Publishing no-op SDK..."
    if ./gradlew :jarvis-noop:publishReleasePublicationToGitHubPackagesRepository --quiet; then
        log_success "No-op SDK published to GitHub Packages"
        echo "    🔗 View at: https://github.com/jdumasleon/jarvis-sdk-android/packages"
    else
        log_error "Failed to publish no-op SDK to GitHub Packages"
        return 1
    fi
}

publish_maven() {
    echo "  🌍 Publishing to Maven Central..."
    
    # Check Maven Central credentials
    if [ -f "publishing.properties" ]; then
        log_info "Using publishing.properties for authentication"
    elif [ -n "$OSSRH_USERNAME" ] && [ -n "$OSSRH_PASSWORD" ]; then
        log_info "Using environment variables for authentication"
    else
        log_error "Maven Central credentials not found"
        return 1
    fi
    
    echo "    📦 Publishing main SDK..."
    if ./gradlew :jarvis:publishReleasePublicationToCentralPortalOSSRHRepository --quiet; then
        log_success "Main SDK published to Maven Central staging"
    else
        log_error "Failed to publish main SDK to Maven Central"
        return 1
    fi
    
    echo "    🔄 Publishing no-op SDK..."
    if ./gradlew :jarvis-noop:publishReleasePublicationToCentralPortalOSSRHRepository --quiet; then
        log_success "No-op SDK published to Maven Central staging"
        echo "    🔗 Monitor at: https://central.sonatype.com/"
        log_info "Note: You may need to manually release from staging in Central Portal"
    else
        log_error "Failed to publish no-op SDK to Maven Central"
        return 1
    fi
}

# Execute publishing based on target
case $PUBLISH_TARGET in
    "local")
        publish_local
        ;;
    "github")
        publish_github
        ;;
    "maven")
        publish_maven
        ;;
    "all")
        publish_local
        echo
        publish_github
        echo
        publish_maven
        ;;
    *)
        log_error "Invalid target: $PUBLISH_TARGET"
        show_usage
        exit 1
        ;;
esac

# Step 7: Git operations
log_step "Step 7: Git Operations"

echo "  📝 Committing version changes..."
git add gradle/libs.versions.toml
git commit -m "📦 Release version $NEW_VERSION" --quiet

echo "  🏷️ Creating git tag..."
git tag -a "v$NEW_VERSION" -m "Release version $NEW_VERSION"

echo "  📤 Pushing changes and tag..."
git push origin $CURRENT_BRANCH --quiet
git push origin "v$NEW_VERSION" --quiet

log_success "Git operations completed"

# Step 8: Cleanup
log_step "Step 8: Cleanup"

# Remove backup file
rm -f gradle/libs.versions.toml.bak

log_success "Cleanup completed"

# Step 9: Summary
echo
echo "🎉 Publishing Summary"
echo "===================="
echo "  📦 Version: $CURRENT_VERSION → $NEW_VERSION"
echo "  🎯 Target: $PUBLISH_TARGET"
echo "  🏷️ Git Tag: v$NEW_VERSION"
echo

case $PUBLISH_TARGET in
    "local"|"all")
        echo "📍 Local Repository:"
        echo "  📁 Path: ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/$NEW_VERSION"
        ;;
esac

case $PUBLISH_TARGET in
    "github"|"all")
        echo "📦 GitHub Packages:"
        echo "  🔗 URL: https://github.com/jdumasleon/jarvis-sdk-android/packages"
        echo "  📋 Usage: implementation(\"io.github.jdumasleon:jarvis-android-sdk:$NEW_VERSION\")"
        ;;
esac

case $PUBLISH_TARGET in
    "maven"|"all")
        echo "🌍 Maven Central:"
        echo "  🔗 Portal: https://central.sonatype.com/"
        echo "  🔗 Search: https://central.sonatype.com/artifact/io.github.jdumasleon/jarvis-android-sdk"
        echo "  📋 Usage: implementation(\"io.github.jdumasleon:jarvis-android-sdk:$NEW_VERSION\")"
        ;;
esac

echo
echo "🔄 Next Steps:"
if [[ "$PUBLISH_TARGET" == "maven" || "$PUBLISH_TARGET" == "all" ]]; then
    echo "  1. 🌍 Check Maven Central staging and release if needed"
fi
echo "  2. 🎉 Create GitHub Release: https://github.com/jdumasleon/jarvis-sdk-android/releases/new"
echo "  3. 📢 Announce the release to your users"

echo
log_success "Publishing completed successfully! 🚀"

# Final verification prompt
echo
read -p "🔍 Would you like to verify the publications? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo
    log_info "Verification commands:"
    echo
    
    if [[ "$PUBLISH_TARGET" == "local" || "$PUBLISH_TARGET" == "all" ]]; then
        echo "📍 Local Repository:"
        echo "  ls -la ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/$NEW_VERSION/"
        ls -la ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/$NEW_VERSION/ 2>/dev/null || echo "  (Directory not found)"
        echo
    fi
    
    if [[ "$PUBLISH_TARGET" == "github" || "$PUBLISH_TARGET" == "all" ]]; then
        echo "📦 GitHub Packages:"
        echo "  Open: https://github.com/jdumasleon/jarvis-sdk-android/packages"
        echo
    fi
    
    if [[ "$PUBLISH_TARGET" == "maven" || "$PUBLISH_TARGET" == "all" ]]; then
        echo "🌍 Maven Central:"
        echo "  Open: https://central.sonatype.com/"
        echo
    fi
fi