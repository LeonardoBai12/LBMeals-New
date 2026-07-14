package io.lb.lbmealsnew.feature.meals.domain.di

import io.lb.lbmealsnew.feature.meals.domain.usecase.ObserveMealDetailsUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.ObserveMealsByCategoryUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.RefreshMealDetailsUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.RefreshMealsByCategoryUseCase
import org.koin.dsl.module

val mealsDomainModule = module {
    factory { ObserveMealsByCategoryUseCase(get()) }
    factory { RefreshMealsByCategoryUseCase(get()) }
    factory { ObserveMealDetailsUseCase(get()) }
    factory { RefreshMealDetailsUseCase(get()) }
}
