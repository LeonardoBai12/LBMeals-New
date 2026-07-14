# LBMeals

Offline-first recipes app built on [TheMealDB](https://www.themealdb.com/api.php), running on **Android and iOS from a single Compose Multiplatform codebase**. It is the full rewrite of [LBMeals](https://github.com/leonardobai/LBMeals) — one of my first projects — rebuilt as a reference implementation of a modern multiplatform architecture: feature-sliced modules, Clean Architecture layers, MVI-style unidirectional data flow, SQLDelight as single source of truth, Ktor, Koin, and convention plugins.

## The offline-first pattern

**The UI never renders a network response. The UI observes the database. The network only writes into the database.**

```
┌──────────────────── presentation (Compose Multiplatform) ─┐
│  Screen (stateless Composable)                             │
│    │ observes StateFlow<State>       │ reports onEvent()   │
│    ▼                                 ▼                     │
│  ViewModel = state machine (state, event) -> state         │
│    effects: SharedFlow (snackbar/navigation, one-shot)     │
└─────┬──────────────────────────────────┬───────────────────┘
      │ collects Flow (read path)        │ Flow<Resource<T>> (sync path)
┌─────▼──────────────────────────────────▼───────────────────┐
│  domain: use cases + repository INTERFACE (pure Kotlin)     │
└─────▲───────────────────────────────────────────────────────┘
      │ implements
┌─────┴───────────────────────────────────────────────────────┐
│  data: RepositoryImpl                                        │
│    read:  databaseService.observe...() -> map to domain      │
│    sync:  service.get...() -> databaseService.replace()      │
└───────▲────────────────────────────────┬─────────────────────┘
        │ Flow re-emits on every write   │ writes
┌───────┴────────────┐         ┌─────────▼─────────┐
│ SQLDelight (SSOT)  │◀────────│ Ktor + kotlinx     │
│ behind             │ replace │ serialization DTOs │
│ DatabaseService    │         └────────────────────┘
└────────────────────┘
```

- Opening a screen shows cached data instantly; a one-shot sync runs in parallel.
- SQLDelight's query listeners re-emit after every write — the screen updates live without being told.
- A sync failure becomes a snackbar; cached content never disappears.
- Sync use cases return `Flow<Resource<T>>` (`Loading` → `Success`/`Error`), where `Success` carries what was synced — which is how the ViewModel tells *"empty because nothing exists"* apart from *"empty because data hasn't arrived yet"*. **`Resource` stops at the ViewModel**: a single `when` folds it into plain state fields (`isLoading`, `hasSyncFailed`), so screens only ever see data they can render directly.

## Module graph

```
:composeApp ───────────────────────┐  (composition root: Koin modules, NavHost,
  │                                │   Android Application / iOS MainViewController)
  ├─▶ :feature:categories:presentation ─▶ :feature:categories:domain ◀─ :feature:categories:data
  ├─▶ :feature:meals:presentation      ─▶ :feature:meals:domain      ◀─ :feature:meals:data
  │        │                                                              │        │
  │        ▼                                                              ▼        ▼
  ├─▶ :core:designsystem                                        :core:network  :core:database-api
  └─▶ :core:database-sqldelight  (implements :core:database-api) ─────────────────┘
```

Rules that keep the graph healthy:

- **Every module is Kotlin Multiplatform**: business logic, data and UI live in `commonMain`; platform code (SQL driver, HTTP engine, Material You colors) is an `expect`/`actual` at the edges.
- **Features are vertical slices** (`domain`/`data`/`presentation`); features never depend on other features — cross-feature navigation is resolved by `:composeApp` through callbacks.
- **`:core:database-api` abstracts persistence**: data layers depend on `CategoriesDatabaseService`/`MealsDatabaseService` interfaces (interface segregation), and `:core:database-sqldelight` provides the engine — its name says which one. The Room→SQLDelight migration touched only that module, which is the Liskov substitution principle paying rent.
- **DI is Koin modules per layer** (`factory` for use cases, `single` for repositories/services, `viewModel` with route parameters), aggregated once in `:composeApp`.
- **Build logic lives in convention plugins** (`build-logic/`): each module's `build.gradle.kts` is a handful of lines, and Detekt (with the formatting ruleset) is applied to every module automatically, scanning all KMP source sets.

## Improvements over the original LBMeals

The [original LBMeals](https://github.com/leonardobai/LBMeals) was a single-module Android app. This rewrite keeps the product and rebuilds everything around it:

| | LBMeals (2023) | LBMeals-New |
|---|---|---|
| Platforms | Android | **Android + iOS** (Compose Multiplatform) |
| Modules | 1 (`feature_x` packages) | 13 Gradle modules, api/impl separation |
| DI | Hilt (kapt) | **Koin** (multiplatform, no codegen) |
| HTTP | Retrofit | **Ktor** (Android/Darwin engines) |
| Persistence | Room | **SQLDelight** (typed SQL, native driver on iOS) |
| Data strategy | cache-then-network mixed in one flow | **offline-first**: reads only observe the DB, syncs only write into it |
| State | `mutableStateOf` mutated per callback; `Resource` with nullable data | Single immutable `StateFlow<State>` of plain fields per screen; sealed `Resource` (non-null `Success`) lives only between use case and ViewModel — loading only when data is genuinely on its way |
| UI | aligned fixed grid, opaque bars | **Staggered masonry grid** respecting each image's aspect ratio (with placeholder ratio while Coil loads), **frosted-glass top bar** (backdrop blur via Haze) that content scrolls behind, floating translucent search bar, **collapsing details header** that shrinks and fades on scroll, YouTube-branded action button, Material You dynamic color on Android 12+ |
| Pull-to-refresh | — | Indicator driven only by user-initiated refreshes; the automatic sync on entry never flashes it |
| Build scripts | Groovy, versions inline | Version catalog + **convention plugins** + Detekt |
| Unit tests | 1 | **33** (use cases, repositories and ViewModels — events in, state/effects out) with **MocKMP** (multiplatform mocks) and Turbine |
| UI tests | template placeholder | **19 instrumented Compose tests** covering every screen state |
| CI | — | GitHub workflows for code style (Detekt) and unit tests |

## Stack

Kotlin 2.3 · Compose Multiplatform 1.9 · Material 3 · Navigation Compose (type-safe routes) · Koin 4 · SQLDelight 2 · Ktor 3 · kotlinx.serialization · Coil 3 · Haze (backdrop blur) · MocKMP + Turbine · Detekt · Gradle convention plugins + version catalog.

## Build & test

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS (or open iosApp/iosApp.xcodeproj in Xcode)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17' build

# Unit tests (common code, runs on the JVM)
./gradlew testDebugUnitTest

# Instrumented Compose tests (device/emulator)
./gradlew connectedDebugAndroidTest

# Code style
./gradlew detekt
```
