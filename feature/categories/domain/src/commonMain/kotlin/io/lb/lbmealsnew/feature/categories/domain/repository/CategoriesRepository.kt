package io.lb.lbmealsnew.feature.categories.domain.repository

import io.lb.lbmealsnew.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first categories repository.
 *
 * Reads always come from the local database (single source of truth) as a
 * never-completing [Flow]. [refreshCategories] is a one-shot sync that
 * fetches from the network and writes into the database — its result reaches
 * the UI through [observeCategories], not through a return value.
 */
interface CategoriesRepository {
    /**
     * Observes every meal category stored locally.
     */
    fun observeCategories(): Flow<List<Category>>

    /**
     * Fetches categories from the network and stores them locally, throwing
     * on failure — error handling is the sync use case's job.
     *
     * @return The categories now stored.
     */
    suspend fun refreshCategories(): List<Category>
}
