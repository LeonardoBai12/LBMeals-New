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
    ) { stored, query, sync, userRefreshing ->
        // Resource stops here: it is folded into plain state fields, so
        // everything above the ViewModel only sees data it can render.
        MealsState(
            category = category,
            meals = stored.filter { it.name.contains(query, ignoreCase = true) },
            searchQuery = query,
            isLoading = stored.isEmpty() && isDataOnItsWay(sync),
            hasSyncFailed = stored.isEmpty() && sync is Resource.Error,
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
                _effects.emit(
                    MealsEffect.NavigateToDetails(event.id, event.name, event.thumbnailUrl),
                )
            }
        }
    }

    /**
     * Whether more data is genuinely about to arrive: the sync is still
     * running, or it succeeded with content the database flow hasn't
     * re-emitted yet.
     */
    private fun isDataOnItsWay(sync: Resource<List<Meal>>): Boolean = when (sync) {
        Resource.Loading -> true
        is Resource.Error -> false
        is Resource.Success -> sync.data.isNotEmpty()
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
                    _effects.emit(MealsEffect.ShowSnackBar("Couldn't refresh meals"))
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
