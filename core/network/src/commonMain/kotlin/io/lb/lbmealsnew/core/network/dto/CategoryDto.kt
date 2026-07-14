package io.lb.lbmealsnew.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A meal category as returned by TheMealDB API. Mirrors the JSON shape —
 * lives in the network module only and never leaks past the data layer.
 */
@Serializable
data class CategoryDto(
    @SerialName("idCategory")
    val id: String,
    @SerialName("strCategory")
    val name: String,
    @SerialName("strCategoryThumb")
    val thumbnailUrl: String,
    @SerialName("strCategoryDescription")
    val description: String,
)

/**
 * Response wrapper of the `categories.php` endpoint.
 */
@Serializable
data class CategoriesResponse(
    val categories: List<CategoryDto> = emptyList(),
)
