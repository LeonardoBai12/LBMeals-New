package io.lb.lbmealsnew.feature.meals.presentation.listing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.lb.lbmealsnew.core.designsystem.components.TEST_TAG_LB_LOADING
import io.lb.lbmealsnew.feature.meals.domain.model.Meal
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Renders [MealsScreen] against each shape of [MealsState] and asserts the
 * screen tells them apart: loading only while data is expected, error only
 * when the sync failed, and empty content shown as empty — never as loading
 * or error.
 */
class MealsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun renderScreen(
        state: MealsState,
        onEvent: (MealsEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            MealsScreen(
                state = state,
                onEvent = onEvent,
                effects = MutableSharedFlow(),
                onNavigateBack = {},
                onNavigateToDetails = { _, _ -> },
            )
        }
    }

    @Test
    fun loading_showsLoadingIndicator() {
        renderScreen(MealsState(category = "Beef"))

        composeRule.onNodeWithTag(TEST_TAG_LB_LOADING).assertIsDisplayed()
    }

    @Test
    fun error_showsRetryEmptyState() {
        renderScreen(MealsState(category = "Beef", isLoading = false, hasSyncFailed = true))

        composeRule
            .onNodeWithText("No meals yet.", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun error_retryClick_sendsRefreshEvent() {
        val events = mutableListOf<MealsEvent>()
        renderScreen(MealsState(category = "Beef", isLoading = false, hasSyncFailed = true), onEvent = events::add)

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(listOf<MealsEvent>(MealsEvent.OnRefresh), events)
    }

    @Test
    fun successEmpty_showsEmptyMessage_notLoadingNorError() {
        renderScreen(MealsState(category = "Beef", isLoading = false))

        composeRule.onNodeWithText("No meals in this category yet.").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_LB_LOADING).assertDoesNotExist()
        composeRule.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun successEmpty_withSearchQuery_showsNoMatchMessage() {
        renderScreen(MealsState(category = "Beef", isLoading = false, searchQuery = "xyz"))

        composeRule.onNodeWithText("No meals match \"xyz\"").assertIsDisplayed()
    }

    @Test
    fun successWithContent_showsMeals() {
        renderScreen(
            MealsState(
                category = "Beef",
                meals = listOf(meal("1", "Asado"), meal("2", "Arepa pelua")),
                isLoading = false,
            ),
        )

        composeRule.onNodeWithText("Asado").assertIsDisplayed()
        composeRule.onNodeWithText("Arepa pelua").assertIsDisplayed()
    }

    @Test
    fun successWithContent_mealClick_sendsClickEvent() {
        val events = mutableListOf<MealsEvent>()
        renderScreen(
            MealsState(category = "Beef", meals = listOf(meal("42", "Asado")), isLoading = false),
            onEvent = events::add,
        )

        composeRule.onNodeWithText("Asado").performClick()

        assertEquals(listOf<MealsEvent>(MealsEvent.OnMealClick("42", "Asado")), events)
    }

    private fun meal(id: String, name: String) = Meal(
        id = id,
        name = name,
        thumbnailUrl = "",
    )
}
