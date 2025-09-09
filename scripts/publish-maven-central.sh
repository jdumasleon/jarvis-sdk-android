#!/bin/bash
# Script to publish Jarvis Android SDK to Maven Central via Sonatype OSSRH

set -e

echo "🚀 Publishing Jarvis Android SDK to Maven Central..."

# Check required files
if [ ! -f "publishing.properties" ]; then
    echo "❌ Error: publishing.properties file not found!"
    echo "Please copy publishing.properties.template to publishing.properties and fill in your credentials."
    exit 1
fi

# Navigate to project directory
cd "$(dirname "$0")/.."

# Verify credentials are set
echo "🔐 Verifying credentials..."
if grep -q "your-sonatype-username" publishing.properties; then
    echo "❌ Error: Please update publishing.properties with your actual Sonatype credentials"
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

# Publish to Maven Central staging
echo "📦 Publishing to Maven Central staging repository..."
./gradlew :jarvis:publishReleasePublicationToOSSRHRepository

echo "✅ Successfully published to Maven Central staging!"
echo ""
echo "Next steps:"
echo "1. Go to https://s01.oss.sonatype.org/"
echo "2. Login with your Sonatype credentials"
echo "3. Navigate to 'Staging Repositories'"
echo "4. Find your repository (usually named com.jarvis.mobile-XXXX)"
echo "5. Select it and click 'Close'"
echo "6. Wait for validation to complete"
echo "7. If validation passes, click 'Release'"
echo "8. Your library will be available on Maven Central within 2-4 hours"