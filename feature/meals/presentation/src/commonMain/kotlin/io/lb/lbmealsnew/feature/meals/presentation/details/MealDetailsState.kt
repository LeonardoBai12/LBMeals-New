package io.lb.lbmealsnew.feature.meals.presentation.details

import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails

/**
 * Everything the meal details screen IS right now, as a single immutable
 * object of plain fields — the ViewModel folds the sync's `Resource` into
 * them, so the screen never sees the wrapper.
 *
 * @property mealName Name known from the list screen — lets the title render
 * before the details arrive.
 * @property details The stored details; null until the first sync lands.
 * @property isLoading Whether the details are genuinely expected to arrive:
 * nothing stored while the first emission or a sync is pending.
 * @property hasSyncFailed Whether the latest sync failed with nothing stored
 * to fall back on.
 * @property isRefreshing Whether a sync the user asked for is running —
 * drives the pull-to-refresh indicator only.
 */
data class MealDetailsState(
    val mealName: String = "",
    val details: MealDetails? = null,
    val isLoading: Boolean = true,
    val hasSyncFailed: Boolean = false,
    val isRefreshing: Boolean = false,
)
