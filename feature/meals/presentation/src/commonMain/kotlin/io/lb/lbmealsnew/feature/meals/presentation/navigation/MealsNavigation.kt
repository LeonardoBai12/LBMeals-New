package io.lb.lbmealsnew.feature.meals.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
 */
@Serializable
data class MealDetailsRoute(val mealId: String, val mealName: String)

/**
 * Wires the meals screen into a nav graph.
 *
 * @param onNavigateBack Called when the user leaves the screen.
 * @param onNavigateToDetails Called with the meal id and name when a meal is clicked.
 */
fun NavGraphBuilder.mealsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (id: String, name: String) -> Unit,
) {
    composable<MealsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MealsRoute>()
        val viewModel: MealsViewModel = koinViewModel(
            parameters = { parametersOf(route.category) },
        )
        val state by viewModel.state.collectAsStateWithLifecycle()

        MealsScreen(
            state = state,
            onEvent = viewModel::onEvent,
            effects = viewModel.effects,
            onNavigateBack = onNavigateBack,
            onNavigateToDetails = onNavigateToDetails,
        )
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
            parameters = { parametersOf(route.mealId, route.mealName) },
        )
        val state by viewModel.state.collectAsStateWithLifecycle()

        MealDetailsScreen(
            state = state,
            onEvent = viewModel::onEvent,
            effects = viewModel.effects,
            onNavigateBack = onNavigateBack,
        )
    }
}
