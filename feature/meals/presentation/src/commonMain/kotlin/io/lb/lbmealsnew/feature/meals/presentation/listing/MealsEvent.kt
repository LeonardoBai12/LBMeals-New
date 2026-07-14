package io.lb.lbmealsnew.feature.meals.presentation.listing

/**
 * Everything the user can do on the meals screen — the alphabet of the
 * screen's state machine, funnelled through a single onEvent function.
 */
sealed interface MealsEvent {
    /**
     * The search bar text changed.
     *
     * @property query The new query.
     */
    data class OnSearchQueryChange(val query: String) : MealsEvent

    /**
     * The user pulled to refresh.
     */
    data object OnRefresh : MealsEvent

    /**
     * The back button was clicked.
     */
    data object OnBackClick : MealsEvent

    /**
     * A meal card was clicked.
     *
     * @property id The clicked meal API ID.
     * @property name The clicked meal name.
     */
    data class OnMealClick(val id: String, val name: String) : MealsEvent
}
