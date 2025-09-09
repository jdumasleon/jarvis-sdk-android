#!/bin/bash
# Script to publish Jarvis Android SDK to local Maven repository for testing

set -e

echo "🚀 Publishing Jarvis Android SDK to local repository..."

# Navigate to project directory
cd "$(dirname "$0")/.."

# Clean and build
echo "🧹 Cleaning project..."
./gradlew clean

echo "🔨 Building project..."
./gradlew :jarvis:assemble

# Publish to local repository
echo "📦 Publishing to local repository..."
./gradlew :jarvis:publishReleasePublicationToLocalRepository

echo "✅ Successfully published to local repository!"
echo "📍 Location: $(pwd)/jarvis/build/repo"
echo ""
echo "To use in another project, add to your build.gradle:"
echo "repositories {"
echo "    maven { url '$PWD/jarvis/build/repo' }"
echo "}"
echo ""
echo "dependencies {"
echo "    implementation 'io.github.jdumasleon:jarvis-android-sdk:1.0.0'"
echo "}"