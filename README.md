# LBMeals-New

Offline-first recipes app built on [TheMealDB](https://www.themealdb.com/api.php), written as a reference implementation of a modern multi-module Android architecture: feature-sliced modules, Clean Architecture layers, MVVM with MVI-style state/events (Unidirectional Data Flow), Room as single source of truth, Retrofit, Hilt, Compose and Coil.

## The offline-first pattern

**The UI never renders a network response. The UI observes the database. The network only writes into the database.**

```
┌──────────────────── presentation ────────────────────┐
│  Screen (stateless Composable)                        │
│    │ observes StateFlow<State>   │ reports onEvent()  │
│    ▼                             ▼                    │
│  ViewModel = state machine (state, event) -> state    │
│    effects: SharedFlow (snackbar/navigation, 1-shot)  │
└─────┬──────────────────────────────┬──────────────────┘
      │ collects Flow (read path)    │ suspend refresh() (sync path)
┌─────▼──────────────────────────────▼──────────────────┐
│  domain: use cases + repository INTERFACE (pure Kotlin)│
└─────▲──────────────────────────────────────────────────┘
      │ implements
┌─────┴──────────────────────────────────────────────────┐
│  data: RepositoryImpl                                   │
│    read:  databaseService.observe...() -> map to domain │
│    sync:  service.get...() -> databaseService.replace() │
└───────▲───────────────────────────────┬─────────────────┘
        │ Flow re-emits on every write  │ writes
┌───────┴────────────┐        ┌─────────▼─────────┐
│ Room (SSOT)        │◀───────│ Retrofit + kotlinx │
│ behind             │ replace│ serialization DTOs │
│ DatabaseService    │        └────────────────────┘
└────────────────────┘
```

- Opening a screen shows cached data instantly; a one-shot `refresh()` runs in parallel.
- Room's invalidation tracker re-emits the query after every write — the screen updates live without being told.
- A sync failure becomes a snackbar; cached content never disappears.
- Sync triggers: screen open (`init`), pull-to-refresh, and a periodic `SyncCategoriesWorker` (WorkManager + Hilt) that runs the same use case in the background.

## Module graph

```
:app ──────────────────────────────┐  (composition root: Hilt wiring, NavHost, WorkManager)
  ├─▶ :feature:categories:presentation ─▶ :feature:categories:domain   ◀─ :feature:categories:data
  ├─▶ :feature:meals:presentation      ─▶ :feature:meals:domain        ◀─ :feature:meals:data
  │        │                                                                │        │
  │        ▼                                                                ▼        ▼
  ├─▶ :core:designsystem                                          :core:network  :core:database-api
  └─▶ :core:database  (Room; implements :core:database-api)                          ▲
                                                                                      │
                                            :core:database ──────────────────────────┘
```

Rules that keep the graph healthy:

- **Features are vertical slices** (`domain`/`data`/`presentation` each); features never depend on other features — cross-feature navigation is resolved by `:app` through callbacks.
- **Domain modules are pure Kotlin** (no Android), injectable via `javax.inject`.
- **`:core:database-api` abstracts persistence**: the data layers depend on `CategoriesDatabaseService`/`MealsDatabaseService` interfaces (interface segregation), and `:core:database` provides the Room implementation. Swap the engine without touching any feature (Liskov substitution).
- **All internal edges use `implementation`**, so ABI changes don't ripple; KSP processors (Room, Hilt, serialization) each run only in their own module.

## Stack

Kotlin 2.2 · Compose (BOM 2025.09) · Material 3 · Navigation Compose (type-safe routes) · Hilt · Room · Retrofit + kotlinx.serialization · Coil 3 · WorkManager · Gradle version catalog + typesafe project accessors.

## Build

```
./gradlew :app:assembleDebug
```
