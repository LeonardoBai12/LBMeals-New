package io.lb.lbmealsnew.feature.categories.domain.usecase

import io.lb.lbmealsnew.feature.categories.domain.model.Category
import io.lb.lbmealsnew.feature.categories.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mocker
import org.kodein.mock.UsesMocks
import org.kodein.mock.generated.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@UsesMocks(CategoriesRepository::class)
class ObserveCategoriesUseCaseTest {
    private val mocker = Mocker()
    private val repository: CategoriesRepository = mocker.mock()
    private lateinit var useCase: ObserveCategoriesUseCase

    @BeforeTest
    fun setUp() {
        mocker.reset()
        useCase = ObserveCategoriesUseCase(repository)
    }

    @AfterTest
    fun tearDown() = mocker.reset()

    @Test
    fun `When observing categories, expect the stored categories`() = runTest {
        val categories = listOf(
            Category(id = "1", name = "Beef", thumbnailUrl = "", description = ""),
            Category(id = "2", name = "Dessert", thumbnailUrl = "", description = ""),
        )
        mocker.every { repository.observeCategories() } returns flowOf(categories)

        assertEquals(categories, useCase().first())
    }
}
