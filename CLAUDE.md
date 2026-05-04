
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Period Vibe** is a lightweight Android period tracking app built with modern Android technologies. It focuses on providing a simple, intuitive user experience for menstrual cycle management.

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM + Clean Architecture
- **Local Storage:** Room Database
- **Dependency Injection:** Hilt
- **Navigation:** Jetpack Navigation Compose
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 36

## Architecture Overview

The project follows Clean Architecture with three main layers:

1. **Data Layer**: `data/` - Room database, repositories, mappers, DAOs, entities
2. **Domain Layer**: `domain/` - Use cases, domain models, repository interfaces
3. **UI Layer**: `ui/`, `navigation/` - Screens, ViewModels, components, navigation

## Key Packages & Files

```
app/src/main/java/com/example/periodvibe/
├── data/
│   ├── local/                  # Room database
│   │   ├── dao/               # Data access objects
│   │   ├── entity/            # Database entities
│   │   ├── converter/         # Type converters
│   │   └── AppDatabase.kt     # Database class
│   ├── repository/            # Data repository implementations
│   ├── mapper/                # Entity <-> Domain model mappers
│   └── exportimport/          # CSV/JSON export/import
├── domain/
│   ├── model/                 # Domain models
│   ├── repository/            # Repository interfaces
│   └── usecase/               # Use cases
├── di/                        # Hilt modules
├── ui/                        # UI layer
│   ├── home/                 # Home screen
│   ├── calendar/             # Calendar screen
│   ├── history/              # History screen
│   ├── settings/             # Settings screen
│   ├── onboarding/           # Onboarding screens
│   ├── setup/                # Initial setup screen
│   ├── applock/              # App lock screens
│   └── theme/                # Compose theme
├── navigation/                # Navigation setup
├── util/                      # Utilities
└── utils/                     # More utilities
```

## Build & Run

### Gradle Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Install debug APK
./gradlew installDebug

# Run tests
./gradlew test

# Build release APK
./gradlew assembleRelease
```

### Project Structure
- **Root `build.gradle.kts`**: Project-wide build configuration
- **App `app/build.gradle.kts`**: App-specific build configuration
- **`settings.gradle.kts`**: Project settings and module structure

## Code Style & Guidelines

1. **Architecture**: Follow Clean Architecture principles - keep UI layer thin, business logic in domain layer
2. **Compose**: Use state hoisting, `remember`, and `derivedStateOf` appropriately
3. **Coroutines**: Use `viewModelScope` for ViewModel coroutines, `Dispatchers.IO` for database operations
4. **Hilt**: Use `@HiltViewModel` for ViewModels, inject dependencies with `@Inject`

## Key Features

1. **Cycle Tracking**: Record period start/end, flow levels
2. **Calendar View**: Visualize cycle history and predictions
3. **History**: View past cycles and daily records
4. **Settings**: Configure cycle parameters, notifications, theme, privacy
5. **Onboarding**: First-time user setup
6. **Data Import/Export**: CSV/JSON support
7. **App Lock**: PIN protection for privacy

## Important Notes

- Database migrations are handled in `DatabaseModule.kt`
- Navigation uses type-safe Compose Navigation in `PeriodVibeNavHost.kt`
- The app supports light/dark/system themes
- Data is stored locally on the device (no cloud sync)
