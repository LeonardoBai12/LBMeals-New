package io.lb.lbmealsnew.feature.categories.domain.usecase

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Syncs categories from the network into the local database (the sync path).
 */
class RefreshCategoriesUseCase(
    private val repository: CategoriesRepository,
) {
    /**
     * @return A [Flow] of the sync attempt: [Resource.Loading] while it runs,
     * then [Resource.Success] with the categories now stored, or
     * [Resource.Error]. The content itself reaches screens through the
     * observe path; this flow reports how the attempt went.
     */
    operator fun invoke(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(repository.refreshCategories()))
    }.catch { emit(Resource.Error(it.message)) }
}
