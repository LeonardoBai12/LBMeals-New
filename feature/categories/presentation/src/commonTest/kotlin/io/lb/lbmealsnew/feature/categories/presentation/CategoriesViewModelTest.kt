package io.lb.lbmealsnew.feature.categories.presentation

import app.cash.turbine.test
import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
import io.lb.lbmealsnew.feature.categories.domain.usecase.ObserveCategoriesUseCase
import io.lb.lbmealsnew.feature.categories.domain.usecase.RefreshCategoriesUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock

@UsesMocks(CategoriesRepository::class)
class CategoriesViewModelTest {
    private val mocker = Mocker()
    private val repository: CategoriesRepository = mocker.mock()

    private val categories = listOf(
        Category(id = "1", name = "Beef", thumbnailUrl = "", description = ""),
        Category(id = "2", name = "Dessert", thumbnailUrl = "", description = ""),
    )

    @BeforeTest
    fun setUp() = mocker.reset()

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    private fun createViewModel() = CategoriesViewModel(
        observeCategories = ObserveCategoriesUseCase(repository),
        refreshCategories = RefreshCategoriesUseCase(repository),
    )

    @Test
    fun `When the screen opens, expect the stored categories as success`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeCategories() } returns flowOf(categories)
        mocker.everySuspending { repository.refreshCategories() } returns categories

        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(CategoriesState(), awaitItem())
            advanceUntilIdle()
            val state = expectMostRecentItem()
            val content = state.categories
            assertTrue(content is Resource.Success)
            assertEquals(categories, content.data)
        }
    }

    @Test
    fun `When the search query changes, expect only matching categories`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeCategories() } returns flowOf(categories)
        mocker.everySuspending { repository.refreshCategories() } returns categories

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(CategoriesEvent.OnSearchQueryChange("des"))
            advanceUntilIdle()
            val state = expectMostRecentItem()
            val content = state.categories
            assertTrue(content is Resource.Success)
            assertEquals(listOf("Dessert"), content.data.map { it.name })
            assertEquals("des", state.searchQuery)
        }
    }

    @Test
    fun `When a category is clicked, expect a navigation effect`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeCategories() } returns flowOf(categories)
        mocker.everySuspending { repository.refreshCategories() } returns categories

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onEvent(CategoriesEvent.OnCategoryClick("Beef"))
            assertEquals(CategoriesEffect.NavigateToMeals("Beef"), awaitItem())
        }
    }

    @Test
    fun `When the sync fails with nothing stored, expect error state and a snackbar`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeCategories() } returns flowOf(emptyList())
        mocker.everySuspending { repository.refreshCategories() } runs { throw Exception("boom") }

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.state.test {
                advanceUntilIdle()
                val state = expectMostRecentItem()
                assertTrue(state.categories is Resource.Error)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(
                CategoriesEffect.ShowSnackbar("Couldn't refresh categories"),
                awaitItem(),
            )
        }
    }

    @Test
    fun `When the user pulls to refresh, expect the refreshing indicator only then`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        mocker.every { repository.observeCategories() } returns flowOf(categories)
        mocker.everySuspending { repository.refreshCategories() } runs { awaitCancellation() }

        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            advanceUntilIdle()
            // The automatic sync on entry never drives the indicator.
            assertEquals(false, expectMostRecentItem().isRefreshing)

            viewModel.onEvent(CategoriesEvent.OnRefresh)
            advanceUntilIdle()
            assertEquals(true, expectMostRecentItem().isRefreshing)
        }
    }
}
