package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Syncs the meals of a given category from the network into the local
 * database (the sync path).
 */
class RefreshMealsByCategoryUseCase(
    private val repository: MealsRepository,
) {
    /**
     * @param category The category name.
     * @return A [Flow] of the sync attempt: [Resource.Loading] while it runs,
     * then [Resource.Success] with the meals now stored, or [Resource.Error].
     * The content itself reaches screens through the observe path; this flow
     * reports how the attempt went.
     */
    operator fun invoke(category: String): Flow<Resource<List<Meal>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(repository.refreshMealsByCategory(category)))
    }.catch { emit(Resource.Error(it.message)) }
}
