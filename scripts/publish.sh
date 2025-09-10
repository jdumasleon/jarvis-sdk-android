#!/bin/bash

# 📦 Jarvis Android SDK - Publishing Script
# Publishes to GitHub Packages and Maven Central with proper signing
# Usage: ./publish.sh <version> [target] [options]

set -e

# Script metadata
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SCRIPT_VERSION="2.0.0"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Helper functions
log_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
log_success() { echo -e "${GREEN}✅ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}"; }
log_step() { echo -e "${PURPLE}🎯 $1${NC}"; }
log_header() { echo -e "${CYAN}🚀 $1${NC}"; }

# Parse arguments
NEW_VERSION=${1:-}
PUBLISH_TARGET=${2:-"all"}
DRY_RUN=${3:-false}

show_usage() {
    echo -e "${CYAN}🚀 Jarvis Android SDK Publishing Script v${SCRIPT_VERSION}${NC}"
    echo "========================================="
    echo ""
    echo "Usage: $0 <version> [target] [--dry-run]"
    echo ""
    echo "Arguments:"
    echo "  version     New version to publish (e.g., 1.0.11)"
    echo "  target      Publishing target: 'all', 'github', 'maven', 'local' (default: all)"
    echo "  --dry-run   Simulate publishing without actually doing it"
    echo ""
    echo "Examples:"
    echo "  $0 1.0.11                    # Publish v1.0.11 to all repositories"
    echo "  $0 1.0.11 github            # Publish v1.0.11 to GitHub Packages only"
    echo "  $0 1.0.11 maven             # Publish v1.0.11 to Maven Central only"
    echo "  $0 1.0.11 all --dry-run     # Simulate publishing v1.0.11"
    echo ""
    echo "Supported targets:"
    echo "  all       - GitHub Packages + Maven Central"
    echo "  github    - GitHub Packages only"
    echo "  maven     - Maven Central only"
    echo "  local     - Local Maven repository only"
    echo ""
    echo "Prerequisites:"
    echo "  • github.properties configured (for GitHub Packages)"
    echo "  • Environment variables set (for Maven Central + PGP)"
    echo "  • new_pgp_key.asc available (for signed releases)"
    echo ""
}

validate_version() {
    if [[ ! $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$ ]]; then
        log_error "Invalid version format: $1"
        log_info "Expected format: X.Y.Z or X.Y.Z-suffix (e.g., 1.0.11, 1.2.0-beta)"
        return 1
    fi
}

check_dependencies() {
    log_step "Checking dependencies..."
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle.kts" ] || [ ! -d "jarvis" ]; then
        log_error "Not in project root directory. Please run from mobile-jarvis-android-sdk/"
        return 1
    fi
    
    # Check Gradle wrapper
    if [ ! -f "./gradlew" ]; then
        log_error "Gradle wrapper not found"
        return 1
    fi
    
    # Check current version
    if [ ! -f "gradle/libs.versions.toml" ]; then
        log_error "Version catalog not found: gradle/libs.versions.toml"
        return 1
    fi
    
    log_success "Dependencies check passed"
}

