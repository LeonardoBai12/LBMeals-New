package io.lb.lbmealsnew.feature.categories.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.lb.lbmealsnew.feature.categories.presentation.CategoriesScreen
import io.lb.lbmealsnew.feature.categories.presentation.CategoriesViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe route of the categories screen.
 */
@Serializable
data object CategoriesRoute

/**
 * Wires the categories screen into a nav graph. Cross-feature navigation is
 * a callback resolved by the app module — features never depend on each other.
 *
 * @param onNavigateToMeals Called with the category name when a category is clicked.
 */
fun NavGraphBuilder.categoriesScreen(onNavigateToMeals: (String) -> Unit) {
    composable<CategoriesRoute> {
        val viewModel: CategoriesViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        CategoriesScreen(
            state = state,
            onEvent = viewModel::onEvent,
            effects = viewModel.effects,
            onNavigateToMeals = onNavigateToMeals,
        )
    }
}
