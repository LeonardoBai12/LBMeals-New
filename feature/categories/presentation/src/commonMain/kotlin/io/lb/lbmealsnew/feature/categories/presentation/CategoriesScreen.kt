package io.lb.lbmealsnew.feature.categories.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.lb.lbmealsnew.core.designsystem.components.LBEmptyState
import io.lb.lbmealsnew.core.designsystem.components.LBFloatingCard
import io.lb.lbmealsnew.core.designsystem.components.LBLoading
import io.lb.lbmealsnew.core.designsystem.components.LBLogo
import io.lb.lbmealsnew.core.designsystem.components.LBSearchBar
import io.lb.lbmealsnew.core.designsystem.components.lbFrostedBarStyle
import io.lb.lbmealsnew.core.designsystem.scroll.rememberHideOnScrollState
import io.lb.lbmealsnew.core.designsystem.transition.lbSharedBounds
import kotlinx.coroutines.flow.SharedFlow

/**
 * Room the floating search bar occupies above the grid: the field's height
 * plus its vertical padding.
 */
private val SEARCH_BAR_SPACE = 72.dp

/**
 * Stateless categories screen: renders [state], reports user actions through
 * [onEvent] and consumes one-shot [effects]. Owns no state of its own.
 *
 * The grid scrolls behind a frosted-glass top bar and a floating, slightly
 * translucent search bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    state: CategoriesState,
    onEvent: (CategoriesEvent) -> Unit,
    effects: SharedFlow<CategoriesEffect>,
    onNavigateToMeals: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                is CategoriesEffect.ShowSnackBar ->
                    snackbarHostState.showSnackbar(effect.message)

                is CategoriesEffect.NavigateToMeals ->
                    onNavigateToMeals(effect.category)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LBLogo(modifier = Modifier.height(40.dp)) },
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
        val searchBarState = rememberHideOnScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(searchBarState.nestedScrollConnection),
        ) {
            val pullState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onEvent(CategoriesEvent.OnRefresh) },
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
                CategoriesContent(state, onEvent, contentTopPadding = topPadding + SEARCH_BAR_SPACE)
            }

            // Scrolling down tucks the search bar away; scrolling back up
            // brings it back.
            AnimatedVisibility(
                visible = searchBarState.isVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.padding(top = topPadding),
            ) {
                LBSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(CategoriesEvent.OnSearchQueryChange(it)) },
                    placeholder = "Search categories",
                )
            }
        }
    }
}

@Composable
private fun CategoriesContent(
    state: CategoriesState,
    onEvent: (CategoriesEvent) -> Unit,
    contentTopPadding: Dp,
) {
    when {
        state.isLoading -> LBLoading()

        state.categories.isEmpty() && state.searchQuery.isNotBlank() ->
            LBEmptyState(message = "No categories match \"${state.searchQuery}\"")

        state.categories.isEmpty() && state.hasSyncFailed -> LBEmptyState(
            message = "No categories yet.\nCheck your connection and try again.",
            actionLabel = "Retry",
            onActionClick = { onEvent(CategoriesEvent.OnRefresh) },
        )

        state.categories.isEmpty() -> LBEmptyState(message = "No categories available.")

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
            items(state.categories, key = { it.id }) { category ->
                LBFloatingCard(
                    title = category.name,
                    imageUrl = category.thumbnailUrl,
                    onClick = { onEvent(CategoriesEvent.OnCategoryClick(category.name)) },
                    // Container transform: the card's bounds morph into the
                    // meals screen it opens.
                    modifier = Modifier.lbSharedBounds("category-${category.name}"),
                )
            }
        }
    }
}
