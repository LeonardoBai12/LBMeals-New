package io.lb.lbmealsnew.feature.meals.presentation.listing

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.Meal

/**
 * Everything the meals screen IS right now, as a single immutable object.
 * Survives rotation through the ViewModel's StateFlow.
 *
 * @property category Category whose meals are on screen.
 * @property meals The meals on their way to the screen: [Resource.Loading]
 * while data is genuinely expected to arrive, [Resource.Error] when the sync
 * failed with nothing cached, and [Resource.Success] once there is content to
 * render (already filtered by [searchQuery] — possibly empty).
 * @property searchQuery Current search bar text.
 * @property isRefreshing Whether a network sync is in flight over content
 * already on screen — drives the pull-to-refresh indicator only; the first
 * load shows the full-screen loading instead.
 */
data class MealsState(
    val category: String = "",
    val meals: Resource<List<Meal>> = Resource.Loading,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
)
