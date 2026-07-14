package io.lb.lbmealsnew.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.lb.lbmealsnew.core.network.dto.CategoriesResponse
import io.lb.lbmealsnew.core.network.dto.MealLookupResponse
import io.lb.lbmealsnew.core.network.dto.MealsResponse

private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1"

/**
 * Ktor-backed implementation of [MealDbService]. The only place in the app
 * that knows which HTTP client library is used.
 */
internal class KtorMealDbService(
    private val httpClient: HttpClient,
) : MealDbService {

    override suspend fun getCategories(): CategoriesResponse =
        httpClient.get("$BASE_URL/categories.php").body()

    override suspend fun getMealsByCategory(category: String): MealsResponse =
        httpClient.get("$BASE_URL/filter.php") {
            parameter("c", category)
        }.body()

    override suspend fun getMealById(id: String): MealLookupResponse =
        httpClient.get("$BASE_URL/lookup.php") {
            parameter("i", id)
        }.body()
}
