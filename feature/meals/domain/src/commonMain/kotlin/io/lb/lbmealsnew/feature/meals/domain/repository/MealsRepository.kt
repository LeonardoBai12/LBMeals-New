package io.lb.lbmealsnew.feature.meals.domain.repository

import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first meals repository.
 *
 * Reads always come from the local database (single source of truth) as a
 * never-completing [Flow]. [refreshMealsByCategory] is a one-shot sync that
 * fetches from the network and writes into the database — its result reaches
 * the UI through [observeMealsByCategory], not through a return value.
 */
interface MealsRepository {
    /**
     * Observes the locally stored meals of a given category.
     *
     * @param category The category name.
     */
    fun observeMealsByCategory(category: String): Flow<List<Meal>>

    /**
     * Fetches the meals of a given category from the network and stores them
     * locally, throwing on failure — error handling is the sync use case's job.
     *
     * @param category The category name.
     * @return The meals now stored for [category].
     */
    suspend fun refreshMealsByCategory(category: String): List<Meal>

    /**
     * Observes the locally stored details of a meal; emits null while nothing
     * is stored for that id yet.
     *
     * @param id The meal API ID.
     */
    fun observeMealDetails(id: String): Flow<MealDetails?>

    /**
     * Fetches the details of a meal from the network and stores them locally,
     * throwing on failure — error handling is the sync use case's job.
     *
     * @param id The meal API ID.
     * @return The details now stored.
     */
    suspend fun refreshMealDetails(id: String): MealDetails
}
