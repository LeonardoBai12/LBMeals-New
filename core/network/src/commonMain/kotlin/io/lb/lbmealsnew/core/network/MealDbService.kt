package io.lb.lbmealsnew.core.network

import io.lb.lbmealsnew.core.network.dto.CategoriesResponse
import io.lb.lbmealsnew.core.network.dto.MealLookupResponse
import io.lb.lbmealsnew.core.network.dto.MealsResponse

/**
 * TheMealDB REST API.
 */
interface MealDbService {
    /**
     * Fetches every meal category.
     */
    suspend fun getCategories(): CategoriesResponse

    /**
     * Fetches the meals of a given category.
     *
     * @param category The category name, e.g. "Seafood".
     */
    suspend fun getMealsByCategory(category: String): MealsResponse

    /**
     * Fetches the full details of a meal.
     *
     * @param id The meal API ID, e.g. "52772".
     */
    suspend fun getMealById(id: String): MealLookupResponse
}
