package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@UsesMocks(MealsRepository::class)
class RefreshMealDetailsUseCaseTest {
    private val mocker = Mocker()
    private val repository: MealsRepository = mocker.mock()
    private lateinit var useCase: RefreshMealDetailsUseCase

    @BeforeTest
    fun setUp() {
        mocker.reset()
        useCase = RefreshMealDetailsUseCase(repository)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When refresh succeeds, expect loading then success with the synced details`() = runTest {
        val details = MealDetails(
            id = "42",
            name = "Asado",
            category = "Beef",
            area = "Argentinian",
            instructions = "Grill it.",
            thumbnailUrl = "",
            youtubeUrl = null,
            ingredients = emptyList(),
        )
        mocker.everySuspending { repository.refreshMealDetails("42") } returns details

        val states = useCase("42").toList()

        assertTrue(states.first() is Resource.Loading)
        val last = states.last()
        assertTrue(last is Resource.Success)
        assertEquals(details, last.data)
    }

    @Test
    fun `When refresh fails, expect loading then error`() = runTest {
        mocker.everySuspending {
            repository.refreshMealDetails("42")
        } runs { throw Exception("Meal 42 not found") }

        val states = useCase("42").toList()

        assertTrue(states.first() is Resource.Loading)
        val last = states.last()
        assertTrue(last is Resource.Error)
        assertEquals("Meal 42 not found", last.message)
    }
}
