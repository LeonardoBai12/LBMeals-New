package io.lb.lbmealsnew.feature.categories.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.lb.lbmealsnew.core.designsystem.components.TEST_TAG_LB_LOADING
import io.lb.lbmealsnew.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Renders [CategoriesScreen] against each shape of [CategoriesState] and
 * asserts the screen tells them apart: loading only while data is expected,
 * error only when the sync failed, and empty content shown as empty — never
 * as loading or error.
 */
class CategoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun renderScreen(
        state: CategoriesState,
        onEvent: (CategoriesEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            CategoriesScreen(
                state = state,
                onEvent = onEvent,
                effects = MutableSharedFlow(),
                onNavigateToMeals = {},
            )
        }
    }

    @Test
    fun loading_showsLoadingIndicator() {
        renderScreen(CategoriesState())

        composeRule.onNodeWithTag(TEST_TAG_LB_LOADING).assertIsDisplayed()
    }

    @Test
    fun error_showsRetryEmptyState() {
        renderScreen(CategoriesState(isLoading = false, hasSyncFailed = true))

        composeRule
            .onNodeWithText("No categories yet.", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun error_retryClick_sendsRefreshEvent() {
        val events = mutableListOf<CategoriesEvent>()
        renderScreen(CategoriesState(isLoading = false, hasSyncFailed = true), onEvent = events::add)

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(listOf<CategoriesEvent>(CategoriesEvent.OnRefresh), events)
    }

    @Test
    fun successEmpty_showsEmptyMessage_notLoadingNorError() {
        renderScreen(CategoriesState(isLoading = false))

        composeRule.onNodeWithText("No categories available.").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_LB_LOADING).assertDoesNotExist()
        composeRule.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun successEmpty_withSearchQuery_showsNoMatchMessage() {
        renderScreen(CategoriesState(isLoading = false, searchQuery = "xyz"))

        composeRule.onNodeWithText("No categories match \"xyz\"").assertIsDisplayed()
    }

    @Test
    fun successWithContent_showsCategories() {
        renderScreen(
            CategoriesState(
                categories = listOf(category("1", "Beef"), category("2", "Dessert")),
                isLoading = false,
            ),
        )

        composeRule.onNodeWithText("Beef").assertIsDisplayed()
        composeRule.onNodeWithText("Dessert").assertIsDisplayed()
    }

    @Test
    fun successWithContent_categoryClick_sendsClickEvent() {
        val events = mutableListOf<CategoriesEvent>()
        renderScreen(
            CategoriesState(categories = listOf(category("1", "Beef")), isLoading = false),
            onEvent = events::add,
        )

        composeRule.onNodeWithText("Beef").performClick()

        assertEquals(listOf<CategoriesEvent>(CategoriesEvent.OnCategoryClick("Beef")), events)
    }

    private fun category(id: String, name: String) = Category(
        id = id,
        name = name,
        thumbnailUrl = "",
        description = "",
    )
}