check_credentials() {
    log_step "Checking credentials..."
    local missing_creds=false
    
    if [[ "$PUBLISH_TARGET" == "all" || "$PUBLISH_TARGET" == "github" ]]; then
        if [ ! -f "github.properties" ]; then
            log_error "GitHub credentials missing: github.properties not found"
            missing_creds=true
        elif grep -q "your-github-" github.properties; then
            log_error "GitHub credentials not configured in github.properties"
            missing_creds=true
        else
            log_success "GitHub credentials found"
        fi
        
        # For GitHub-only publishing, we still need real PGP key due to Vanniktech plugin requirements
        if [[ "$PUBLISH_TARGET" == "github" ]]; then
            if [ ! -f "new_pgp_key.asc" ]; then
                log_error "PGP key required even for GitHub Packages due to plugin constraints"
                log_info "Please ensure new_pgp_key.asc exists for signing"
                missing_creds=true
            elif [ -z "$ORG_GRADLE_PROJECT_signingInMemoryKey" ]; then
                log_info "Setting up PGP signing for GitHub Packages..."
                export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=""
                export ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat new_pgp_key.asc)"
                log_success "PGP signing configured for GitHub Packages"
            else
                log_success "PGP signing already configured for GitHub Packages"
            fi
        fi
    fi
    
    if [[ "$PUBLISH_TARGET" == "all" || "$PUBLISH_TARGET" == "maven" ]]; then
        if [ -z "$ORG_GRADLE_PROJECT_mavenCentralUsername" ] || [ -z "$ORG_GRADLE_PROJECT_mavenCentralPassword" ]; then
            log_error "Maven Central credentials missing. Set environment variables:"
            echo "  export ORG_GRADLE_PROJECT_mavenCentralUsername=\"sJy6Nw\""
            echo "  export ORG_GRADLE_PROJECT_mavenCentralPassword=\"your-password\""
            missing_creds=true
        else
            log_success "Maven Central credentials found"
        fi
        
        if [ ! -f "new_pgp_key.asc" ]; then
            log_warning "PGP key not found: new_pgp_key.asc"
            log_info "Signing will be disabled for this release"
        elif [ -z "$ORG_GRADLE_PROJECT_signingInMemoryKey" ]; then
            log_info "Setting up PGP signing..."
            export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=""
            export ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat new_pgp_key.asc)"
            log_success "PGP signing configured"
        else
            log_success "PGP signing already configured"
        fi
    fi
    
    if [[ "$PUBLISH_TARGET" == "local" ]]; then
        # Even for local publishing, we need PGP credentials for Vanniktech plugin
        if [ ! -f "new_pgp_key.asc" ]; then
            log_warning "PGP key not found: new_pgp_key.asc"
            log_info "Local publishing will use dummy signing"
        elif [ -z "$ORG_GRADLE_PROJECT_signingInMemoryKey" ]; then
            log_info "Setting up PGP signing for local publishing..."
            export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=""
            export ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat new_pgp_key.asc)"
            log_success "PGP signing configured for local publishing"
        else
            log_success "PGP signing already configured for local publishing"
        fi
    fi
    
    if [ "$missing_creds" = true ]; then
        log_error "Credentials check failed"
        return 1
    fi
}

update_version() {
    log_step "Updating version to $NEW_VERSION..."
    
    local current_version=$(grep 'jarvisVersion = ' gradle/libs.versions.toml | sed 's/.*"\(.*\)".*/\1/')
    log_info "Current version: $current_version"
    log_info "New version: $NEW_VERSION"
    
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY RUN] Would update version in gradle/libs.versions.toml"
        return 0
    fi
    
    # Create backup
    cp gradle/libs.versions.toml gradle/libs.versions.toml.backup
    
    # Update version
    if [ "$(uname)" = "Darwin" ]; then
        # macOS
        sed -i '' "s/jarvisVersion = \".*\"/jarvisVersion = \"$NEW_VERSION\"/" gradle/libs.versions.toml
    else
        # Linux
        sed -i "s/jarvisVersion = \".*\"/jarvisVersion = \"$NEW_VERSION\"/" gradle/libs.versions.toml
    fi
    
    # Verify update
    local updated_version=$(grep 'jarvisVersion = ' gradle/libs.versions.toml | sed 's/.*"\(.*\)".*/\1/')
    if [ "$updated_version" != "$NEW_VERSION" ]; then
        log_error "Version update failed. Expected: $NEW_VERSION, Got: $updated_version"
        mv gradle/libs.versions.toml.backup gradle/libs.versions.toml
        return 1
    fi
    
    log_success "Version updated successfully"
}

