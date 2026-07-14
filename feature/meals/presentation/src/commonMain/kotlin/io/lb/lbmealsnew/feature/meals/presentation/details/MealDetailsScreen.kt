package io.lb.lbmealsnew.feature.meals.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.lb.lbmealsnew.core.designsystem.components.LBBackButton
import io.lb.lbmealsnew.core.designsystem.components.LBEmptyState
import io.lb.lbmealsnew.core.designsystem.components.LBLoading
import io.lb.lbmealsnew.core.designsystem.components.LBRemoteImage
import io.lb.lbmealsnew.core.designsystem.components.lbFrostedBarStyle
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import kotlinx.coroutines.flow.SharedFlow

private val HEADER_MAX_HEIGHT = 320.dp
private val HEADER_MIN_HEIGHT = 120.dp
private const val HEADER_COLLAPSED_ALPHA = 0.4f

/**
 * YouTube's brand red — fixed on both themes, like the brand itself.
 */
private val YoutubeRed = Color(0xFFFF0000)

/**
 * Stateless meal details screen: renders [state], reports user actions
 * through [onEvent] and consumes one-shot [effects]. Owns no state of its own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailsScreen(
    state: MealDetailsState,
    onEvent: (MealDetailsEvent) -> Unit,
    effects: SharedFlow<MealDetailsEffect>,
    onNavigateBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                is MealDetailsEffect.ShowSnackBar -> snackbarHostState.showSnackbar(effect.message)
                MealDetailsEffect.NavigateBack -> onNavigateBack()
                is MealDetailsEffect.OpenUrl -> uriHandler.openUri(effect.url)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.mealName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    LBBackButton(onClick = { onEvent(MealDetailsEvent.OnBackClick) })
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
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(MealDetailsEvent.OnRefresh) },
            // Unlike the listing screens, the content starts below the bar:
            // the recipe photo is the hero here and is never occluded.
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .hazeSource(hazeState),
        ) {
            val details = state.details
            when {
                details != null -> MealDetailsContent(details, onEvent)

                state.hasSyncFailed -> LBEmptyState(
                    message = "Couldn't load this recipe.\nCheck your connection and try again.",
                    actionLabel = "Retry",
                    onActionClick = { onEvent(MealDetailsEvent.OnRefresh) },
                )

                else -> LBLoading()
            }
        }
    }
}

/**
 * Details with a collapsing image header: the photo starts tall enough to
 * show the whole dish and shrinks as the content scrolls up, so it never
 * permanently costs the reading space.
 */
@Composable
private fun MealDetailsContent(
    details: MealDetails,
    onEvent: (MealDetailsEvent) -> Unit,
) {
    val density = LocalDensity.current
    val minHeightPx = with(density) { HEADER_MIN_HEIGHT.toPx() }
    val maxHeightPx = with(density) { HEADER_MAX_HEIGHT.toPx() }
    var headerHeightPx by rememberSaveable { mutableFloatStateOf(maxHeightPx) }

    // Scrolling up collapses the header before the list scrolls (pre-scroll);
    // scrolling down expands it only with what the list left over after
    // reaching its top (post-scroll).
    val collapsingConnection = remember(minHeightPx, maxHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0) return Offset.Zero
                val newHeight = (headerHeightPx + available.y).coerceAtLeast(minHeightPx)
                val consumed = newHeight - headerHeightPx
                headerHeightPx = newHeight
                return Offset(0f, consumed)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y <= 0) return Offset.Zero
                val newHeight = (headerHeightPx + available.y).coerceAtMost(maxHeightPx)
                val used = newHeight - headerHeightPx
                headerHeightPx = newHeight
                return Offset(0f, used)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(collapsingConnection),
    ) {
        LBRemoteImage(
            url = details.thumbnailUrl,
            contentDescription = details.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { headerHeightPx.toDp() })
                // The image fades towards HEADER_COLLAPSED_ALPHA in lockstep
                // with the collapse, so the transition is as smooth as the
                // scroll gesture itself.
                .graphicsLayer {
                    val collapseFraction =
                        (maxHeightPx - headerHeightPx) / (maxHeightPx - minHeightPx)
                    alpha = lerp(1f, HEADER_COLLAPSED_ALPHA, collapseFraction)
                },
            contentScale = ContentScale.Crop,
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val tags = listOfNotNull(details.category, details.area)
                    Text(
                        text = tags.joinToString(" • "),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            item {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            items(details.ingredients) { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = ingredient.measure,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            item {
                Text(
                    text = details.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (details.youtubeUrl != null) {
                item {
                    Button(
                        onClick = { onEvent(MealDetailsEvent.OnYoutubeClick) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YoutubeRed,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Watch on YouTube")
                    }
                }
            }
        }
    }
}
