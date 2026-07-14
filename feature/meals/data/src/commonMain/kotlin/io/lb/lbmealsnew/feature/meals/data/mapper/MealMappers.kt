package io.lb.lbmealsnew.feature.meals.data.mapper

import io.lb.lbmealsnew.core.databaseapi.model.LocalMeal
import io.lb.lbmealsnew.core.network.dto.MealDto
import io.lb.lbmealsnew.feature.meals.domain.model.Meal

/**
 * Network DTO → local storage model (the sync path writes this). The
 * category is not part of the `filter.php` payload, so it is carried in from
 * the request.
 */
internal fun MealDto.toLocal(category: String) = LocalMeal(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl,
    category = category,
)

/**
 * Local storage model → domain model (the read path emits this).
 */
internal fun LocalMeal.toDomain() = Meal(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl,
)