clean_and_build() {
    log_step "Building artifacts..."
    
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY RUN] Would clean and build artifacts"
        return 0
    fi
    
    ./gradlew clean
    ./gradlew :jarvis:bundleProdComposeReleaseAar :jarvis-noop:bundleProdComposeReleaseAar
    
    # Verify artifacts were created
    local main_aar=$(find jarvis/build -name "*-prod-compose-release.aar" | head -1)
    local noop_aar=$(find jarvis-noop/build -name "*-prod-compose-release.aar" | head -1)
    
    if [ ! -f "$main_aar" ] || [ ! -f "$noop_aar" ]; then
        log_error "Artifact build failed. Missing AAR files."
        return 1
    fi
    
    local main_size=$(ls -lh "$main_aar" | awk '{print $5}')
    local noop_size=$(ls -lh "$noop_aar" | awk '{print $5}')
    
    log_success "Artifacts built successfully"
    log_info "Main SDK: $main_size"
    log_info "No-op SDK: $noop_size"
}

publish_to_github() {
    log_step "Publishing to GitHub Packages..."
    
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY RUN] Would publish to GitHub Packages"
        return 0
    fi
    
    # Set up environment
    local github_user=$(grep 'gpr.usr=' github.properties | cut -d'=' -f2)
    local github_token=$(grep 'gpr.key=' github.properties | cut -d'=' -f2)
    
    export GITHUB_ACTOR="$github_user"
    export GITHUB_TOKEN="$github_token"
    
    # Publish main SDK
    log_info "Publishing main SDK..."
    ./gradlew :jarvis:publishMavenPublicationToGitHubPackagesRepository
    
    # Publish no-op SDK
    log_info "Publishing no-op SDK..."
    ./gradlew :jarvis-noop:publishMavenPublicationToGitHubPackagesRepository
    
    log_success "Published to GitHub Packages"
    log_info "Repository: https://github.com/jdumasleon/mobile-jarvis-android-sdk/packages"
}

publish_to_maven() {
    log_step "Publishing to Maven Central..."
    
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY RUN] Would publish to Maven Central"
        return 0
    fi
    
    # Publish main SDK
    log_info "Publishing main SDK..."
    ./gradlew :jarvis:publishMavenPublicationToMavenCentralRepository
    
    # Publish no-op SDK
    log_info "Publishing no-op SDK..."
    ./gradlew :jarvis-noop:publishMavenPublicationToMavenCentralRepository
    
    log_success "Published to Maven Central"
    log_info "Will be available at: https://repo1.maven.org/maven2/io/github/jdumasleon/"
}

publish_to_local() {
    log_step "Publishing to local Maven repository..."
    
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY RUN] Would publish to local Maven"
        return 0
    fi
    
    # Use publishToMavenLocal which doesn't require signing
    ./gradlew :jarvis:publishToMavenLocal :jarvis-noop:publishToMavenLocal
    
    log_success "Published to local Maven repository"
    log_info "Location: ~/.m2/repository/io/github/jdumasleon/"
}

show_usage_instructions() {
    log_step "Usage Instructions"
    
    echo ""
    echo -e "${CYAN}📋 Usage Instructions:${NC}"
    echo ""
    
    if [[ "$PUBLISH_TARGET" == "all" || "$PUBLISH_TARGET" == "maven" ]]; then
        echo -e "${GREEN}Maven Central (Recommended):${NC}"
        echo "dependencies {"
        echo "    debugImplementation(\"io.github.jdumasleon:jarvis-android-sdk:$NEW_VERSION\")"
        echo "    releaseImplementation(\"io.github.jdumasleon:jarvis-android-sdk-noop:$NEW_VERSION\")"
        echo "}"
        echo ""
    fi
    
    if [[ "$PUBLISH_TARGET" == "all" || "$PUBLISH_TARGET" == "github" ]]; then
        echo -e "${BLUE}GitHub Packages:${NC}"
        echo "repositories {"
        echo "    maven {"
        echo "        name = \"GitHubPackages\""
        echo "        url = uri(\"https://maven.pkg.github.com/jdumasleon/jarvis-sdk-android\")"
        echo "        credentials {"
        echo "            username = \"your-github-username\""
        echo "            password = \"your-github-token\""
        echo "        }"
        echo "    }"
        echo "}"
        echo ""
        echo "dependencies {"
        echo "    debugImplementation(\"io.github.jdumasleon:jarvis-android-sdk:$NEW_VERSION\")"
        echo "    releaseImplementation(\"io.github.jdumasleon:jarvis-android-sdk-noop:$NEW_VERSION\")"
        echo "}"
        echo ""
    fi
    
    if [[ "$PUBLISH_TARGET" == "local" ]]; then
        echo -e "${YELLOW}Local Maven:${NC}"
        echo "Check: ls -la ~/.m2/repository/io/github/jdumasleon/"
        echo ""
    fi
}

