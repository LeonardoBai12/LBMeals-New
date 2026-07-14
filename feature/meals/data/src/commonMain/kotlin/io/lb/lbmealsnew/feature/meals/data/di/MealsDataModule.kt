package io.lb.lbmealsnew.feature.meals.data.di

import io.lb.lbmealsnew.feature.meals.data.repository.MealsRepositoryImpl
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import org.koin.dsl.module

val mealsDataModule = module {
    single<MealsRepository> { MealsRepositoryImpl(get(), get()) }
}
