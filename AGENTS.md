# Repository Guidelines

## Project Structure & Module Organization

HealthPlanner is a single-module Android project. The `app/` module contains all application code and resources. Java sources live under `app/src/main/java/com/example/healthplanner/`. Activities live directly in that package: `MainActivity`, `GoalActivity`, `MealActivity`, and `HistoryActivity`. Domain models are in `models/`; RecyclerView adapters are in `adapters/`; SQLite persistence is centralized in `database/DatabaseHelper.java`. XML layouts and values are in `app/src/main/res/`, including dashboard, goal, meal, history, dialog, and item layouts, plus `values/`, `values-night/`, drawable assets, launcher icons, and backup/data extraction XML. Project documentation currently includes `docs/class_diagram.mmd`. Local JVM tests belong in `app/src/test/java/`; instrumented Android tests belong in `app/src/androidTest/java/`. Gradle configuration is split between root `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`, and `gradle/libs.versions.toml`.

## App Architecture & Data Flow

The app uses classic Android Views, AppCompat/Material components, RecyclerViews, dialogs, and direct SQLite access. `MainActivity` is the dashboard: it refreshes in `onResume()`, reads the active goal, totals today's calories, updates the progress bar, and navigates to the goal, meal, and history screens. `GoalActivity` creates and ends calorie goals. `MealActivity` creates and deletes meals for the current day. `HistoryActivity` lists days that have meals and expands each day to show the active goal, progress, and meal details.

`DatabaseHelper` owns the `health_planner.db` schema and should remain the single place for SQL table definitions, queries, and migrations unless a larger data layer is intentionally introduced. The current schema version is `3`. Dates are stored as ISO-8601 `LocalDateTime` text using `DateTimeFormatter.ISO_LOCAL_DATE_TIME`. An active goal is represented by a `NULL` `end_date`; inserting a new active goal closes the previous one or deletes same-day replacements. Meals are queried by start/end-of-day ranges, and history days are derived from the first 10 characters of the meal timestamp.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

- `./gradlew.bat assembleDebug` builds a debug APK on Windows.
- `./gradlew.bat test` runs local JVM unit tests.
- `./gradlew.bat connectedAndroidTest` runs instrumented tests on a connected device or emulator.
- `./gradlew.bat lint` runs Android lint checks.

On Unix-like shells, use `./gradlew` with the same tasks. The wrapper may download the configured Gradle distribution on first use, so the first run can require network access. The app currently targets Java 11, `minSdk` 26, and `targetSdk` 36.

## Coding Style & Naming Conventions

This project uses Java 11, AndroidX, AppCompat, ConstraintLayout, and Material components. Keep Java indentation at 4 spaces, place braces on the same line, and keep package names under `com.example.healthplanner`. Use `PascalCase` for classes and enums (`Meal`, `UserSettings`), `camelCase` for fields and methods, and uppercase enum constants (`SNACK`). Keep model classes simple, with private fields and public constructors/getters/setters unless a stronger Android pattern is introduced. XML resource names should be lowercase with underscores, for example `activity_main.xml`. Match the existing view-based style when adding UI; do not introduce Compose, Room, ViewModel, or a repository layer unless the task explicitly calls for that broader refactor.

## Testing Guidelines

Use JUnit 4 for local tests and AndroidX/JUnit/Espresso for instrumented tests. The current test files are placeholders, so add focused coverage when changing behavior. Name test methods after the behavior being verified, such as `fromValue_returnsSnackForNull`. Put pure Java logic tests, model tests, and enum parsing tests in `app/src/test/java`; use `app/src/androidTest/java` when Android framework, SQLiteOpenHelper, UI, device, or emulator behavior is required. Run `./gradlew.bat test` before opening a pull request.

## Commit & Pull Request Guidelines

The current history uses short, descriptive commits, including Portuguese messages. Keep commits focused and imperative when possible, for example `Add meal validation tests`. Pull requests should include a brief summary, test results, linked issue if applicable, and screenshots or recordings for UI changes.

## Security & Configuration Tips

Do not commit machine-specific files such as `local.properties`, `.gradle/`, build outputs, or IDE workspace files. Keep SDK paths and signing credentials outside version control. The calorie estimation feature reads AI connection settings from an ignored root `.env` file: `AI_CALORIE_API_BASE_URL`, `AI_CALORIE_MODEL`, and `AI_CALORIE_API_KEY`. Keep real API keys out of git; use `.env.example` only for placeholder configuration. Because Android `BuildConfig` values are embedded in the APK, prefer a backend proxy before using real production credentials.
