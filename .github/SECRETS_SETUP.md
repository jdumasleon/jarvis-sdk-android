# 🔐 GitHub Secrets and Environment Variables Setup

This document outlines all the secrets and environment variables needed for the automated release workflow.

## 📋 Required Secrets

Configure these in your GitHub repository settings: `Settings > Secrets and variables > Actions`

### 🔑 Repository Secrets

| Secret Name | Description | Example/Format | Required |
|------------|-------------|----------------|----------|
| `OSSRH_USERNAME` | Maven Central (OSSRH) username token | `aTMDrR` | ✅ |
| `OSSRH_PASSWORD` | Maven Central (OSSRH) password token | `Xh9niJETii5Bm5nvufPtItKtZKXhB0PmM` | ✅ |
| `SIGNING_KEY` | Base64 encoded PGP private key for signing | `LS0tLS1CRUdJTi...` | ✅ |
| `SIGNING_PASSWORD` | PGP key password | `your-pgp-password` | ✅ |
| `SENTRY_AUTH_TOKEN` | Sentry authentication token | `sntrys_xxx...` | ⚠️ |

### 🌍 Environment Variables (Repository Variables)

Configure these in: `Settings > Secrets and variables > Actions > Variables`

| Variable Name | Description | Example | Required |
|---------------|-------------|---------|----------|
| `POSTHOG_PROJECT_API_KEY` | PostHog project API key | `phc_xxxxx` | ⚠️ |
| `SENTRY_ORG` | Sentry organization slug | `your-org-name` | ⚠️ |
| `SENTRY_PROJECT` | Sentry project slug | `jarvis-android-sdk` | ⚠️ |

**Legend:**
- ✅ Required for workflow to function
- ⚠️ Optional (workflow will skip these steps if not configured)

## 🚀 Setup Instructions

### 1. Maven Central Publishing Secrets

You already have these configured in your `publishing.properties`:

```bash
# Add to GitHub Secrets
OSSRH_USERNAME=aTMDrR
OSSRH_PASSWORD=Xh9niJETii5Bm5nvufPtItKtZKXhB0PmM
```

### 2. PGP Signing Setup

#### Generate or Export PGP Key:
```bash
# If you need to generate a new key
gpg --gen-key

# Export your existing private key (replace KEY_ID with your key ID)
gpg --export-secret-keys --armor YOUR_KEY_ID | base64 -w 0
```

#### Add to GitHub Secrets:
```bash
SIGNING_KEY=<base64-encoded-private-key>
SIGNING_PASSWORD=<your-pgp-passphrase>
```

### 3. Sentry Integration (Optional)

#### Create Sentry Auth Token:
1. Go to Sentry → Settings → Account → Auth Tokens
2. Create a new token with `project:releases` scope
3. Add to GitHub Secrets:

```bash
SENTRY_AUTH_TOKEN=sntrys_your_token_here
```

#### Add Sentry Variables:
```bash
SENTRY_ORG=your-organization-slug
SENTRY_PROJECT=jarvis-android-sdk
```

### 4. PostHog Integration (Optional)

#### Get PostHog API Key:
1. Go to PostHog → Settings → Project API Keys
2. Copy your project API key
3. Add to GitHub Variables:

```bash
POSTHOG_PROJECT_API_KEY=phc_your_api_key_here
```

### 5. GitHub Token (Automatic)

The `GITHUB_TOKEN` is automatically provided by GitHub Actions. No setup required.

## 🔧 Additional Configuration Files

### Create `.github/dependabot.yml` (Optional)
```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
```

### Update `.gitignore`
Ensure these files are ignored:
```gitignore
# Release automation
signing-key.gpg
publishing.properties
release-notes.md
```

## 🎯 Workflow Triggers

### Automatic Release Trigger
The release workflow triggers when:
- A pull request is **merged** into `main` branch
- PR title determines version bump:
  - `MAJOR ...` → Major version bump (1.0.0 → 2.0.0)
  - `MINOR ...` → Minor version bump (1.0.0 → 1.1.0)
  - `PATCH ...` → Patch version bump (1.0.0 → 1.0.1)
  - No prefix → Defaults to patch

### PR Check Trigger
The PR check workflow triggers on:
- Pull requests opened against `main` or `develop`
- New commits pushed to open PRs

## 🚦 Testing the Setup

### 1. Test PR Checks
1. Create a feature branch
2. Make changes and push
3. Open PR to `main`
4. Verify PR checks run successfully

### 2. Test Release (Use Caution)
1. Create a test PR with title: `PATCH Test release automation`
2. Merge the PR
3. Verify release workflow runs and creates:
   - New git tag
   - GitHub release
   - Maven Central publication
   - GitHub Packages publication

## 🔍 Troubleshooting

### Common Issues

#### Maven Central Publishing Fails
- ✅ Verify `OSSRH_USERNAME` and `OSSRH_PASSWORD` are correct
- ✅ Check PGP signing key is valid and password is correct
- ✅ Ensure namespace `io.github.jdumasleon` is verified in Central Portal

#### Version Bumping Issues
- ✅ Check `gradle/libs.versions.toml` has `jarvisVersion` entry
- ✅ Verify PR title format matches expected patterns

#### Build Failures
- ✅ Ensure all dependencies are available
- ✅ Check Java version compatibility
- ✅ Verify Gradle wrapper is committed

### Debug Commands
```bash
# Test local build
./gradlew build

# Test Maven publishing locally
./gradlew :jarvis:publishReleasePublicationToLocalRepository

# Check current version
grep jarvisVersion gradle/libs.versions.toml
```

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Central Publishing Guide](https://central.sonatype.org/publish/)
- [Sentry Releases Documentation](https://docs.sentry.io/product/releases/)
- [PostHog API Documentation](https://posthog.com/docs/api)

---

💡 **Tip**: Start with the required secrets only, then add optional integrations (Sentry, PostHog) once the basic workflow is functioning.