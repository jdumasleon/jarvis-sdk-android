# 📦 Manual Publishing Guide

This guide explains how to manually publish the Jarvis Android SDK from your Mac to Maven Central, GitHub Packages, and local repository without using CI/CD.

## 🎯 When to Use Manual Publishing

- 🧪 **Testing releases locally**
- 🚨 **Emergency hotfixes** when CI/CD is down
- 🔍 **Debugging publishing issues**
- 🎛️ **Custom release configurations**
- 📋 **Pre-production testing**

## 🛠️ Prerequisites

### System Requirements
- ✅ **macOS** with Xcode command line tools
- ✅ **Java 18+** (Amazon Corretto recommended)
- ✅ **Git** configured with your GitHub account
- ✅ **Android SDK** and Build Tools
- ✅ **GPG** for artifact signing

### Verify Prerequisites
```bash
# Check Java version
java -version

# Check Git configuration
git config --global user.name
git config --global user.email

# Check Android SDK
echo $ANDROID_HOME

# Check GPG
gpg --version
```

## 🔑 Setup Credentials

### 1. Maven Central Credentials
Create or update `publishing.properties` in project root:
```properties
# Maven Central Publishing Configuration
ossrh.username=aTMDrR
ossrh.password=Xh9niJETii5Bm5nvufPtItKtZKXhB0PmM

# PGP Signing Configuration
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
```

### 2. GitHub Credentials
Create or update `github.properties`:
```properties
# GitHub Packages Configuration
gpr.usr=jdumasleon
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

### 3. Environment Variables (Optional)
Set these in your shell profile (`~/.zshrc` or `~/.bash_profile`):
```bash
# Maven Central
export OSSRH_USERNAME=aTMDrR
export OSSRH_PASSWORD=Xh9niJETii5Bm5nvufPtItKtZKXhB0PmM

# GitHub
export GITHUB_ACTOR=jdumasleon
export GITHUB_TOKEN=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN

# Signing
export SIGNING_KEY="-----BEGIN PGP PRIVATE KEY BLOCK-----..."
export SIGNING_PASSWORD=Alucard@2
```

## 📋 Step-by-Step Publishing Process

### Step 1: Prepare Your Workspace
```bash
# Navigate to project directory
cd /Users/jldumas/Jo/Jarvis/mobile-jarvis-android-sdk

# Ensure you're on the main branch
git checkout main
git pull origin main

# Check current version
grep jarvisVersion gradle/libs.versions.toml
```

### Step 2: Update Version (If Needed)
```bash
# Current version
CURRENT_VERSION=$(grep -E "jarvisVersion\s*=" gradle/libs.versions.toml | sed -E 's/.*=\s*"([^"]+)".*/\1/')
echo "Current version: $CURRENT_VERSION"

# Manual version update (replace X.Y.Z with desired version)
NEW_VERSION="1.0.1"
sed -i.bak "s/jarvisVersion = \".*\"/jarvisVersion = \"$NEW_VERSION\"/" gradle/libs.versions.toml

# Verify the change
grep jarvisVersion gradle/libs.versions.toml
```

### Step 3: Clean and Build
```bash
# Clean previous builds
./gradlew clean

# Build all variants to ensure everything works
./gradlew build

# Build specific release artifacts
./gradlew :jarvis:assembleProdComposeRelease
./gradlew :jarvis:bundleProdComposeReleaseAar
./gradlew :jarvis:sourceProdComposeReleaseJar
./gradlew :jarvis:javaDocProdComposeReleaseJar
```

### Step 4: Run Quality Checks
```bash
# Run lint checks
./gradlew ktlintCheck detekt

# Run unit tests
./gradlew test

# Run instrumented tests (if device/emulator is connected)
./gradlew connectedAndroidTest
```

### Step 5: Test Local Publishing
```bash
# Test local repository publishing first
./gradlew :jarvis:publishReleasePublicationToLocalRepository

# Check if artifacts were created
ls -la ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/
```

## 🚀 Publishing to Different Repositories

### 🏠 Local Repository
```bash
# Publish to local Maven repository (~/.m2/repository)
./gradlew :jarvis:publishReleasePublicationToLocalRepository

# Verify local publication
ls -la ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/
```

### 📦 GitHub Packages
```bash
# Ensure GitHub credentials are set
export GITHUB_ACTOR=jdumasleon
export GITHUB_TOKEN=your_github_token

# Publish to GitHub Packages
./gradlew :jarvis:publishReleasePublicationToGitHubPackagesRepository

# Verify on GitHub
# Go to: https://github.com/jdumasleon/jarvis-sdk-android/packages
```

### 🌍 Maven Central
```bash
# Publish to Maven Central Staging
./gradlew :jarvis:publishReleasePublicationToCentralPortalOSSRHRepository

# Monitor the publication
echo "Check Central Portal: https://central.sonatype.com/"
```

## 🎯 All-in-One Publishing Script

Create a script to publish to all repositories:

```bash
#!/bin/bash
# save as scripts/manual-publish.sh

set -e

echo "🚀 Manual Publishing Script"
echo "=========================="

# Configuration
NEW_VERSION=${1:-}
SKIP_TESTS=${2:-false}

if [ -z "$NEW_VERSION" ]; then
    echo "Usage: $0 <new_version> [skip_tests]"
    echo "Example: $0 1.0.1"
    echo "Example: $0 1.0.1 true  # Skip tests"
    exit 1
fi

echo "📋 Publishing version: $NEW_VERSION"
echo "🧪 Skip tests: $SKIP_TESTS"
echo

