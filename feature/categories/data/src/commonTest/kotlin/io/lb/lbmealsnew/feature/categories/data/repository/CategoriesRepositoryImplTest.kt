package io.lb.lbmealsnew.feature.categories.data.repository

import io.lb.lbmealsnew.core.databaseapi.CategoriesDatabaseService
import io.lb.lbmealsnew.core.databaseapi.model.LocalCategory
import io.lb.lbmealsnew.core.network.MealDbService
import io.lb.lbmealsnew.core.network.dto.CategoriesResponse
import io.lb.lbmealsnew.core.network.dto.CategoryDto
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

@UsesMocks(MealDbService::class, CategoriesDatabaseService::class)
class CategoriesRepositoryImplTest {
    private val mocker = Mocker()
    private val service: MealDbService = mocker.mock()
    private val databaseService: CategoriesDatabaseService = mocker.mock()
    private lateinit var repository: CategoriesRepositoryImpl

    @BeforeTest
    fun setUp() {
        mocker.reset()
        repository = CategoriesRepositoryImpl(service, databaseService)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When observing categories, expect the stored ones mapped to domain`() = runTest {
        val local = LocalCategory(
            id = "1",
            name = "Beef",
            thumbnailUrl = "https://thumb",
            description = "Meat",
        )
        mocker.every { databaseService.observeCategories() } returns flowOf(listOf(local))

        val categories = repository.observeCategories().first()

        assertEquals(1, categories.size)
        assertEquals("1", categories.first().id)
        assertEquals("Beef", categories.first().name)
        assertEquals("https://thumb", categories.first().thumbnailUrl)
        assertEquals("Meat", categories.first().description)
    }

    @Test
    fun `When refreshing, expect the fetched categories stored and returned`() = runTest {
        val dto = CategoryDto(
            id = "1",
            name = "Beef",
            thumbnailUrl = "https://thumb",
            description = "Meat",
        )
        mocker.everySuspending { service.getCategories() } returns CategoriesResponse(listOf(dto))
        mocker.everySuspending { databaseService.replaceCategories(isAny()) } returns Unit

        val categories = repository.refreshCategories()

        assertEquals(1, categories.size)
        assertEquals("Beef", categories.first().name)
        mocker.verifyWithSuspend(exhaustive = false, inOrder = false) {
            databaseService.replaceCategories(
                listOf(
                    LocalCategory(
                        id = "1",
                        name = "Beef",
                        thumbnailUrl = "https://thumb",
                        description = "Meat",
                    ),
                ),
            )
        }
    }
}
