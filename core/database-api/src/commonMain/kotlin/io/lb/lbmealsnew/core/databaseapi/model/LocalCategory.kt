package io.lb.lbmealsnew.core.databaseapi.model

/**
 * A meal category as persisted locally. Plain Kotlin — no persistence-library
 * annotations, so the storage engine behind [io.lb.lbmealsnew.core.databaseapi.MealsDatabaseService]
 * stays replaceable.
 *
 * @property id Category API ID.
 * @property name Category name.
 * @property thumbnailUrl Category thumbnail URL.
 * @property description Category description.
 */
data class LocalCategory(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val description: String,
)
