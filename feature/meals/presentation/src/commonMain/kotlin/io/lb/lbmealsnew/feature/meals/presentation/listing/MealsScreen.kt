package io.lb.lbmealsnew.feature.meals.presentation.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.lb.lbmealsnew.core.designsystem.components.LBBackButton
import io.lb.lbmealsnew.core.designsystem.components.LBEmptyState
import io.lb.lbmealsnew.core.designsystem.components.LBLoading
import io.lb.lbmealsnew.core.designsystem.components.LBSearchBar
import io.lb.lbmealsnew.core.designsystem.components.LBThumbnailCard
import io.lb.lbmealsnew.core.designsystem.components.lbFrostedBarStyle
import kotlinx.coroutines.flow.SharedFlow

/**
 * Room the floating search bar occupies above the grid: the field's height
 * plus its vertical padding.
 */
private val SEARCH_BAR_SPACE = 72.dp

/**
 * Stateless meals screen: renders [state], reports user actions through
 * [onEvent] and consumes one-shot [effects]. Owns no state of its own.
 *
 * The grid scrolls behind a frosted-glass top bar and a floating, slightly
 * translucent search bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(
    state: MealsState,
    onEvent: (MealsEvent) -> Unit,
    effects: SharedFlow<MealsEffect>,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (id: String, name: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                is MealsEffect.ShowSnackBar -> snackbarHostState.showSnackbar(effect.message)
                MealsEffect.NavigateBack -> onNavigateBack()
                is MealsEffect.NavigateToDetails -> onNavigateToDetails(effect.id, effect.name)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.category) },
                navigationIcon = {
                    LBBackButton(onClick = { onEvent(MealsEvent.OnBackClick) })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                modifier = Modifier.hazeEffect(state = hazeState, style = lbFrostedBarStyle()),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val topPadding = padding.calculateTopPadding()

        Box(modifier = Modifier.fillMaxSize()) {
            val pullState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onEvent(MealsEvent.OnRefresh) },
                state = pullState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
                indicator = {
                    // The box starts behind the frosted bars, so the default
                    // top-aligned indicator would be hidden by them.
                    PullToRefreshDefaults.Indicator(
                        state = pullState,
                        isRefreshing = state.isRefreshing,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = topPadding + SEARCH_BAR_SPACE),
                    )
                },
            ) {
                MealsContent(state, onEvent, contentTopPadding = topPadding + SEARCH_BAR_SPACE)
            }

            LBSearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(MealsEvent.OnSearchQueryChange(it)) },
                placeholder = "Search meals",
                modifier = Modifier.padding(top = topPadding),
            )
        }
    }
}

@Composable
private fun MealsContent(
    state: MealsState,
    onEvent: (MealsEvent) -> Unit,
    contentTopPadding: Dp,
) {
    when {
        state.isLoading -> LBLoading()

        state.meals.isEmpty() && state.searchQuery.isNotBlank() ->
            LBEmptyState(message = "No meals match \"${state.searchQuery}\"")

        state.meals.isEmpty() && state.hasSyncFailed -> LBEmptyState(
            message = "No meals yet.\nCheck your connection and try again.",
            actionLabel = "Retry",
            onActionClick = { onEvent(MealsEvent.OnRefresh) },
        )

        state.meals.isEmpty() ->
            LBEmptyState(message = "No meals in this category yet.")

        else -> LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentTopPadding,
                bottom = 16.dp,
            ),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.meals, key = { it.id }) { meal ->
                LBThumbnailCard(
                    title = meal.name,
                    imageUrl = meal.thumbnailUrl,
                    onClick = { onEvent(MealsEvent.OnMealClick(meal.id, meal.name)) },
                )
            }
        }
    }
}
