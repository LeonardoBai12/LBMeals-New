package io.lb.lbmealsnew.app.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.lb.lbmealsnew.core.designsystem.transition.LocalSharedTransitionScope
import io.lb.lbmealsnew.feature.categories.presentation.navigation.CategoriesRoute
import io.lb.lbmealsnew.feature.categories.presentation.navigation.categoriesScreen
import io.lb.lbmealsnew.feature.meals.presentation.navigation.MealDetailsRoute
import io.lb.lbmealsnew.feature.meals.presentation.navigation.MealsRoute
import io.lb.lbmealsnew.feature.meals.presentation.navigation.mealDetailsScreen
import io.lb.lbmealsnew.feature.meals.presentation.navigation.mealsScreen

/**
 * App-level navigation graph. Features expose their screens and routes; only
 * this module knows how they connect — the single place where cross-feature
 * navigation is resolved.
 *
 * The whole graph lives inside a [SharedTransitionLayout], so screens can
 * morph shared elements (meal images, category cards) into each other.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = CategoriesRoute,
            ) {
                categoriesScreen(
                    onNavigateToMeals = { category ->
                        navController.navigate(MealsRoute(category))
                    },
                )

                mealsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { id, name, thumbnailUrl ->
                        navController.navigate(
                            MealDetailsRoute(
                                mealId = id,
                                mealName = name,
                                mealThumbnailUrl = thumbnailUrl,
                            ),
                        )
                    },
                )

                mealDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
