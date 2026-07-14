package io.lb.lbmealsnew.feature.meals.presentation.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.lb.lbmealsnew.core.common.Resource
import io.lb.lbmealsnew.core.designsystem.components.TEST_TAG_LB_LOADING
import io.lb.lbmealsnew.feature.meals.domain.model.Ingredient
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Renders [MealDetailsScreen] against each shape of [MealDetailsState] and
 * asserts the screen tells them apart: loading only while data is expected,
 * error only when the sync failed, and content once details are stored.
 */
class MealDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun renderScreen(
        state: MealDetailsState,
        onEvent: (MealDetailsEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            MealDetailsScreen(
                state = state,
                onEvent = onEvent,
                effects = MutableSharedFlow(),
                onNavigateBack = {},
            )
        }
    }

    @Test
    fun loading_showsLoadingIndicator_andKnownMealName() {
        renderScreen(MealDetailsState(mealName = "Asado", details = Resource.Loading))

        composeRule.onNodeWithTag(TEST_TAG_LB_LOADING).assertIsDisplayed()
        composeRule.onNodeWithText("Asado").assertIsDisplayed()
    }

    @Test
    fun error_showsRetryEmptyState() {
        renderScreen(MealDetailsState(mealName = "Asado", details = Resource.Error()))

        composeRule
            .onNodeWithText("Couldn't load this recipe.", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun error_retryClick_sendsRefreshEvent() {
        val events = mutableListOf<MealDetailsEvent>()
        renderScreen(
            MealDetailsState(mealName = "Asado", details = Resource.Error()),
            onEvent = events::add,
        )

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(listOf<MealDetailsEvent>(MealDetailsEvent.OnRefresh), events)
    }

    @Test
    fun success_showsDetailsContent() {
        renderScreen(MealDetailsState(mealName = "Asado", details = Resource.Success(details())))

        composeRule.onNodeWithText("Ingredients").assertIsDisplayed()
        composeRule.onNodeWithText("Flank steak").assertIsDisplayed()
        composeRule.onNodeWithText("500g").assertIsDisplayed()
        composeRule.onNodeWithText("Instructions").assertIsDisplayed()
        composeRule.onNodeWithText("Grill it.").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_LB_LOADING).assertDoesNotExist()
    }

    @Test
    fun success_withYoutubeUrl_youtubeClick_sendsEvent() {
        val events = mutableListOf<MealDetailsEvent>()
        renderScreen(
            MealDetailsState(mealName = "Asado", details = Resource.Success(details())),
            onEvent = events::add,
        )

        composeRule.onNodeWithText("Watch on YouTube").performClick()

        assertEquals(listOf<MealDetailsEvent>(MealDetailsEvent.OnYoutubeClick), events)
    }

    private fun details() = MealDetails(
        id = "42",
        name = "Asado",
        category = "Beef",
        area = "Argentinian",
        instructions = "Grill it.",
        thumbnailUrl = "",
        youtubeUrl = "https://youtube.com/watch?v=42",
        ingredients = listOf(Ingredient(name = "Flank steak", measure = "500g")),
    )
}
