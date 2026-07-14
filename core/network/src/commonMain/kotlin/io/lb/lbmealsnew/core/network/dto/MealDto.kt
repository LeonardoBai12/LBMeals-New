package io.lb.lbmealsnew.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A meal as returned by the `filter.php` endpoint of TheMealDB API, which
 * only carries id, name and thumbnail.
 */
@Serializable
data class MealDto(
    @SerialName("idMeal")
    val id: String,
    @SerialName("strMeal")
    val name: String,
    @SerialName("strMealThumb")
    val thumbnailUrl: String,
)

/**
 * Response wrapper of the `filter.php` endpoint. The API returns a JSON
 * `null` for unknown categories, hence the nullable list.
 */
@Serializable
data class MealsResponse(
    val meals: List<MealDto>? = null,
)
