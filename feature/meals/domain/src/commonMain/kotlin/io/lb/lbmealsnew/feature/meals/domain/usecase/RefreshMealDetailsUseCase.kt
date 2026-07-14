package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Syncs the details of a meal from the network into the local database (the
 * sync path).
 */
class RefreshMealDetailsUseCase(
    private val repository: MealsRepository,
) {
    /**
     * @param id The meal API ID.
     * @return A [Flow] of the sync attempt: [Resource.Loading] while it runs,
     * then [Resource.Success] with the details now stored, or [Resource.Error].
     * The content itself reaches screens through the observe path; this flow
     * reports how the attempt went.
     */
    operator fun invoke(id: String): Flow<Resource<MealDetails>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(repository.refreshMealDetails(id)))
    }.catch { emit(Resource.Error(it.message)) }
}
