# Offline-first strategy

The rule that shapes every feature module in this app:

**The UI never renders a network response. The UI observes the database. The
network only writes into the database.**

There is no `if (online) ... else ...` branch anywhere in the codebase. A
network failure only affects whether the database gets *updated* — it never
decides whether the screen has anything to show.

## The two paths

Every repository splits cleanly into a **read path** and a **sync path**,
and they never call into each other:

```kotlin
internal class MealsRepositoryImpl(
    private val service: MealDbService,
    private val databaseService: MealsDatabaseService,
) : MealsRepository {

    // Read path: only ever touches the database.
    override fun observeMealsByCategory(category: String): Flow<List<Meal>> =
        databaseService.observeMealsByCategory(category)
            .map { meals -> meals.map { it.toDomain() } }

    // Sync path: fetches, then writes. Never returns the network
    // response directly to the caller as "the" data.
    override suspend fun refreshMealsByCategory(category: String): List<Meal> {
        val meals = service.getMealsByCategory(category).meals.orEmpty().map { it.toLocal(category) }
        databaseService.replaceMealsByCategory(category = category, meals = meals)
        return meals.map { it.toDomain() }
    }
}
```

The sync method's return value is barely used — what actually reaches the
screen is the *next emission* of `observeMealsByCategory`, triggered by the
write. The return value only exists so the use case can report whether the
attempt succeeded (see `Resource`, below).

## Observing the database via Flow

SQLDelight generates query objects whose `.asFlow()` re-emits automatically
every time a write touches the tables the query reads from — there is no
manual "notify the UI" step anywhere. `replaceMealsByCategory` writes,
and every active `observeMealsByCategory` collector gets a new list for free:

```kotlin
override suspend fun replaceMealsByCategory(category: String, meals: List<LocalMeal>) =
    withContext(Dispatchers.IO) {
        // Delete + insert inside a single transaction, so observers see
        // exactly one re-emission with the final rows (not a flicker to
        // empty and back).
        queries.transaction {
            queries.clearMealsByCategory(category)
            meals.forEach { meal -> queries.insertMeal(/* ... */) }
        }
    }
```

**Why a transaction matters:** without it, `clearMealsByCategory` and each
`insertMeal` would each trigger their own re-emission — collectors would
briefly see an empty list between the clear and the first insert. Wrapping
both in one `transaction { }` collapses that into a single emission of the
final state.

## Replace-from-remote: how sync writes

There are two shapes of "sync writes into the database", depending on whether
the remote payload is the *complete* set for a key or a single record:

- **Replace** (`replaceMealsByCategory`, `replaceCategories`): the remote
  response for "meals in category X" is the *entire* truth for that category
  — so the sync deletes everything previously stored for that key and
  inserts the fresh set. This is what makes deletions on the server show up
  locally too (a meal removed from the API disappears from the cache on the
  next sync, instead of lingering forever).
- **Upsert** (`upsertMealDetails`): a single meal's details are fetched and
  written by id, with no wider set to reconcile — insert-or-replace by
  primary key is enough.

Picking the right one per endpoint is the actual design decision — "replace"
is not automatically correct for a single-record fetch (it would need to
carry the full id, which would defeat caching entirely), and "upsert" is not
correct for a collection fetch (it would leave server-side deletions
invisible locally).

## `Resource`: reporting the sync attempt without leaking it

```kotlin
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String? = null) : Resource<Nothing>
}
```

Sync use cases wrap the repository call in a `flow { }` that always emits
`Loading` first, so the ViewModel can distinguish "the attempt is running"
from "nothing has happened yet":

```kotlin
class RefreshCategoriesUseCase(private val repository: CategoriesRepository) {
    operator fun invoke(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(repository.refreshCategories()))
    }.catch { emit(Resource.Error(it.message)) }
}
```

