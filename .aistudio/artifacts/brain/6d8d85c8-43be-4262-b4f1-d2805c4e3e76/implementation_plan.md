# Fix GitHub Actions Build: Auto-Install Android SDK 36

Resolves the `Build debug APK: Process completed with exit code 1` error on GitHub Actions runners by automatically installing Android SDK 36 and accepting SDK licenses prior to compilation.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Decision**: We will automatically install Android SDK 36 in `.github/workflows/build-apk.yml` without modifying your app source code, dependencies, or Gradle configuration.

- **Root Cause Identified**: GitHub's standard `ubuntu-latest` runner image pre-installs Android SDK platforms up to API 35 (`android-35`). When Gradle tries to assemble this project (`compileSdk = 36`), it fails with `Failed to find target with hash string 'android-36'`.
- **Selected Resolution**: Add an explicit Android SDK setup step in the workflow to install `platforms;android-36` and accept licenses before running `gradle :app:assembleDebug`.

---

## 1. Overview & System Analysis

### Why the Build Failed
On GitHub Actions `ubuntu-latest`:
1. The runner only contains Android SDKs 33, 34, and 35 out of the box.
2. The project's `app/build.gradle.kts` targets Android SDK 36 (`compileSdk = 36`).
3. When Gradle executes `:app:assembleDebug`, it attempts to locate `platforms;android-36` in the runner's SDK directory and fails with exit code 1.

### The Solution
We will add `android-actions/setup-android@v3` to `.github/workflows/build-apk.yml` right after setting up Java 17. This step:
- Configures the Android SDK environment on the runner.
- Automatically downloads and installs `platforms;android-36` and `build-tools`.
- Automatically accepts all required Google SDK licenses.

---

## 2. Technical Architecture & Workflow Pipeline

```
┌─────────────────────────┐
│   Checkout Repository   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│     Set up Java 17      │ (Temurin JDK 17)
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Set up Android SDK 36  │ ◄── [NEW STEP] Installs platforms;android-36
└────────────┬────────────┘     and accepts licenses automatically
             │
             ▼
┌─────────────────────────┐
│    Set up Gradle 9.3.1  │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ Create Debug Keystore   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│     Build Debug APK     │ (gradle :app:assembleDebug) ───► ✅ SUCCEEDS
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│    Verify & Upload      │ (Artifact: app-debug-apk)
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ Publish GitHub Release  │ (Direct mobile download link)
└─────────────────────────┘
```

---

## 3. Implementation Plan

1. **Update `.github/workflows/build-apk.yml`**:
   - Insert `android-actions/setup-android@v3` with `packages: 'platforms;android-36 platform-tools'` right after `Verify Java environment` and before `Set up Gradle 9.3.1`.
   - Maintain all existing requirements: Gradle 9.3.1, Java 17, debug keystore generation, APK verification, artifact upload (`app-debug-apk`), and GitHub Release creation.

2. **Zero Modification to Application Code**:
   - No changes to `app/build.gradle.kts`, `gradle/libs.versions.toml`, source files, or resources.

---

## 4. Verification & Testing

- Validate workflow YAML syntax and structure.
- Verify `compile_applet` succeeds locally.
- Provide instructions for committing the workflow to GitHub and confirming the green build.
