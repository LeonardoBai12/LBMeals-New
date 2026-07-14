package io.lb.lbmealsnew.core.database.sqldelight.di

import io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase
import io.lb.lbmealsnew.core.database.sqldelight.factory.DatabaseDriverFactory
import io.lb.lbmealsnew.core.database.sqldelight.service.SQLDelightCategoriesDatabaseService
import io.lb.lbmealsnew.core.database.sqldelight.service.SQLDelightMealsDatabaseService
import io.lb.lbmealsnew.core.databaseapi.CategoriesDatabaseService
import io.lb.lbmealsnew.core.databaseapi.MealsDatabaseService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The only place in the app that knows SQLDelight is the engine behind the
 * database services.
 */
val databaseModule = module {
    single { get<DatabaseDriverFactory>().create() }
    single { LBMealsDatabase(get()) }
    single<CategoriesDatabaseService> { SQLDelightCategoriesDatabaseService(get()) }
    single<MealsDatabaseService> { SQLDelightMealsDatabaseService(get()) }
}

/**
 * Provides the platform-specific [DatabaseDriverFactory].
 */
expect val platformDatabaseModule: Module
