package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock

@UsesMocks(MealsRepository::class)
class ObserveMealsByCategoryUseCaseTest {
    private val mocker = Mocker()
    private val repository: MealsRepository = mocker.mock()
    private lateinit var useCase: ObserveMealsByCategoryUseCase

    @BeforeTest
    fun setUp() {
        mocker.reset()
        useCase = ObserveMealsByCategoryUseCase(repository)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When observing meals, expect the stored meals of the category`() = runTest {
        val meals = listOf(
            Meal(id = "1", name = "Asado", thumbnailUrl = ""),
            Meal(id = "2", name = "Arepa pelua", thumbnailUrl = ""),
        )
        mocker.every { repository.observeMealsByCategory("Beef") } returns flowOf(meals)

        assertEquals(meals, useCase("Beef").first())
    }
}
