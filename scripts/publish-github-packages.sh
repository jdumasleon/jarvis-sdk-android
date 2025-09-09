#!/bin/bash
# Script to publish Jarvis Android SDK to GitHub Packages

set -e

echo "🚀 Publishing Jarvis Android SDK to GitHub Packages..."

# Check required files
if [ ! -f "github.properties" ]; then
    echo "❌ Error: github.properties file not found!"
    echo "Please copy github.properties.template to github.properties and fill in your credentials."
    exit 1
fi

# Navigate to project directory
cd "$(dirname "$0")/.."

# Verify credentials are set
echo "🔐 Verifying credentials..."
if grep -q "your-github-username" github.properties; then
    echo "❌ Error: Please update github.properties with your actual GitHub credentials"
    exit 1
fi

# Clean and build
echo "🧹 Cleaning project..."
./gradlew clean

echo "🔨 Building project..."
./gradlew :jarvis:assemble

# Run tests
echo "🧪 Running tests..."
./gradlew :jarvis:test

# Publish to GitHub Packages
echo "📦 Publishing to GitHub Packages..."
./gradlew :jarvis:publishReleasePublicationToGitHubPackagesRepository

echo "✅ Successfully published to GitHub Packages!"
echo ""
echo "Your library is now available at:"
echo "https://github.com/jdumasleon/mobile-jarvis-android-sdk/packages"
echo ""
echo "To use in another project:"
echo "repositories {"
echo "    maven {"
echo "        name = 'GitHubPackages'"
echo "        url = uri('https://maven.pkg.github.com/jdumasleon/mobile-jarvis-android-sdk')"
echo "        credentials {"
echo "            username = 'your-github-username'"
echo "            password = 'your-github-token'"
echo "        }"
echo "    }"
echo "}"
echo ""
echo "dependencies {"
echo "    implementation 'io.github.jdumasleon:jarvis-android-sdk:1.0.0'"
echo "}"