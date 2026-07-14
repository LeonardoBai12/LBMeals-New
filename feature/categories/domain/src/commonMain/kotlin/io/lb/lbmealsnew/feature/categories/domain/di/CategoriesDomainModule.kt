package io.lb.lbmealsnew.feature.categories.domain.di

import io.lb.lbmealsnew.feature.categories.domain.usecase.ObserveCategoriesUseCase
import io.lb.lbmealsnew.feature.categories.domain.usecase.RefreshCategoriesUseCase
import org.koin.dsl.module

val categoriesDomainModule = module {
    factory { ObserveCategoriesUseCase(get()) }
    factory { RefreshCategoriesUseCase(get()) }
}
