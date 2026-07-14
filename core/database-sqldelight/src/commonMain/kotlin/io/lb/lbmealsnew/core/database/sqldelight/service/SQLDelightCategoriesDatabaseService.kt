package io.lb.lbmealsnew.core.database.sqldelight.service

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.lb.lbmealsnew.core.database.sqldelight.CategoryEntity
import io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase
import io.lb.lbmealsnew.core.databaseapi.CategoriesDatabaseService
import io.lb.lbmealsnew.core.databaseapi.model.LocalCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * SQLDelight-backed implementation of the categories database service. The
 * observe flow comes from the driver's query listeners: every write into the
 * table re-runs the query and re-emits to all active collectors.
 */
internal class SQLDelightCategoriesDatabaseService(
    database: LBMealsDatabase,
) : CategoriesDatabaseService {

    private val queries = database.categoriesQueries

    override fun observeCategories(): Flow<List<LocalCategory>> =
        queries.observeCategories()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toLocal() } }

    override suspend fun replaceCategories(categories: List<LocalCategory>) =
        withContext(Dispatchers.IO) {
            // Delete + insert inside a single transaction, so observers see
            // exactly one re-emission with the final rows.
            queries.transaction {
                queries.clearCategories()
                categories.forEach { category ->
                    queries.insertCategory(
                        id = category.id,
                        name = category.name,
                        thumbnailUrl = category.thumbnailUrl,
                        description = category.description,
                    )
                }
            }
        }

    private fun CategoryEntity.toLocal() = LocalCategory(
        id = id,
        name = name,
        thumbnailUrl = thumbnailUrl,
        description = description,
    )
}
