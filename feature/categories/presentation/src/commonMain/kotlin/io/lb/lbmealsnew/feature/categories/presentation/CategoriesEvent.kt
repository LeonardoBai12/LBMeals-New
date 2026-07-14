package io.lb.lbmealsnew.feature.categories.presentation

/**
 * Everything the user can do on the categories screen — the alphabet of the
 * screen's state machine, funnelled through a single onEvent function.
 */
sealed interface CategoriesEvent {
    /**
     * The search bar text changed.
     *
     * @property query The new query.
     */
    data class OnSearchQueryChange(val query: String) : CategoriesEvent

    /**
     * The user pulled to refresh.
     */
    data object OnRefresh : CategoriesEvent

    /**
     * A category card was clicked.
     *
     * @property name The clicked category name.
     */
    data class OnCategoryClick(val name: String) : CategoriesEvent
}
