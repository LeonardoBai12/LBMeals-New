package io.lb.lbmealsnew.feature.meals.data.repository

import io.lb.lbmealsnew.core.databaseapi.MealsDatabaseService
import io.lb.lbmealsnew.core.network.MealDbService
import io.lb.lbmealsnew.feature.meals.data.mapper.toDomain
import io.lb.lbmealsnew.feature.meals.data.mapper.toLocal
import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Offline-first implementation: reads only from the local database, and the
 * network only writes into it. There is no `if (online)` branch anywhere —
 * a network failure only affects the sync path, never the read path.
 */
internal class MealsRepositoryImpl(
    private val service: MealDbService,
    private val databaseService: MealsDatabaseService,
) : MealsRepository {

    override fun observeMealsByCategory(category: String): Flow<List<Meal>> =
        databaseService.observeMealsByCategory(category)
            .map { meals -> meals.map { it.toDomain() } }

    override suspend fun refreshMealsByCategory(category: String): List<Meal> {
        val meals = service.getMealsByCategory(category).meals.orEmpty().map { it.toLocal(category) }
        databaseService.replaceMealsByCategory(category = category, meals = meals)
        return meals.map { it.toDomain() }
    }

    override fun observeMealDetails(id: String): Flow<MealDetails?> =
        databaseService.observeMealDetails(id)
            .map { details -> details?.toDomain() }

    override suspend fun refreshMealDetails(id: String): MealDetails {
        val details = service.getMealById(id).meals?.firstOrNull()
            ?: error("Meal $id not found")
        val local = details.toLocal()
        databaseService.upsertMealDetails(local)
        return local.toDomain()
    }
}
