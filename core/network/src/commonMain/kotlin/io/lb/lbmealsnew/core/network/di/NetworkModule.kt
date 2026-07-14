package io.lb.lbmealsnew.core.network.di

import io.lb.lbmealsnew.core.network.KtorMealDbService
import io.lb.lbmealsnew.core.network.MealDbService
import io.lb.lbmealsnew.core.network.factory.HttpClientFactory
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory().create() }
    single<MealDbService> { KtorMealDbService(get()) }
}
