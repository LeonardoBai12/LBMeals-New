package io.lb.lbmealsnew.feature.meals.presentation.details

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails

/**
 * Everything the meal details screen IS right now, as a single immutable
 * object. Survives rotation through the ViewModel's StateFlow.
 *
 * @property mealName Name known from the list screen — lets the title render
 * before the details arrive.
 * @property details The details on their way to the screen: [Resource.Loading]
 * while data is genuinely expected to arrive, [Resource.Error] when the sync
 * failed with nothing cached, and [Resource.Success] once stored details exist.
 * @property isRefreshing Whether a network sync is in flight over content
 * already on screen — drives the pull-to-refresh indicator only; the first
 * load shows the full-screen loading instead.
 */
data class MealDetailsState(
    val mealName: String = "",
    val details: Resource<MealDetails> = Resource.Loading,
    val isRefreshing: Boolean = false,
)
