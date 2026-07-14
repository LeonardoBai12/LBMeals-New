package io.lb.lbmealsnew.core.databaseapi.model

/**
 * A meal's full details as persisted locally. Plain Kotlin — no
 * persistence-library annotations, so the storage engine stays replaceable.
 *
 * @property id Meal API ID.
 * @property name Meal name.
 * @property category Category name, when known.
 * @property area Original country of the meal, when known.
 * @property instructions Cooking instructions.
 * @property thumbnailUrl Meal thumbnail URL.
 * @property youtubeUrl YouTube video URL, when available.
 * @property ingredients Ingredients with their measures, in API order.
 */
data class LocalMealDetails(
    val id: String,
    val name: String,
    val category: String?,
    val area: String?,
    val instructions: String,
    val thumbnailUrl: String,
    val youtubeUrl: String?,
    val ingredients: List<LocalIngredient>,
)

/**
 * An ingredient of a meal.
 *
 * @property name Ingredient name.
 * @property measure Measure of the ingredient, possibly empty.
 */
data class LocalIngredient(
    val name: String,
    val measure: String,
)
