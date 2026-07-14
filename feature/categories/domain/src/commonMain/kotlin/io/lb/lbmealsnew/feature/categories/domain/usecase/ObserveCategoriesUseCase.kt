package io.lb.lbmealsnew.feature.categories.domain.usecase

import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes every meal category stored locally (the read path).
 */
class ObserveCategoriesUseCase(
    private val repository: CategoriesRepository,
) {
    operator fun invoke(): Flow<List<Category>> = repository.observeCategories()
}
