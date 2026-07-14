package io.lb.lbmealsnew.feature.meals.domain.model

/**
 * A meal inside a category.
 *
 * @property id Meal API ID.
 * @property name Meal name.
 * @property thumbnailUrl Meal thumbnail URL.
 */
data class Meal(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
)
