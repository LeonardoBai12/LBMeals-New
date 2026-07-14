package io.lb.lbmealsnew.feature.categories.domain.usecase

import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
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

@UsesMocks(CategoriesRepository::class)
class RefreshCategoriesUseCaseTest {
    private val mocker = Mocker()
    private val repository: CategoriesRepository = mocker.mock()
    private lateinit var useCase: RefreshCategoriesUseCase

    @BeforeTest
    fun setUp() {
        mocker.reset()
        useCase = RefreshCategoriesUseCase(repository)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When refresh succeeds, expect loading then success with the synced categories`() = runTest {
        val categories = listOf(
            Category(id = "1", name = "Beef", thumbnailUrl = "", description = ""),
        )
        mocker.everySuspending { repository.refreshCategories() } returns categories

        val states = useCase().toList()

        assertTrue(states.first() is Resource.Loading)
        val last = states.last()
        assertTrue(last is Resource.Success)
        assertEquals(categories, last.data)
    }

    @Test
    fun `When refresh fails, expect loading then error`() = runTest {
        mocker.everySuspending { repository.refreshCategories() } runs { throw Exception("Error") }

        val states = useCase().toList()

        assertTrue(states.first() is Resource.Loading)
        val last = states.last()
        assertTrue(last is Resource.Error)
        assertEquals("Error", last.message)
    }
}