create_git_tag() {
    if [ "$DRY_RUN" = "true" ]; then
        log_info "[DRY RUN] Would create git tag v$NEW_VERSION"
        return 0
    fi
    
    log_step "Creating git tag..."
    
    git add gradle/libs.versions.toml
    git commit -m "Release v$NEW_VERSION"
    
    git tag -a "v$NEW_VERSION" -m "Release v$NEW_VERSION"
    
    log_success "Git tag v$NEW_VERSION created"
    log_info "Push with: git push origin main --tags"
}

cleanup_on_error() {
    if [ -f "gradle/libs.versions.toml.backup" ]; then
        log_info "Restoring version file..."
        mv gradle/libs.versions.toml.backup gradle/libs.versions.toml
    fi
}

main() {
    cd "$ROOT_DIR"
    
    # Show usage if no arguments
    if [ -z "$NEW_VERSION" ]; then
        show_usage
        exit 1
    fi
    
    # Handle help flag
    if [[ "$NEW_VERSION" == "-h" || "$NEW_VERSION" == "--help" ]]; then
        show_usage
        exit 0
    fi
    
    # Handle dry-run flag
    if [[ "$3" == "--dry-run" || "$PUBLISH_TARGET" == "--dry-run" ]]; then
        DRY_RUN="true"
        if [[ "$PUBLISH_TARGET" == "--dry-run" ]]; then
            PUBLISH_TARGET="all"
        fi
    fi
    
    # Trap errors for cleanup
    trap cleanup_on_error ERR
    
    # Main execution
    log_header "Jarvis Android SDK Publishing Script v$SCRIPT_VERSION"
    echo "========================================="
    log_info "Version: $NEW_VERSION"
    log_info "Target: $PUBLISH_TARGET"
    log_info "Dry run: $DRY_RUN"
    log_info "Working directory: $ROOT_DIR"
    echo ""
    
    # Validation
    validate_version "$NEW_VERSION"
    check_dependencies
    check_credentials
    
    # Build process
    update_version
    clean_and_build
    
    # Publishing
    case "$PUBLISH_TARGET" in
        "all")
            publish_to_github
            publish_to_maven
            ;;
        "github")
            publish_to_github
            ;;
        "maven")
            publish_to_maven
            ;;
        "local")
            publish_to_local
            ;;
        *)
            log_error "Unknown target: $PUBLISH_TARGET"
            show_usage
            exit 1
            ;;
    esac
    
    # Finalization
    if [[ "$PUBLISH_TARGET" != "local" && "$DRY_RUN" != "true" ]]; then
        create_git_tag
    fi
    
    # Clean up backup
    if [ -f "gradle/libs.versions.toml.backup" ]; then
        rm gradle/libs.versions.toml.backup
    fi
    
    # Success summary
    echo ""
    log_success "🎉 Publishing completed successfully!"
    echo ""
    show_usage_instructions
    
    if [ "$DRY_RUN" = "true" ]; then
        log_warning "This was a dry run. No actual publishing occurred."
    fi
}

# Run main function
main "$@"