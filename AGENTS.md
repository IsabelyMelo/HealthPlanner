# Repository Guidelines

## Project Structure & Module Organization

HealthPlanner is a single-module Android project. The `app/` module contains all application code and resources. Java sources live under `app/src/main/java/com/example/healthplanner/`, with domain models in `models/`. XML layouts and values are in `app/src/main/res/`, including `layout/activity_main.xml`, `values/`, `values-night/`, drawable assets, launcher icons, and backup/data extraction XML. Local JVM tests belong in `app/src/test/java/`; instrumented Android tests belong in `app/src/androidTest/java/`. Gradle configuration is split between root `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`, and `gradle/libs.versions.toml`.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

- `./gradlew.bat assembleDebug` builds a debug APK on Windows.
- `./gradlew.bat test` runs local JVM unit tests.
- `./gradlew.bat connectedAndroidTest` runs instrumented tests on a connected device or emulator.
- `./gradlew.bat lint` runs Android lint checks.

On Unix-like shells, use `./gradlew` with the same tasks.

## Coding Style & Naming Conventions

This project uses Java 11 and AndroidX. Keep Java indentation at 4 spaces, place braces on the same line, and keep package names under `com.example.healthplanner`. Use `PascalCase` for classes and enums (`Meal`, `UserSettings`), `camelCase` for fields and methods, and uppercase enum constants (`SNACK`). Keep model classes simple, with private fields and public constructors/getters/setters unless a stronger Android pattern is introduced. XML resource names should be lowercase with underscores, for example `activity_main.xml`.

## Testing Guidelines

Use JUnit 4 for local tests and AndroidX/JUnit/Espresso for instrumented tests. Name test methods after the behavior being verified, such as `fromValue_returnsSnackForNull`. Put pure Java logic tests in `app/src/test/java`; use `app/src/androidTest/java` only when Android framework, UI, device, or emulator behavior is required. Run `./gradlew.bat test` before opening a pull request.

## Commit & Pull Request Guidelines

The current history uses short, descriptive commits, including Portuguese messages. Keep commits focused and imperative when possible, for example `Add meal validation tests`. Pull requests should include a brief summary, test results, linked issue if applicable, and screenshots or recordings for UI changes.

## Security & Configuration Tips

Do not commit machine-specific files such as `local.properties`, `.gradle/`, build outputs, or IDE workspace files. Keep SDK paths and signing credentials outside version control.
