# Git Hooks

This directory contains Git hooks that help maintain code quality.

## Pre-commit Hook

The pre-commit hook runs code quality checks before allowing commits:

- **ktlint**: Kotlin code style checks
- **detekt**: Static code analysis
- **lint**: Android lint checks

### Installation

To install the pre-commit hook, run this command from the project root:

```bash
cp hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Usage

Once installed, the hook will automatically run before each commit. If any issues are found, the commit will be blocked and you'll need to fix the issues before committing.

To bypass the hook (not recommended), use:

```bash
git commit --no-verify
```

### Manual Execution

You can also run the checks manually:

```bash
# Run all checks
./hooks/pre-commit

# Run individual checks
./gradlew ktlintCheck
./gradlew detekt
./gradlew lint
```