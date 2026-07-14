# LBMeals

Offline-first recipes app built on [TheMealDB](https://www.themealdb.com/api.php), running on **Android and iOS from a single Compose Multiplatform codebase**. It is the third generation of the same idea: my first app ever, [MyFirstJavaApp](https://github.com/LeonardoBai12/MyFirstJavaApp), consumed this exact API in Java — this rewrite rebuilds it as a reference implementation of a modern multiplatform architecture: feature-sliced modules, Clean Architecture layers, MVI-style unidirectional data flow, SQLDelight as single source of truth, Ktor, Koin, and convention plugins.

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

Full write-up, with the actual `combine`/`init`/replace-vs-upsert code: [docs/offline-first.md](docs/offline-first.md).

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

## Three generations of the same app

This is the third time I build a recipes app on TheMealDB, which makes the codebase an honest measure of evolution — from [MyFirstJavaApp](https://github.com/LeonardoBai12/MyFirstJavaApp), literally the first app I ever wrote, through [LBMeals](https://github.com/LeonardoBai12/LBMeals), to this rewrite:

| | [MyFirstJavaApp](https://github.com/LeonardoBai12/MyFirstJavaApp) | [LBMeals](https://github.com/LeonardoBai12/LBMeals) | LBMeals-New |
|---|---|---|---|
| Platforms | Android | Android | **Android + iOS** |
| Language | Java | Kotlin | Kotlin Multiplatform |
| UI | XML + RecyclerView + AndroidAnnotations | Jetpack Compose | **Compose Multiplatform** |
| Modules | 1 | 1 (`feature_x` packages) | 13 Gradle modules, api/impl separation |
| Architecture | Activity-centric: the ViewModel shows `ProgressDialog`/`Toast` itself, views held in `static` fields | MVVM with Clean-ish layers per package | Clean Architecture + MVI-style UDF per feature module |
| DI | — (AndroidAnnotations codegen) | Hilt (kapt) | **Koin** (multiplatform, no codegen) |
| HTTP | Retrofit with manual callbacks | Retrofit + coroutines | **Ktor** (Android/Darwin engines) |
| Persistence | none — network only, category hardcoded to `"Chicken"` | Room, cache-then-network mixed in one flow | **SQLDelight, offline-first**: reads only observe the DB, syncs only write into it |
| State | `MutableLiveData` mutated from network callbacks | `mutableStateOf` per callback; `Resource` with nullable data | Single immutable `StateFlow<State>` of plain fields; sealed `Resource` (non-null `Success`) lives only between use case and ViewModel — loading only when data is genuinely on its way |
| UI polish | fixed list | aligned grid, opaque bars | **Staggered masonry grid** respecting image ratios, **frosted-glass bars** (Haze backdrop blur), search bar that hides on scroll, shared element transitions, **collapsing details header** with a fading hero image, **YouTube preview card** with the video's real thumbnail, Material You on Android 12+. Write-up: [docs/ui-experiments.md](docs/ui-experiments.md) |
| Pull-to-refresh | SwipeRefreshLayout | driven by any sync | Indicator driven only by user-initiated refreshes; the automatic sync on entry never flashes it |
| Build scripts | Groovy, versions inline | Groovy-era catalog-less scripts | Version catalog + **convention plugins** + Detekt |
| Tests | template placeholders | 1 unit test | **33 unit tests** (use cases, repositories, ViewModels — events in, state/effects out; MocKMP + Turbine) and **19 instrumented Compose tests** |
| CI | — | — | GitHub workflows for code style (Detekt) and unit tests |

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