# Update version
echo "📝 Updating version..."
sed -i.bak "s/jarvisVersion = \".*\"/jarvisVersion = \"$NEW_VERSION\"/" gradle/libs.versions.toml

# Clean build
echo "🧹 Cleaning..."
./gradlew clean

# Run tests (if not skipped)
if [ "$SKIP_TESTS" != "true" ]; then
    echo "🧪 Running tests..."
    ./gradlew test
    echo "🔍 Running quality checks..."
    ./gradlew ktlintCheck detekt
fi

# Build
echo "🏗️ Building..."
./gradlew :jarvis:bundleProdComposeReleaseAar

# Publish to local
echo "🏠 Publishing to local repository..."
./gradlew :jarvis:publishReleasePublicationToLocalRepository

# Publish to GitHub Packages
echo "📦 Publishing to GitHub Packages..."
./gradlew :jarvis:publishReleasePublicationToGitHubPackagesRepository

# Publish to Maven Central
echo "🌍 Publishing to Maven Central..."
./gradlew :jarvis:publishReleasePublicationToCentralPortalOSSRHRepository

echo "✅ Publishing completed successfully!"
echo
echo "🔗 Next steps:"
echo "1. Check Maven Central: https://central.sonatype.com/"
echo "2. Check GitHub Packages: https://github.com/jdumasleon/jarvis-sdk-android/packages"
echo "3. Create git tag: git tag v$NEW_VERSION && git push origin v$NEW_VERSION"
```

Make it executable and use it:
```bash
chmod +x scripts/manual-publish.sh

# Publish new version
./scripts/manual-publish.sh 1.0.1

# Publish with skipped tests (faster)
./scripts/manual-publish.sh 1.0.1 true
```

## 🏷️ Creating Git Tags and Releases

### Create Git Tag
```bash
NEW_VERSION="1.0.1"

# Commit version changes
git add gradle/libs.versions.toml
git commit -m "Release version $NEW_VERSION"

# Create and push tag
git tag -a "v$NEW_VERSION" -m "Release version $NEW_VERSION"
git push origin main
git push origin "v$NEW_VERSION"
```

### Create GitHub Release (Manual)
1. Go to: https://github.com/jdumasleon/jarvis-sdk-android/releases
2. Click "Create a new release"
3. Choose tag: `v1.0.1`
4. Title: `🚀 Jarvis SDK v1.0.1`
5. Upload artifacts:
   - `jarvis/build/outputs/aar/jarvis-prod-compose-release.aar`
   - `app/build/outputs/apk/prodCompose/release/app-prod-compose-release.apk`

## 🔍 Verification and Troubleshooting

### Verify Publications

#### Local Repository
```bash
# Check local Maven repository
ls -la ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/

# Test local dependency
./gradlew dependencies --configuration releaseRuntimeClasspath | grep jarvis
```

#### GitHub Packages
```bash
# Check via GitHub CLI
gh api repos/jdumasleon/jarvis-sdk-android/packages

# Or visit: https://github.com/jdumasleon/jarvis-sdk-android/packages
```

#### Maven Central
```bash
# Check Central Portal
open https://central.sonatype.com/

# Search for your artifact
# https://central.sonatype.com/artifact/io.github.jdumasleon/jarvis-android-sdk
```

### Common Issues and Solutions

#### ❌ Publishing Permission Denied
```bash
# Check credentials
echo "OSSRH_USERNAME: $OSSRH_USERNAME"
echo "GITHUB_TOKEN length: ${#GITHUB_TOKEN}"

# Verify GitHub token has packages:write permission
```

#### ❌ Signing Failed
```bash
# Check GPG key
gpg --list-secret-keys

# Verify signing password
# Check publishing.properties file
```

#### ❌ Build Failed
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches
./gradlew clean build

# Check Java version
java -version
```

#### ❌ Version Already Exists
```bash
# Check current published versions
curl -s https://repo1.maven.org/maven2/io/github/jdumasleon/jarvis-android-sdk/maven-metadata.xml

# Update to a new version number
```

## 📊 Publishing Checklist

Before publishing, ensure:

- [ ] ✅ Code is committed and pushed to main branch
- [ ] ✅ Version number is updated in `gradle/libs.versions.toml`
- [ ] ✅ All tests pass (`./gradlew test`)
- [ ] ✅ Lint checks pass (`./gradlew ktlintCheck detekt`)
- [ ] ✅ Build succeeds (`./gradlew build`)
- [ ] ✅ Credentials are configured (`publishing.properties`, `github.properties`)
- [ ] ✅ Local publishing works (`publishToMavenLocal`)

After publishing, verify:

- [ ] ✅ Local repository has artifacts
- [ ] ✅ GitHub Packages shows new version
- [ ] ✅ Maven Central staging completed
- [ ] ✅ Git tag created and pushed
- [ ] ✅ GitHub release created (optional)

## 🎯 Quick Commands Reference

```bash
# Quick version check
grep jarvisVersion gradle/libs.versions.toml

# Quick build and test
./gradlew clean build test

# Publish everywhere
./gradlew publishAllPublicationsToLocalRepository
./gradlew publishAllPublicationsToGitHubPackagesRepository  
./gradlew publishAllPublicationsToCentralPortalOSSRHRepository

# Create tag
git tag v1.0.1 && git push origin v1.0.1

# Check publications
ls ~/.m2/repository/io/github/jdumasleon/jarvis-android-sdk/
```

## 🚀 Success! 

You should now be able to manually publish your Jarvis Android SDK from your Mac to all target repositories. Remember to use the CI/CD pipeline for regular releases and manual publishing for testing and emergency situations.

**Happy Publishing! 📦**