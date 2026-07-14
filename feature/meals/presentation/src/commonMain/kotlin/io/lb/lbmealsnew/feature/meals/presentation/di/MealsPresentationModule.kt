package io.lb.lbmealsnew.feature.meals.presentation.di

import io.lb.lbmealsnew.feature.meals.presentation.details.MealDetailsViewModel
import io.lb.lbmealsnew.feature.meals.presentation.listing.MealsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mealsPresentationModule = module {
    viewModel { (category: String) ->
        MealsViewModel(
            category = category,
            observeMealsByCategory = get(),
            refreshMealsByCategory = get(),
        )
    }

    viewModel { (mealId: String, mealName: String, mealThumbnailUrl: String) ->
        MealDetailsViewModel(
            mealId = mealId,
            mealName = mealName,
            mealThumbnailUrl = mealThumbnailUrl,
            observeMealDetails = get(),
            refreshMealDetails = get(),
        )
    }
}
