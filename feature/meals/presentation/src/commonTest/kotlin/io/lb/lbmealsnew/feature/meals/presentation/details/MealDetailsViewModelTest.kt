package io.lb.lbmealsnew.feature.meals.presentation.details

import app.cash.turbine.test
import io.lb.lbmealsnew.feature.meals.domain.model.Ingredient
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import io.lb.lbmealsnew.feature.meals.domain.repository.MealsRepository
import io.lb.lbmealsnew.feature.meals.domain.usecase.ObserveMealDetailsUseCase
import io.lb.lbmealsnew.feature.meals.domain.usecase.RefreshMealDetailsUseCase
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
class MealDetailsViewModelTest {
    private val mocker = Mocker()
    private val repository: MealsRepository = mocker.mock()

    private val details = MealDetails(
        id = "42",
        name = "Asado",
        category = "Beef",
        area = "Argentinian",
        instructions = "Grill it.",
        thumbnailUrl = "",
        youtubeUrl = "https://youtube.com/watch?v=42",
        ingredients = listOf(Ingredient(name = "Flank steak", measure = "500g")),
    )

    @BeforeTest
    fun setUp() = mocker.reset()

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    private fun createViewModel() = MealDetailsViewModel(
        mealId = "42",
        mealName = "Asado",
        observeMealDetails = ObserveMealDetailsUseCase(repository),
        refreshMealDetails = RefreshMealDetailsUseCase(repository),
    )

    @Test
    fun `When the screen opens, expect the stored details as success`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealDetails("42") } returns flowOf(details)
        mocker.everySuspending { repository.refreshMealDetails("42") } returns details

        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(MealDetailsState(mealName = "Asado"), awaitItem())
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(details, state.details)
            assertEquals(false, state.isLoading)
            assertEquals("Asado", state.mealName)
        }
    }

    @Test
    fun `When the sync fails with nothing stored, expect error state and a snackbar`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealDetails("42") } returns flowOf(null)
        mocker.everySuspending {
            repository.refreshMealDetails("42")
        } runs { throw Exception("boom") }

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.state.test {
                advanceUntilIdle()
                assertTrue(expectMostRecentItem().hasSyncFailed)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(
                MealDetailsEffect.ShowSnackBar("Couldn't refresh this recipe"),
                awaitItem(),
            )
        }
    }

    @Test
    fun `When YouTube is clicked with loaded details, expect an open url effect`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealDetails("42") } returns flowOf(details)
        mocker.everySuspending { repository.refreshMealDetails("42") } returns details

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effects.test {
            viewModel.onEvent(MealDetailsEvent.OnYoutubeClick)
            assertEquals(
                MealDetailsEffect.OpenUrl("https://youtube.com/watch?v=42"),
                awaitItem(),
            )
        }
    }

    @Test
    fun `When back is clicked, expect a navigation effect`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeMealDetails("42") } returns flowOf(details)
        mocker.everySuspending { repository.refreshMealDetails("42") } returns details

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onEvent(MealDetailsEvent.OnBackClick)
            assertEquals(MealDetailsEffect.NavigateBack, awaitItem())
        }
    }
}
