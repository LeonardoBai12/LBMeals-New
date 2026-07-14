package io.lb.lbmealsnew.feature.categories.domain.model

/**
 * A meal category.
 *
 * @property id Category API ID.
 * @property name Category name.
 * @property thumbnailUrl Category thumbnail URL.
 * @property description Category description.
 */
data class Category(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val description: String,
)
