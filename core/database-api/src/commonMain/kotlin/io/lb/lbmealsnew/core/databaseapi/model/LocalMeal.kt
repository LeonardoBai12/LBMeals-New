package io.lb.lbmealsnew.core.databaseapi.model

/**
 * A meal as persisted locally. Plain Kotlin — no persistence-library
 * annotations, so the storage engine behind [io.lb.lbmealsnew.core.databaseapi.MealsDatabaseService]
 * stays replaceable.
 *
 * @property id Meal API ID.
 * @property name Meal name.
 * @property thumbnailUrl Meal thumbnail URL.
 * @property category Name of the category the meal belongs to.
 */
data class LocalMeal(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val category: String,
)
