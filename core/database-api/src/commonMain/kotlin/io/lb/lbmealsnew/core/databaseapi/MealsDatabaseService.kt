package io.lb.lbmealsnew.core.databaseapi

import io.lb.lbmealsnew.core.databaseapi.model.LocalCategory
import io.lb.lbmealsnew.core.databaseapi.model.LocalMeal
import io.lb.lbmealsnew.core.databaseapi.model.LocalMealDetails
import kotlinx.coroutines.flow.Flow

/**
 * Abstractions over the local persistence engine.
 *
 * Each feature's data layer depends on the interface it needs, never on a
 * concrete database library. Any implementation that honours these contracts
 * can be swapped in (Liskov substitution) — Room today, SQLDelight or an
 * in-memory fake tomorrow — without touching the data layers. Splitting per
 * feature keeps the Interface Segregation Principle: the categories feature
 * cannot even see meal storage.
 *
 * Contract: observe functions are never-completing [Flow]s that re-emit
 * whenever the underlying data changes; replace functions atomically swap
 * the stored rows and trigger exactly one re-emission.
 */
interface CategoriesDatabaseService {
    /**
     * Observes every stored category.
     */
    fun observeCategories(): Flow<List<LocalCategory>>

    /**
     * Atomically replaces all stored categories.
     *
     * @param categories The fresh categories.
     */
    suspend fun replaceCategories(categories: List<LocalCategory>)
}

/**
 * Local persistence contract of the meals feature. See [CategoriesDatabaseService]
 * for the general contract of database services.
 */
interface MealsDatabaseService {
    /**
     * Observes the stored meals of a given category.
     *
     * @param category The category name.
     */
    fun observeMealsByCategory(category: String): Flow<List<LocalMeal>>

    /**
     * Atomically replaces the stored meals of a given category.
     *
     * @param category The category name.
     * @param meals The fresh meals of that category.
     */
    suspend fun replaceMealsByCategory(category: String, meals: List<LocalMeal>)

    /**
     * Observes the stored details of a meal; emits null while nothing is
     * stored for that id yet.
     *
     * @param id The meal API ID.
     */
    fun observeMealDetails(id: String): Flow<LocalMealDetails?>

    /**
     * Inserts or updates the stored details of a meal.
     *
     * @param details The fresh details.
     */
    suspend fun upsertMealDetails(details: LocalMealDetails)
}
