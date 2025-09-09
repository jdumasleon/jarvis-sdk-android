# 🚀 Release Guide

This guide explains how to use the automated release system for the Jarvis Android SDK.

## 📋 Quick Start

### 1. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
# Make your changes
git add .
git commit -m "Add new feature"
git push origin feature/your-feature-name
```

### 2. Open a Pull Request
- **Title Format**: `[VERSION_TYPE] Brief description`
- **Version Types**:
  - `MAJOR` - Breaking changes (1.0.0 → 2.0.0)
  - `MINOR` - New features, backward compatible (1.0.0 → 1.1.0) 
  - `PATCH` - Bug fixes, patches (1.0.0 → 1.0.1)
  - No prefix defaults to `PATCH`

### 3. Merge to Trigger Release
When you merge the PR to `main`, the system automatically:
- ✅ Builds demo app and SDK
- ✅ Runs tests and quality checks
- ✅ Bumps version based on PR title
- ✅ Creates git tag
- ✅ Publishes to Maven Central
- ✅ Publishes to GitHub Packages
- ✅ Creates GitHub release with artifacts
- ✅ Generates release notes from commits

## 🎯 PR Title Examples

### Major Release (Breaking Changes)
```
MAJOR Redesign network inspection API
MAJOR Update minimum Android SDK to 26
MAJOR Remove deprecated methods
```

### Minor Release (New Features)
```
MINOR Add real-time network monitoring
MINOR Implement dark mode support
MINOR Add export functionality
```

### Patch Release (Bug Fixes)
```
PATCH Fix memory leak in inspector
PATCH Update dependencies for security
PATCH Improve error handling
```

## 📊 What Gets Released

### GitHub Release Includes:
- 📱 **Demo APK** - `jarvis-demo-v{version}.apk`
- 📱 **Demo AAB** - `jarvis-demo-v{version}.aab`  
- 📦 **SDK AAR** - `jarvis-prod-compose-release.aar`
- 📋 **Release Notes** - Auto-generated from commits

### Published Packages:
- 🌍 **Maven Central**: `io.github.jdumasleon:jarvis-android-sdk:{version}`
- 📦 **GitHub Packages**: Available in your repository packages

## 🧪 Testing Before Release

Use the test script to validate everything works:

```bash
# Test with patch version bump
./scripts/test-release.sh patch

# Test with minor version bump  
./scripts/test-release.sh minor

# Test with major version bump
./scripts/test-release.sh major
```

## 🔍 Monitoring Releases

### GitHub Actions
- View workflow runs: `Actions` tab in GitHub
- Monitor build status and logs
- Download build artifacts

### Maven Central
- Check publication status: https://central.sonatype.com/
- Monitor deployment progress in Central Portal

### Release Notes
Auto-generated from commit messages between releases:
- Format: `- commit message (author)`
- Includes links to artifacts and documentation

## 🛠️ Troubleshooting

### Common Issues

#### ❌ Release Workflow Fails
1. Check GitHub Actions logs
2. Verify all secrets are configured
3. Ensure tests pass locally
4. Check Maven Central credentials

#### ❌ Version Not Updated
1. Verify PR title format includes `MAJOR|MINOR|PATCH`
2. Check `gradle/libs.versions.toml` has `jarvisVersion` entry
3. Ensure workflow has write permissions

#### ❌ Publishing Fails
1. Verify Maven Central tokens in GitHub Secrets
2. Check PGP signing configuration
3. Ensure namespace ownership in Central Portal

### Debug Commands
```bash
# Test local build
./gradlew build

# Test Maven publishing locally
./gradlew :jarvis:publishReleasePublicationToLocalRepository

# Check current version
grep jarvisVersion gradle/libs.versions.toml

# Run full test suite
./scripts/test-release.sh patch
```

## 🎛️ Advanced Configuration

### Custom Release Notes
Edit commit messages to improve release notes:
```bash
git commit -m "Add network inspection feature

- Real-time monitoring
- Export capabilities  
- Performance improvements"
```

### Skip CI for Minor Changes
Add `[skip ci]` to commit message:
```bash
git commit -m "Update README [skip ci]"
```

### Emergency Hotfix
For urgent fixes, create PR directly to `main`:
```
PATCH HOTFIX: Critical security vulnerability fix
```

## 📚 Integration Examples

### Using the Published SDK

#### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.jdumasleon:jarvis-android-sdk:1.2.3")
}
```

#### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.jdumasleon:jarvis-android-sdk:1.2.3'
}
```

### Version Catalog
```toml
[libraries]
jarvis-sdk = { group = "io.github.jdumasleon", name = "jarvis-android-sdk", version.ref = "jarvis" }

[versions]
jarvis = "1.2.3"
```

## 🔄 Release Cadence

### Recommended Schedule
- **Major Releases**: Quarterly (breaking changes)
- **Minor Releases**: Monthly (new features)
- **Patch Releases**: As needed (bug fixes)

### Branch Strategy
- `main` - Production ready code
- `develop` - Integration branch (optional)
- `feature/*` - Feature branches
- `hotfix/*` - Emergency fixes

## 📈 Analytics Integration

### PostHog Events
Releases automatically track analytics events:
```json
{
  "event": "sdk_release",
  "properties": {
    "version": "1.2.3",
    "version_type": "minor",
    "repository": "jdumasleon/jarvis-sdk-android"
  }
}
```

### Sentry Releases
Automatically creates Sentry releases for error tracking:
- Links commits to releases
- Enables release-based error filtering
- Tracks deployment success

---

## 🎉 Success Metrics

A successful release includes:
- ✅ All CI/CD checks pass
- ✅ Artifacts published to all repositories
- ✅ GitHub release created with proper artifacts
- ✅ Documentation updated automatically
- ✅ Analytics events tracked
- ✅ Team notifications sent

**Happy Releasing! 🚀**