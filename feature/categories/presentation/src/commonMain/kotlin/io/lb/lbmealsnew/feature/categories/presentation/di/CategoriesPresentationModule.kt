package io.lb.lbmealsnew.feature.categories.presentation.di

import io.lb.lbmealsnew.feature.categories.presentation.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesPresentationModule = module {
    viewModel {
        CategoriesViewModel(
            observeCategories = get(),
            refreshCategories = get(),
        )
    }
}
