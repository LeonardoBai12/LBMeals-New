package io.lb.lbmealsnew.feature.meals.domain.model

/**
 * Full details of a meal.
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
data class MealDetails(
    val id: String,
    val name: String,
    val category: String?,
    val area: String?,
    val instructions: String,
    val thumbnailUrl: String,
    val youtubeUrl: String?,
    val ingredients: List<Ingredient>,
)

/**
 * An ingredient of a meal.
 *
 * @property name Ingredient name.
 * @property measure Measure of the ingredient, possibly empty.
 */
data class Ingredient(
    val name: String,
    val measure: String,
)
