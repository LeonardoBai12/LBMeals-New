package io.lb.lbmealsnew.feature.meals.presentation.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.lb.lbmealsnew.core.designsystem.transition.LocalNavAnimatedVisibilityScope
import io.lb.lbmealsnew.feature.meals.presentation.details.MealDetailsScreen
import io.lb.lbmealsnew.feature.meals.presentation.details.MealDetailsViewModel
import io.lb.lbmealsnew.feature.meals.presentation.listing.MealsScreen
import io.lb.lbmealsnew.feature.meals.presentation.listing.MealsViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Type-safe route of the meals screen. The navigation library restores the
 * argument on process death and hands it back to the ViewModel through its
 * Koin parameters.
 *
 * @property category The category whose meals are shown.
 */
@Serializable
data class MealsRoute(val category: String)

/**
 * Type-safe route of the meal details screen.
 *
 * @property mealId The meal API ID.
 * @property mealName The meal name, shown as title while details load.
 * @property mealThumbnailUrl The meal thumbnail URL, shown as header while
 * details load — the same image the list already rendered, so the shared
 * element transition has its target from the first frame.
 */
@Serializable
data class MealDetailsRoute(
    val mealId: String,
    val mealName: String,
    val mealThumbnailUrl: String,
)

/**
 * Wires the meals screen into a nav graph.
 *
 * @param onNavigateBack Called when the user leaves the screen.
 * @param onNavigateToDetails Called with the meal id and name when a meal is clicked.
 */
fun NavGraphBuilder.mealsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (id: String, name: String, thumbnailUrl: String) -> Unit,
) {
    composable<MealsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MealsRoute>()
        val viewModel: MealsViewModel = koinViewModel(
            parameters = { parametersOf(route.category) },
        )
        val state by viewModel.state.collectAsStateWithLifecycle()

        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            MealsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                effects = viewModel.effects,
                onNavigateBack = onNavigateBack,
                onNavigateToDetails = onNavigateToDetails,
            )
        }
    }
}

/**
 * Wires the meal details screen into a nav graph.
 *
 * @param onNavigateBack Called when the user leaves the screen.
 */
fun NavGraphBuilder.mealDetailsScreen(onNavigateBack: () -> Unit) {
    composable<MealDetailsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MealDetailsRoute>()
        val viewModel: MealDetailsViewModel = koinViewModel(
            parameters = { parametersOf(route.mealId, route.mealName, route.mealThumbnailUrl) },
        )
        val state by viewModel.state.collectAsStateWithLifecycle()

        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            MealDetailsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                effects = viewModel.effects,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}
