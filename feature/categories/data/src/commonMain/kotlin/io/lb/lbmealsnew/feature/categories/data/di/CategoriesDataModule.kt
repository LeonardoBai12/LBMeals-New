package io.lb.lbmealsnew.feature.categories.data.di

import io.lb.lbmealsnew.feature.categories.data.repository.CategoriesRepositoryImpl
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
import org.koin.dsl.module

val categoriesDataModule = module {
    single<CategoriesRepository> { CategoriesRepositoryImpl(get(), get()) }
}
