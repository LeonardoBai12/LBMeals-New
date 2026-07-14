package io.lb.lbmealsnew.feature.meals.presentation.details

/**
 * Everything the user can do on the meal details screen.
 */
sealed interface MealDetailsEvent {
    /**
     * The user pulled to refresh.
     */
    data object OnRefresh : MealDetailsEvent

    /**
     * The back button was clicked.
     */
    data object OnBackClick : MealDetailsEvent

    /**
     * The YouTube button was clicked.
     */
    data object OnYoutubeClick : MealDetailsEvent
}
