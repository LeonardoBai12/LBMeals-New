package io.lb.lbmealsnew.feature.meals.presentation.listing

import app.cash.turbine.test
import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import io.lb.lbmealsnew.feature.meals.domain.usecase.ObserveMealsByCategoryUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.RefreshMealsByCategoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@UsesMocks(MealsRepository::class)
class MealsViewModelTest {
    private val mocker = Mocker()
    private val repository: MealsRepository = mocker.mock()

    private val meals = listOf(
        Meal(id = "1", name = "Asado", thumbnailUrl = ""),
        Meal(id = "2", name = "Aussie Burgers", thumbnailUrl = ""),
    )

    @BeforeTest
    fun setUp() = mocker.reset()

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    private fun createViewModel() = MealsViewModel(
        category = "Beef",
        observeMealsByCategory = ObserveMealsByCategoryUseCase(repository),
        refreshMealsByCategory = RefreshMealsByCategoryUseCase(repository),
    )

    @Test
    fun `When the screen opens, expect the stored meals as success`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealsByCategory("Beef") } returns flowOf(meals)
        mocker.everySuspending { repository.refreshMealsByCategory("Beef") } returns meals

        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(MealsState(category = "Beef"), awaitItem())
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(meals, state.meals)
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `When the search query changes, expect only matching meals`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealsByCategory("Beef") } returns flowOf(meals)
        mocker.everySuspending { repository.refreshMealsByCategory("Beef") } returns meals

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(MealsEvent.OnSearchQueryChange("asado"))
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(listOf("Asado"), state.meals.map { it.name })
        }
    }

    @Test
    fun `When a meal is clicked, expect a navigation effect`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealsByCategory("Beef") } returns flowOf(meals)
        mocker.everySuspending { repository.refreshMealsByCategory("Beef") } returns meals

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onEvent(MealsEvent.OnMealClick("1", "Asado", "https://thumb"))
            assertEquals(
                MealsEffect.NavigateToDetails("1", "Asado", "https://thumb"),
                awaitItem(),
            )
        }
    }

    @Test
    fun `When back is clicked, expect a navigation effect`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealsByCategory("Beef") } returns flowOf(meals)
        mocker.everySuspending { repository.refreshMealsByCategory("Beef") } returns meals

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onEvent(MealsEvent.OnBackClick)
            assertEquals(MealsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `When the sync fails with nothing stored, expect error state and a snackbar`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealsByCategory("Beef") } returns flowOf(emptyList())
        mocker.everySuspending {
            repository.refreshMealsByCategory("Beef")
        } runs { throw Exception("boom") }

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.state.test {
                advanceUntilIdle()
                assertTrue(expectMostRecentItem().hasSyncFailed)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(MealsEffect.ShowSnackBar("Couldn't refresh meals"), awaitItem())
        }
    }
}
