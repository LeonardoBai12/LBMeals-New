package io.lb.lbmealsnew.feature.categories.presentation

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.categories.domain.model.Category

/**
 * Everything the categories screen IS right now, as a single immutable
 * object. Survives rotation through the ViewModel's StateFlow.
 *
 * @property categories The categories on their way to the screen: [Resource.Loading]
 * while data is genuinely expected to arrive, [Resource.Error] when the sync
 * failed with nothing cached, and [Resource.Success] once there is content to
 * render (already filtered by [searchQuery] — possibly empty).
 * @property searchQuery Current search bar text.
 * @property isRefreshing Whether a network sync is in flight over content
 * already on screen — drives the pull-to-refresh indicator only; the first
 * load shows the full-screen loading instead.
 */
data class CategoriesState(
    val categories: Resource<List<Category>> = Resource.Loading,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
)
