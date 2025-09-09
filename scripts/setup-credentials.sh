#!/bin/bash

# 🔐 Credentials Setup Script
# Helps set up publishing credentials for manual publishing

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
log_success() { echo -e "${GREEN}✅ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}"; }

echo "🔐 Credentials Setup Script"
echo "=========================="
echo

cd "$ROOT_DIR"

# Check if credentials already exist
PUBLISHING_EXISTS=false
GITHUB_EXISTS=false

if [ -f "publishing.properties" ]; then
    PUBLISHING_EXISTS=true
    log_info "publishing.properties already exists"
fi

if [ -f "github.properties" ]; then
    GITHUB_EXISTS=true
    log_info "github.properties already exists"
fi

echo

# Setup Maven Central credentials
if [ "$PUBLISHING_EXISTS" = "true" ]; then
    read -p "📦 Overwrite existing publishing.properties? (y/N): " -n 1 -r
    echo
    CREATE_PUBLISHING=$([[ $REPLY =~ ^[Yy]$ ]] && echo "true" || echo "false")
else
    CREATE_PUBLISHING="true"
fi

if [ "$CREATE_PUBLISHING" = "true" ]; then
    log_info "Setting up Maven Central credentials..."
    
    # Use the existing credentials you provided
    cat > publishing.properties << 'EOF'
# Maven Central Publishing Configuration
# Never commit this file to version control

# Sonatype OSSRH credentials (for Maven Central)
ossrh.username=aTMDrR
ossrh.password=Xh9niJETii5Bm5nvufPtItKtZKXhB0PmM

# PGP signing configuration
# You can generate a key pair with: gpg --gen-key
# Export the private key with: gpg --export-secret-keys --armor KEY_ID
signing.key=-----BEGIN PGP PRIVATE KEY BLOCK-----
lIYEaL9ihhYJKwYBBAHaRw8BAQdAmcXUzV4UqLDTzkTJi+HBRbxQ+NFSOtdRWYZ4
JPmCATb+BwMCG2WA4ryexwn66xn3ovRIR2vVJ7ytd2nPhc42Tt84EP56JhwqXH/I
dS5mddP9hFh++oQyPRhXi53VLl3+XEO6HNYDk6Rm9XwGoF5BCy8j5bQmSm9zZSBM
dWlzIER1bWFzIDxqZHVtYXNsZW9uQGdtYWlsLmNvbT6ImQQTFgoAQRYhBHvLUfrq
3BZbqomdY7EoRUOH+HqDBQJov2KGAhsDBQkFo5qABQsJCAcCAiICBhUKCQgLAgQW
AgMBAh4HAheAAAoJELEoRUOH+HqDvgYA/1yWehMrn4PWjC1faQDVB1KyKy0bfZPs
fWIy1Va1HEN1AQDYYLIQR19pRXdNp6Uov00Kgq9HjlsVGBrrGATz1ojrA5yLBGi/
YoYSCisGAQQBl1UBBQEBB0A6Q7R4jHYM2AKhpXMCTlHKM99Yhh8huf6wISoIRYcR
JwMBCAf+BwMCQZL9i/FZPir6a+K/aMzR5H5G01plQNImuxW+3xdLudOq+SkojOT1
fZlzZy0nOaddfjVWUr2X/x5FQOFCOg9lAzzwR0QZ414qBI/1yKOr04h+BBgWCgAm
FiEEe8tR+urcFluqiZ1jsShFQ4f4eoMFAmi/YoYCGwwFCQWjmoAACgkQsShFQ4f4
eoNDwAEA9pcWDvEezSwboXC+pqk9EEzL1a7ThfdfgcHh3Y2Xz1oA/izcJDD6dcWF
7fI+KdR2rx7e7MetK/X3Z/V7L8PUFaMO
=FSjm
-----END PGP PRIVATE KEY BLOCK-----
signing.password=Alucard@2
EOF

    log_success "Created publishing.properties with your Maven Central credentials"
fi

# Setup GitHub credentials
if [ "$GITHUB_EXISTS" = "true" ]; then
    read -p "📦 Overwrite existing github.properties? (y/N): " -n 1 -r
    echo
    CREATE_GITHUB=$([[ $REPLY =~ ^[Yy]$ ]] && echo "true" || echo "false")
