package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.feature.meals.domain.model.Ingredient
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock

@UsesMocks(MealsRepository::class)
class ObserveMealDetailsUseCaseTest {
    private val mocker = Mocker()
    private val repository: MealsRepository = mocker.mock()
    private lateinit var useCase: ObserveMealDetailsUseCase

    @BeforeTest
    fun setUp() {
        mocker.reset()
        useCase = ObserveMealDetailsUseCase(repository)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When details are stored, expect them`() = runTest {
        val details = MealDetails(
            id = "42",
            name = "Asado",
            category = "Beef",
            area = "Argentinian",
            instructions = "Grill it.",
            thumbnailUrl = "",
            youtubeUrl = null,
            ingredients = listOf(Ingredient(name = "Flank steak", measure = "500g")),
        )
        mocker.every { repository.observeMealDetails("42") } returns flowOf(details)

        assertEquals(details, useCase("42").first())
    }

    @Test
    fun `When nothing is stored yet, expect null`() = runTest {
        mocker.every { repository.observeMealDetails("42") } returns flowOf(null)

        assertNull(useCase("42").first())
    }
}
