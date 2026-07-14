package io.lb.lbmealsnew.feature.meals.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.usecase.ObserveMealDetailsUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.RefreshMealDetailsUseCase
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
 * State machine of the meal details screen. Same offline-first shape: the
 * read path observes the stored details (null until the first sync lands),
 * the sync path fetches by id and writes into the database.
 */
class MealDetailsViewModel(
    private val mealId: String,
    private val mealName: String,
    observeMealDetails: ObserveMealDetailsUseCase,
    private val refreshMealDetails: RefreshMealDetailsUseCase,
) : ViewModel() {

    /**
     * The latest sync attempt, as reported by the refresh use case's flow. A
     * [Resource.Success] means the details are stored, so nothing on screen
     * plus a successful sync can only be data on its way.
     */
    private val syncStatus = MutableStateFlow<Resource<MealDetails>>(Resource.Loading)

    /**
     * Whether a sync the user asked for (pull gesture or Retry) is running.
     * The automatic sync on entry stays out of it, so the pull-to-refresh
     * indicator never flashes on its own.
     */
    private val isUserRefreshing = MutableStateFlow(false)

    val state: StateFlow<MealDetailsState> = combine(
        observeMealDetails(mealId),
        syncStatus,
        isUserRefreshing,
    ) { details, sync, userRefreshing ->
        MealDetailsState(
            mealName = details?.name ?: mealName,
            details = when {
                details != null -> Resource.Success(details)

                sync is Resource.Error -> Resource.Error(sync.message)

                // Nothing stored and no failure: either the sync is still in
                // flight or it stored details the database flow hasn't
                // re-emitted yet — they are about to arrive.
                else -> Resource.Loading
            },
            isRefreshing = userRefreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MealDetailsState(mealName = mealName),
    )

    private val _effects = MutableSharedFlow<MealDetailsEffect>()
    val effects: SharedFlow<MealDetailsEffect> = _effects.asSharedFlow()

    init {
        sync(userInitiated = false)
    }

    fun onEvent(event: MealDetailsEvent) {
        when (event) {
            MealDetailsEvent.OnRefresh -> sync(userInitiated = true)

            MealDetailsEvent.OnBackClick -> viewModelScope.launch {
                _effects.emit(MealDetailsEffect.NavigateBack)
            }

            MealDetailsEvent.OnYoutubeClick -> viewModelScope.launch {
                (state.value.details as? Resource.Success)?.data?.youtubeUrl?.let {
                    _effects.emit(MealDetailsEffect.OpenUrl(it))
                }
            }
        }
    }

    private fun sync(userInitiated: Boolean) {
        viewModelScope.launch {
            refreshMealDetails(mealId).collect { sync ->
                syncStatus.value = sync
                if (userInitiated) {
                    isUserRefreshing.value = sync is Resource.Loading
                }
                if (sync is Resource.Error) {
                    // A sync failure never destroys content — cached data
                    // stays on screen and the error becomes a transient
                    // message. The status only matters when there is no cache
                    // to fall back on.
                    _effects.emit(MealDetailsEffect.ShowSnackbar("Couldn't refresh this recipe"))
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