**Important rule, learned the hard way:** `Resource` lives *only* between the
use case and the ViewModel. It never reaches the screen, and it never sits
inside the UI `State` class. The first version of this code leaked `Resource`
all the way into `CategoriesState`, which coupled every screen to a type that
means nothing to a screen ("is `Success` with an empty list different from
`Loading`? screens shouldn't have to know"). The ViewModel folds it into plain
fields the screen can render directly, in one `when`.

## `combine`: merging the read path, the sync status and local UI flags

The ViewModel's `state` is a single `combine` of everything that can change
it — the database flow, the last sync outcome, and any purely-local flags
(search query, "is the user pulling to refresh") — folded into one immutable
state object per emission:

```kotlin
val state: StateFlow<CategoriesState> = combine(
    observeCategories(),   // read path: Flow<List<Category>>
    searchQuery,           // local UI state
    syncStatus,            // sync path: Resource<List<Category>>
    isUserRefreshing,      // local UI state
) { stored, query, sync, userRefreshing ->
    CategoriesState(
        categories = stored.filter { it.name.contains(query, ignoreCase = true) },
        searchQuery = query,
        isLoading = stored.isEmpty() && isDataOnItsWay(sync),
        hasSyncFailed = stored.isEmpty() && sync is Resource.Error,
        isRefreshing = userRefreshing,
    )
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesState())
```

The `isLoading`/`hasSyncFailed` logic only looks at `sync` when the database
is *already empty*. That single guard is what makes cached content survive a
failed sync: if `stored` isn't empty, the screen renders it regardless of
what the last sync attempt did — a failure becomes a snackbar, never a blank
screen or an error state stomping on real content.

```kotlin
private fun isDataOnItsWay(sync: Resource<List<Category>>): Boolean = when (sync) {
    Resource.Loading -> true
    is Resource.Error -> false
    is Resource.Success -> sync.data.isNotEmpty()
}
```

This helper is why `Resource.Success` needs to carry data instead of being a
bare marker: a successful sync that stored zero rows (a category genuinely
has no meals) must *not* be read as "still loading" forever.

## Sync on `init`: fetch without ever blocking the read path

Every ViewModel kicks off exactly one automatic sync when created, completely
decoupled from whether the database already has something to show:

```kotlin
init {
    sync(userInitiated = false)
}

private fun sync(userInitiated: Boolean) {
    viewModelScope.launch {
        refreshCategories().collect { sync ->
            syncStatus.value = sync
            if (userInitiated) {
                isUserRefreshing.value = sync is Resource.Loading
            }
            if (sync is Resource.Error) {
                _effects.emit(CategoriesEffect.ShowSnackBar("Couldn't refresh categories"))
            }
        }
    }
}
```

Two details make this behave correctly for both the automatic sync and a
user-triggered one (pull-to-refresh, a Retry button):

- **`userInitiated` gates the refresh indicator.** The automatic sync on
  entry updates `syncStatus` (which can affect `isLoading`/`hasSyncFailed`),
  but it deliberately does **not** touch `isUserRefreshing` — otherwise the
  pull-to-refresh spinner would flash on every screen open, not just when the
  user actually pulled.
- **A failed sync only ever becomes a snackbar**, never a mutation of
  `categories`. The cached list a user is currently reading is never replaced
  by an error state — this is the same guarantee `isDataOnItsWay` gives on
  the read side, just from the sync side.

## Putting it together

```
Screen  --collects-->  StateFlow<State>  <--combine--  ViewModel
                              ▲                            │
                              │ observe (Flow, read path)   │ sync (Flow<Resource<T>>)
                              │                             ▼
                        Repository.observe...()     Repository.refresh...()
                              ▲                             │
                     SQLDelight query.asFlow()               │ service.get...()
                              │                             ▼
                        (SQLDelight tables)  <--- replace/upsert ---  Ktor + DTOs
```

Opening a screen shows cached data instantly (or an empty/loading state, the
first time), the sync runs in the background, and when it writes, the same
observed `Flow` re-emits — the screen updates itself without the ViewModel
ever manually pushing the fetched data into its own state.
