package io.lb.lbmealsnew.feature.meals.presentation.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.usecase.ObserveMealsByCategoryUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.RefreshMealsByCategoryUseCase
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
 * State machine of the meals screen. Same shape as the categories one: read
 * path combines the database flow with the search query and the sync status;
 * sync path writes into the database and the content comes back through the
 * read path.
 */
class MealsViewModel(
    private val category: String,
    observeMealsByCategory: ObserveMealsByCategoryUseCase,
    private val refreshMealsByCategory: RefreshMealsByCategoryUseCase,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    /**
     * The latest sync attempt, as reported by the refresh use case's flow. A
     * [Resource.Success] carries what the sync stored, which is what tells
     * "empty because nothing exists" apart from "empty because data hasn't
     * arrived yet".
     */
    private val syncStatus = MutableStateFlow<Resource<List<Meal>>>(Resource.Loading)

    /**
     * Whether a sync the user asked for (pull gesture or Retry) is running.
     * The automatic sync on entry stays out of it, so the pull-to-refresh
     * indicator never flashes on its own.
     */
    private val isUserRefreshing = MutableStateFlow(false)

    val state: StateFlow<MealsState> = combine(
        observeMealsByCategory(category),
        searchQuery,
        syncStatus,
        isUserRefreshing,
    ) { meals, query, sync, userRefreshing ->
        MealsState(
            category = category,
            meals = mealsResource(meals, query, sync),
            searchQuery = query,
            isRefreshing = userRefreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MealsState(category = category),
    )

    private val _effects = MutableSharedFlow<MealsEffect>()
    val effects: SharedFlow<MealsEffect> = _effects.asSharedFlow()

    init {
        sync(userInitiated = false)
    }

    fun onEvent(event: MealsEvent) {
        when (event) {
            is MealsEvent.OnSearchQueryChange -> searchQuery.value = event.query

            MealsEvent.OnRefresh -> sync(userInitiated = true)

            MealsEvent.OnBackClick -> viewModelScope.launch {
                _effects.emit(MealsEffect.NavigateBack)
            }

            is MealsEvent.OnMealClick -> viewModelScope.launch {
                _effects.emit(MealsEffect.NavigateToDetails(event.id, event.name))
            }
        }
    }

    /**
     * What the screen should show, given what is stored and how the latest
     * sync went. Loading is only ever produced while data is genuinely
     * expected to arrive.
     */
    private fun mealsResource(
        stored: List<Meal>,
        query: String,
        sync: Resource<List<Meal>>,
    ): Resource<List<Meal>> = when {
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
            refreshMealsByCategory(category).collect { sync ->
                syncStatus.value = sync
                if (userInitiated) {
                    isUserRefreshing.value = sync is Resource.Loading
                }
                if (sync is Resource.Error) {
                    // A sync failure never destroys content — cached data
                    // stays on screen and the error becomes a transient
                    // message. The status only matters when there is no cache
                    // to fall back on.
                    _effects.emit(MealsEffect.ShowSnackbar("Couldn't refresh meals"))
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
