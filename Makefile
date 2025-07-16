# Makefile for Android SDK linting and formatting

# Variables
GRADLE = ./gradlew

# Default target
.PHONY: all
all: lint format

# Run detekt linting
.PHONY: lint
lint:
	@echo "Running detekt..."
	$(GRADLE) detekt

# Run ktlint linting
.PHONY: format-check
format-check:
	@echo "Running ktlint check..."
	$(GRADLE) ktlintCheck

# Auto-fix formatting issues with ktlint
.PHONY: format
format:
	@echo "Running ktlint format..."
	$(GRADLE) ktlintFormat

# Run all quality checks
.PHONY: check
check: lint format-check

# Build the project
.PHONY: build
build:
	@echo "Building Android SDK..."
	$(GRADLE) build

# Run tests
.PHONY: test
test:
	@echo "Running tests..."
	$(GRADLE) test

# Clean build artifacts
.PHONY: clean
clean:
	@echo "Cleaning build artifacts..."
	$(GRADLE) clean

# Run detekt with baseline generation
.PHONY: detekt-baseline
detekt-baseline:
	@echo "Generating detekt baseline..."
	$(GRADLE) detektBaseline

# Full CI workflow
.PHONY: ci
ci: clean build test check

# Help target
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  all           - Run lint and format"
	@echo "  lint          - Run detekt"
	@echo "  format-check  - Check formatting with ktlint"
	@echo "  format        - Format code with ktlint"
	@echo "  check         - Run all quality checks"
	@echo "  build         - Build the Android SDK"
	@echo "  test          - Run tests"
	@echo "  clean         - Clean build artifacts"
	@echo "  detekt-baseline - Generate detekt baseline"
	@echo "  ci            - Full CI workflow"
	@echo "  help          - Show this help message"