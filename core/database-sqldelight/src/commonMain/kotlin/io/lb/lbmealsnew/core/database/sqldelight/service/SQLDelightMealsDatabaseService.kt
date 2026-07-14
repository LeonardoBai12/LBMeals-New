package io.lb.lbmealsnew.core.database.sqldelight.service

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase
import io.lb.lbmealsnew.core.database.sqldelight.MealDetailsEntity
import io.lb.lbmealsnew.core.database.sqldelight.MealEntity
import io.lb.lbmealsnew.core.databaseapi.MealsDatabaseService
import io.lb.lbmealsnew.core.databaseapi.model.LocalIngredient
import io.lb.lbmealsnew.core.databaseapi.model.LocalMeal
import io.lb.lbmealsnew.core.databaseapi.model.LocalMealDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * SQLDelight-backed implementation of the meals database service. The
 * observe flows come from the driver's query listeners: every write into a
 * table re-runs the query and re-emits to all active collectors.
 *
 * Ingredients are stored as a JSON column: they are always read together
 * with the meal and never queried relationally, so a normalized ingredients
 * table would only add joins.
 */
internal class SQLDelightMealsDatabaseService(
    database: LBMealsDatabase,
) : MealsDatabaseService {

    private val queries = database.mealsQueries
    private val json = Json

    override fun observeMealsByCategory(category: String): Flow<List<LocalMeal>> =
        queries.observeMealsByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toLocal() } }

    override suspend fun replaceMealsByCategory(category: String, meals: List<LocalMeal>) =
        withContext(Dispatchers.IO) {
            // Delete + insert inside a single transaction, so observers see
            // exactly one re-emission with the final rows.
            queries.transaction {
                queries.clearMealsByCategory(category)
                meals.forEach { meal ->
                    queries.insertMeal(
                        id = meal.id,
                        name = meal.name,
                        thumbnailUrl = meal.thumbnailUrl,
                        category = meal.category,
                    )
                }
            }
        }

    override fun observeMealDetails(id: String): Flow<LocalMealDetails?> =
        queries.observeMealDetails(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity -> entity?.toLocal() }

    override suspend fun upsertMealDetails(details: LocalMealDetails) {
        withContext(Dispatchers.IO) {
            queries.upsertMealDetails(
                id = details.id,
                name = details.name,
                category = details.category,
                area = details.area,
                instructions = details.instructions,
                thumbnailUrl = details.thumbnailUrl,
                youtubeUrl = details.youtubeUrl,
                ingredientsJson = json.encodeToString(
                    details.ingredients.map { StoredIngredient(name = it.name, measure = it.measure) },
                ),
            )
        }
    }

    private fun MealEntity.toLocal() = LocalMeal(
        id = id,
        name = name,
        thumbnailUrl = thumbnailUrl,
        category = category,
    )

    private fun MealDetailsEntity.toLocal() = LocalMealDetails(
        id = id,
        name = name,
        category = category,
        area = area,
        instructions = instructions,
        thumbnailUrl = thumbnailUrl,
        youtubeUrl = youtubeUrl,
        ingredients = json.decodeFromString<List<StoredIngredient>>(ingredientsJson)
            .map { LocalIngredient(name = it.name, measure = it.measure) },
    )
}

@Serializable
private data class StoredIngredient(val name: String, val measure: String)