else
    CREATE_GITHUB="true"
fi

if [ "$CREATE_GITHUB" = "true" ]; then
    log_info "Setting up GitHub Packages credentials..."
    
    echo "You need a GitHub Personal Access Token with 'packages:write' permission."
    echo "Create one at: https://github.com/settings/tokens"
    echo
    
    read -p "Enter your GitHub username [jdumasleon]: " GITHUB_USERNAME
    GITHUB_USERNAME=${GITHUB_USERNAME:-jdumasleon}
    
    read -p "Enter your GitHub Personal Access Token: " GITHUB_TOKEN
    
    if [ -n "$GITHUB_TOKEN" ]; then
        cat > github.properties << EOF
# GitHub Packages Configuration  
# Never commit this file to version control

gpr.usr=$GITHUB_USERNAME
gpr.key=$GITHUB_TOKEN
EOF
        
        log_success "Created github.properties with your GitHub credentials"
    else
        log_warning "Skipped GitHub credentials setup (no token provided)"
    fi
fi

# Update .gitignore
log_info "Updating .gitignore to exclude credential files..."

if [ -f ".gitignore" ]; then
    if ! grep -q "publishing.properties" .gitignore; then
        echo "" >> .gitignore
        echo "# Publishing credentials" >> .gitignore
        echo "publishing.properties" >> .gitignore
    fi
    
    if ! grep -q "github.properties" .gitignore; then
        echo "github.properties" >> .gitignore
    fi
    
    log_success "Updated .gitignore"
else
    cat > .gitignore << EOF
# Publishing credentials
publishing.properties
github.properties

# Gradle
.gradle/
build/
*/build/

# Android
*.apk
*.aab
local.properties

# IDE
.idea/
*.iml
.vscode/

# OS
.DS_Store
Thumbs.db
EOF
    log_success "Created .gitignore with credential exclusions"
fi

# Environment variables setup
echo
log_info "Setting up environment variables (optional)..."
echo

SHELL_RC=""
if [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
elif [ -f "$HOME/.bash_profile" ]; then
    SHELL_RC="$HOME/.bash_profile"
elif [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
fi

if [ -n "$SHELL_RC" ]; then
    read -p "Add publishing environment variables to $SHELL_RC? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        
        # Check if already exists
        if grep -q "OSSRH_USERNAME" "$SHELL_RC"; then
            log_warning "Environment variables already exist in $SHELL_RC"
        else
            cat >> "$SHELL_RC" << 'EOF'

# Jarvis SDK Publishing Environment Variables
export OSSRH_USERNAME=aTMDrR
export OSSRH_PASSWORD=Xh9niJETii5Bm5nvufPtItKtZKXhB0PmM
export SIGNING_PASSWORD=Alucard@2

# GitHub (update with your token)
export GITHUB_ACTOR=jdumasleon
# export GITHUB_TOKEN=your_github_token_here
EOF
            
            log_success "Added environment variables to $SHELL_RC"
            log_info "Run 'source $SHELL_RC' to reload, or restart your terminal"
        fi
    fi
fi

# Final summary
echo
echo "🎯 Setup Summary"
echo "==============="

if [ -f "publishing.properties" ]; then
    log_success "Maven Central credentials: publishing.properties ✓"
else
    log_warning "Maven Central credentials: Not configured"
fi

if [ -f "github.properties" ]; then
    log_success "GitHub Packages credentials: github.properties ✓"
else
    log_warning "GitHub Packages credentials: Not configured"
fi

if [ -f ".gitignore" ]; then
    log_success "Credential files excluded from git ✓"
fi

echo
log_info "Next Steps:"
echo "1. 🧪 Test your setup: ./scripts/test-release.sh patch"
echo "2. 📦 Manual publish: ./scripts/manual-publish.sh 1.0.1"
echo "3. 🔧 Configure GitHub Secrets for CI/CD (see .github/SECRETS_SETUP.md)"

echo
log_success "Credentials setup completed! 🎉"