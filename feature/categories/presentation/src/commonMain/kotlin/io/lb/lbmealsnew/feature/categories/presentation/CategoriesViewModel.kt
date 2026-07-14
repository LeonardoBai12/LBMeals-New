package io.lb.lbmealsnew.feature.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.usecase.ObserveCategoriesUseCase
import io.lb.lbmealsnew.feature.categories.domain.usecase.RefreshCategoriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State machine of the categories screen: the current state lives in [state],
 * [CategoriesEvent] enumerates every accepted event, and [onEvent] is the
 * transition function.
 *
 * Read path: the database flow is combined with the search query and the sync
 * status into a single state — the UI never sees a network response.
 * Sync path: [sync] fetches and writes into the database; the content arrives
 * through the read path, while [syncStatus] tracks only the attempt's outcome.
 */
class CategoriesViewModel(
    observeCategories: ObserveCategoriesUseCase,
    private val refreshCategories: RefreshCategoriesUseCase,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    /**
     * The latest sync attempt, as reported by the refresh use case's flow. A
     * [Resource.Success] carries what the sync stored, which is what tells
     * "empty because nothing exists" apart from "empty because data hasn't
     * arrived yet".
     */
    private val syncStatus = MutableStateFlow<Resource<List<Category>>>(Resource.Loading)

    /**
     * Whether a sync the user asked for (pull gesture or Retry) is running.
     * The automatic sync on entry stays out of it, so the pull-to-refresh
     * indicator never flashes on its own.
     */
    private val isUserRefreshing = MutableStateFlow(false)

    val state: StateFlow<CategoriesState> = combine(
        observeCategories(),
        searchQuery,
        syncStatus,
        isUserRefreshing,
    ) { categories, query, sync, userRefreshing ->
        CategoriesState(
            categories = categoriesResource(categories, query, sync),
            searchQuery = query,
            isRefreshing = userRefreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CategoriesState(),
    )

    private val _effects = MutableSharedFlow<CategoriesEffect>()
    val effects: SharedFlow<CategoriesEffect> = _effects.asSharedFlow()

    init {
        sync(userInitiated = false)
    }

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            is CategoriesEvent.OnSearchQueryChange -> searchQuery.value = event.query

            CategoriesEvent.OnRefresh -> sync(userInitiated = true)

            is CategoriesEvent.OnCategoryClick -> viewModelScope.launch {
                _effects.emit(CategoriesEffect.NavigateToMeals(event.name))
            }
        }
    }

    /**
     * What the screen should show, given what is stored and how the latest
     * sync went. Loading is only ever produced while data is genuinely
     * expected to arrive.
     */
    private fun categoriesResource(
        stored: List<Category>,
        query: String,
        sync: Resource<List<Category>>,
    ): Resource<List<Category>> = when {
        stored.isNotEmpty() -> Resource.Success(
            stored.filter { it.name.contains(query, ignoreCase = true) },
        )

        // Nothing stored: only the sync outcome can tell whether data is on
        // its way, the fetch failed, or there is genuinely nothing to show.
        else -> when (sync) {
            Resource.Loading -> Resource.Loading

            is Resource.Error -> Resource.Error(sync.message)

            is Resource.Success ->
                if (sync.data.isEmpty()) {
                    Resource.Success(emptyList())
                } else {
                    // The sync stored rows the database flow hasn't re-emitted
                    // yet — they are about to arrive.
                    Resource.Loading
                }
        }
    }

    private fun sync(userInitiated: Boolean) {
        viewModelScope.launch {
            refreshCategories().collect { sync ->
                syncStatus.value = sync
                if (userInitiated) {
                    isUserRefreshing.value = sync is Resource.Loading
                }
                if (sync is Resource.Error) {
                    // A sync failure never destroys content — cached data
                    // stays on screen and the error becomes a transient
                    // message. The status only matters when there is no cache
                    // to fall back on.
                    _effects.emit(CategoriesEffect.ShowSnackbar("Couldn't refresh categories"))
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
