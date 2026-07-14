package io.lb.lbmealsnew.feature.meals.data.mapper

import io.lb.lbmealsnew.core.databaseapi.model.LocalIngredient
import io.lb.lbmealsnew.core.databaseapi.model.LocalMealDetails
import io.lb.lbmealsnew.core.network.dto.MealDetailsDto
import io.lb.lbmealsnew.feature.meals.domain.model.Ingredient
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails

/**
 * Network DTO → local storage model (the sync path writes this). The API's
 * 20 flattened ingredient fields become a proper list here, at the edge.
 */
internal fun MealDetailsDto.toLocal() = LocalMealDetails(
    id = id,
    name = name,
    category = category,
    area = area,
    instructions = instructions.orEmpty(),
    thumbnailUrl = thumbnailUrl,
    youtubeUrl = youtubeUrl?.takeIf { it.isNotBlank() },
    ingredients = ingredientPairs().map { (name, measure) ->
        LocalIngredient(name = name, measure = measure)
    },
)

/**
 * Local storage model → domain model (the read path emits this).
 */
internal fun LocalMealDetails.toDomain() = MealDetails(
    id = id,
    name = name,
    category = category,
    area = area,
    instructions = instructions,
    thumbnailUrl = thumbnailUrl,
    youtubeUrl = youtubeUrl,
    ingredients = ingredients.map { Ingredient(name = it.name, measure = it.measure) },
)
