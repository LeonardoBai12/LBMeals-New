package io.lb.lbmealsnew.feature.meals.presentation.listing

import io.lb.lbmealsnew.feature.meals.domain.model.Meal

/**
 * Everything the meals screen IS right now, as a single immutable object of
 * plain fields — the ViewModel folds the sync's `Resource` into them, so the
 * screen never sees the wrapper.
 *
 * @property category Category whose meals are on screen.
 * @property meals Meals to render, already filtered by [searchQuery].
 * @property searchQuery Current search bar text.
 * @property isLoading Whether data is genuinely expected to arrive: nothing
 * stored while the first emission or a sync is pending. An empty list by
 * itself never means loading.
 * @property hasSyncFailed Whether the latest sync failed with nothing stored
 * to fall back on.
 * @property isRefreshing Whether a sync the user asked for is running —
 * drives the pull-to-refresh indicator only.
 */
data class MealsState(
    val category: String = "",
    val meals: List<Meal> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val hasSyncFailed: Boolean = false,
    val isRefreshing: Boolean = false,
)
