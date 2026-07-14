package io.lb.lbmealsnew.feature.categories.data.repository

import io.lb.lbmealsnew.core.databaseapi.CategoriesDatabaseService
import io.lb.lbmealsnew.core.network.MealDbService
import io.lb.lbmealsnew.feature.categories.data.mapper.toDomain
import io.lb.lbmealsnew.feature.categories.data.mapper.toLocal
import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Offline-first implementation: reads only from the local database, and the
 * network only writes into it. There is no `if (online)` branch anywhere —
 * a network failure only affects the sync path, never the read path.
 */
internal class CategoriesRepositoryImpl(
    private val service: MealDbService,
    private val databaseService: CategoriesDatabaseService,
) : CategoriesRepository {

    override fun observeCategories(): Flow<List<Category>> =
        databaseService.observeCategories()
            .map { categories -> categories.map { it.toDomain() } }

    override suspend fun refreshCategories(): List<Category> {
        val categories = service.getCategories().categories.map { it.toLocal() }
        databaseService.replaceCategories(categories)
        return categories.map { it.toDomain() }
    }
}
