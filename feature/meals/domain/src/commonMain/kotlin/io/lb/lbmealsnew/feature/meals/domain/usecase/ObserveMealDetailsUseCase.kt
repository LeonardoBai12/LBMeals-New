package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the locally stored details of a meal (the read path).
 */
class ObserveMealDetailsUseCase(
    private val repository: MealsRepository,
) {
    operator fun invoke(id: String): Flow<MealDetails?> =
        repository.observeMealDetails(id)
}
