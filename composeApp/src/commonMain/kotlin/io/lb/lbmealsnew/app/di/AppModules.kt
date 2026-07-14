package io.lb.lbmealsnew.app.di

import io.lb.lbmealsnew.core.database.sqldelight.di.databaseModule
import io.lb.lbmealsnew.core.network.di.networkModule
import io.lb.lbmealsnew.feature.categories.data.di.categoriesDataModule
import io.lb.lbmealsnew.feature.categories.domain.di.categoriesDomainModule
import io.lb.lbmealsnew.feature.categories.presentation.di.categoriesPresentationModule
import io.lb.lbmealsnew.feature.meals.data.di.mealsDataModule
import io.lb.lbmealsnew.feature.meals.domain.di.mealsDomainModule
import io.lb.lbmealsnew.feature.meals.presentation.di.mealsPresentationModule

/**
 * Every Koin module of the app. The composition root is the only place where
 * every implementation meets its abstraction — the platform entry points add
 * their platform-specific modules on top.
 */
val appModules = listOf(
    networkModule,
    databaseModule,
    categoriesDataModule,
    categoriesDomainModule,
    categoriesPresentationModule,
    mealsDataModule,
    mealsDomainModule,
    mealsPresentationModule,
)
