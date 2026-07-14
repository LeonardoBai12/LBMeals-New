package io.lb.lbmealsnew.feature.categories.data.mapper

import io.lb.lbmealsnew.core.databaseapi.model.LocalCategory
import io.lb.lbmealsnew.core.network.dto.CategoryDto
import io.lb.lbmealsnew.feature.categories.domain.model.Category

/**
 * Network DTO → local storage model (the sync path writes this).
 */
internal fun CategoryDto.toLocal() = LocalCategory(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl,
    description = description,
)

/**
 * Local storage model → domain model (the read path emits this).
 */
internal fun LocalCategory.toDomain() = Category(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl,
    description = description,
)
