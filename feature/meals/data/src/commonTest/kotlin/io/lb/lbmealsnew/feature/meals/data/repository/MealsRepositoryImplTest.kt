package io.lb.lbmealsnew.feature.meals.data.repository

import io.lb.lbmealsnew.core.databaseapi.MealsDatabaseService
import io.lb.lbmealsnew.core.databaseapi.model.LocalIngredient
import io.lb.lbmealsnew.core.databaseapi.model.LocalMeal
import io.lb.lbmealsnew.core.databaseapi.model.LocalMealDetails
import io.lb.lbmealsnew.core.network.MealDbService
import io.lb.lbmealsnew.core.network.dto.MealDetailsDto
import io.lb.lbmealsnew.core.network.dto.MealDto
import io.lb.lbmealsnew.core.network.dto.MealLookupResponse
import io.lb.lbmealsnew.core.network.dto.MealsResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock

@UsesMocks(MealDbService::class, MealsDatabaseService::class)
class MealsRepositoryImplTest {
    private val mocker = Mocker()
    private val service: MealDbService = mocker.mock()
    private val databaseService: MealsDatabaseService = mocker.mock()
    private lateinit var repository: MealsRepositoryImpl

    @BeforeTest
    fun setUp() {
        mocker.reset()
        repository = MealsRepositoryImpl(service, databaseService)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When observing meals, expect the stored ones mapped to domain`() = runTest {
        val local = LocalMeal(id = "1", name = "Asado", thumbnailUrl = "https://thumb", category = "Beef")
        mocker.every { databaseService.observeMealsByCategory("Beef") } returns flowOf(listOf(local))

        val meals = repository.observeMealsByCategory("Beef").first()

        assertEquals(1, meals.size)
        assertEquals("1", meals.first().id)
        assertEquals("Asado", meals.first().name)
        assertEquals("https://thumb", meals.first().thumbnailUrl)
    }

    @Test
    fun `When refreshing meals, expect the fetched ones stored and returned`() = runTest {
        val dto = MealDto(id = "1", name = "Asado", thumbnailUrl = "https://thumb")
        mocker.everySuspending { service.getMealsByCategory("Beef") } returns MealsResponse(listOf(dto))
        mocker.everySuspending { databaseService.replaceMealsByCategory(isEqual("Beef"), isAny()) } returns Unit

        val meals = repository.refreshMealsByCategory("Beef")

        assertEquals(1, meals.size)
        assertEquals("Asado", meals.first().name)
        mocker.verifyWithSuspend(exhaustive = false, inOrder = false) {
            databaseService.replaceMealsByCategory(
                "Beef",
                listOf(LocalMeal(id = "1", name = "Asado", thumbnailUrl = "https://thumb", category = "Beef")),
            )
        }
    }

    @Test
    fun `When the API has no meals for the category, expect empty stored and returned`() = runTest {
        mocker.everySuspending { service.getMealsByCategory("Beef") } returns MealsResponse(null)
        mocker.everySuspending { databaseService.replaceMealsByCategory(isEqual("Beef"), isAny()) } returns Unit

        val meals = repository.refreshMealsByCategory("Beef")

        assertTrue(meals.isEmpty())
    }

    @Test
    fun `When observing details, expect the stored ones mapped to domain`() = runTest {
        val local = LocalMealDetails(
            id = "42",
            name = "Asado",
            category = "Beef",
            area = "Argentinian",
            instructions = "Grill it.",
            thumbnailUrl = "https://thumb",
            youtubeUrl = "https://youtube",
            ingredients = listOf(LocalIngredient(name = "Flank steak", measure = "500g")),
        )
        mocker.every { databaseService.observeMealDetails("42") } returns flowOf(local)

        val details = repository.observeMealDetails("42").first()

        assertEquals("Asado", details?.name)
        assertEquals("Grill it.", details?.instructions)
        assertEquals("Flank steak", details?.ingredients?.first()?.name)
        assertEquals("500g", details?.ingredients?.first()?.measure)
    }

    @Test
    fun `When nothing is stored for the id, expect null details`() = runTest {
        mocker.every { databaseService.observeMealDetails("42") } returns flowOf(null)

        assertNull(repository.observeMealDetails("42").first())
    }

    @Test
    fun `When refreshing details, expect them stored and returned with ingredients rebuilt`() = runTest {
        val dto = MealDetailsDto(
            id = "42",
            name = "Asado",
            category = "Beef",
            area = "Argentinian",
            instructions = "Grill it.",
            thumbnailUrl = "https://thumb",
            youtubeUrl = "https://youtube",
            ingredient1 = "Flank steak",
            measure1 = "500g",
        )
        mocker.everySuspending { service.getMealById("42") } returns MealLookupResponse(listOf(dto))
        mocker.everySuspending { databaseService.upsertMealDetails(isAny()) } returns Unit

        val details = repository.refreshMealDetails("42")

        assertEquals("Asado", details.name)
        assertEquals(1, details.ingredients.size)
        assertEquals("Flank steak", details.ingredients.first().name)
        assertEquals("500g", details.ingredients.first().measure)
    }

    @Test
    fun `When the API does not know the meal, expect refresh to fail`() = runTest {
        mocker.everySuspending { service.getMealById("42") } returns MealLookupResponse(null)

        assertFailsWith<IllegalStateException> {
            repository.refreshMealDetails("42")
        }
    }
}
