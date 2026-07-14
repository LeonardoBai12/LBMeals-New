package io.lb.lbmealsnew.feature.meals.presentation.details

/**
 * One-shot side effects. Kept out of [MealDetailsState] on purpose: state
 * replays on rotation, effects must happen exactly once.
 */
sealed interface MealDetailsEffect {
    /**
     * Shows a transient message without touching the content on screen.
     *
     * @property message The message.
     */
    data class ShowSnackbar(val message: String) : MealDetailsEffect

    /**
     * Navigates back to the meals screen.
     */
    data object NavigateBack : MealDetailsEffect

    /**
     * Opens an external URL (the recipe's YouTube video).
     *
     * @property url The URL to open.
     */
    data class OpenUrl(val url: String) : MealDetailsEffect
}
