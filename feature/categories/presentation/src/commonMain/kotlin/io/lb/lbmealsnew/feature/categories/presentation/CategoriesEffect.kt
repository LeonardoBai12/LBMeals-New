package io.lb.lbmealsnew.feature.categories.presentation

/**
 * One-shot side effects. Kept out of [CategoriesState] on purpose: state
 * replays on rotation, effects must happen exactly once.
 */
sealed interface CategoriesEffect {
    /**
     * Shows a transient message without touching the content on screen.
     *
     * @property message The message.
     */
    data class ShowSnackbar(val message: String) : CategoriesEffect

    /**
     * Navigates to the meals screen of a category.
     *
     * @property category The category name.
     */
    data class NavigateToMeals(val category: String) : CategoriesEffect
}
