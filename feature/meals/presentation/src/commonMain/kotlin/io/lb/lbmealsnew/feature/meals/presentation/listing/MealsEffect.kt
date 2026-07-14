package io.lb.lbmealsnew.feature.meals.presentation.listing

/**
 * One-shot side effects. Kept out of [MealsState] on purpose: state replays
 * on rotation, effects must happen exactly once.
 */
sealed interface MealsEffect {
    /**
     * Shows a transient message without touching the content on screen.
     *
     * @property message The message.
     */
    data class ShowSnackBar(val message: String) : MealsEffect

    /**
     * Navigates back to the categories screen.
     */
    data object NavigateBack : MealsEffect

    /**
     * Navigates to the details screen of a meal.
     *
     * @property id The meal API ID.
     * @property name The meal name, shown as title while details load.
     * @property thumbnailUrl The meal thumbnail URL, shown as header while
     * details load.
     */
    data class NavigateToDetails(
        val id: String,
        val name: String,
        val thumbnailUrl: String,
    ) : MealsEffect
}
