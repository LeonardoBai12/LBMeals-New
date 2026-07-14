package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the locally stored meals of a given category (the read path).
 */
class ObserveMealsByCategoryUseCase(
    private val repository: MealsRepository,
) {
    operator fun invoke(category: String): Flow<List<Meal>> =
        repository.observeMealsByCategory(category)
}
