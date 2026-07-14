package io.lb.lbmealsnew.feature.meals.domain.usecase

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.meals.domain.model.Meal
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
class RefreshMealsByCategoryUseCaseTest {
    private val mocker = Mocker()
    private val repository: MealsRepository = mocker.mock()
    private lateinit var useCase: RefreshMealsByCategoryUseCase

    @BeforeTest
    fun setUp() {
        mocker.reset()
        useCase = RefreshMealsByCategoryUseCase(repository)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When refresh succeeds, expect loading then success with the synced meals`() = runTest {
        val meals = listOf(Meal(id = "1", name = "Asado", thumbnailUrl = ""))
        mocker.everySuspending { repository.refreshMealsByCategory("Beef") } returns meals

        val states = useCase("Beef").toList()

        assertTrue(states.first() is Resource.Loading)
        val last = states.last()
        assertTrue(last is Resource.Success)
        assertEquals(meals, last.data)
    }

    @Test
    fun `When refresh fails, expect loading then error`() = runTest {
        mocker.everySuspending {
            repository.refreshMealsByCategory("Beef")
        } runs { throw Exception("Error") }

        val states = useCase("Beef").toList()

        assertTrue(states.first() is Resource.Loading)
        val last = states.last()
        assertTrue(last is Resource.Error)
        assertEquals("Error", last.message)
    }
}
