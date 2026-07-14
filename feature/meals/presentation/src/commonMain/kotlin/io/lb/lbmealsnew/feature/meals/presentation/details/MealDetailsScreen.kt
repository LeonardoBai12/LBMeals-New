package io.lb.lbmealsnew.feature.meals.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import io.lb.lbmealsnew.core.designsystem.components.LBEmptyState
import io.lb.lbmealsnew.core.designsystem.components.LBFloatingBackButton
import io.lb.lbmealsnew.core.designsystem.components.LBLoading
import io.lb.lbmealsnew.core.designsystem.components.LBRemoteImage
import io.lb.lbmealsnew.core.designsystem.transition.lbSharedElement
import io.lb.lbmealsnew.feature.meals.domain.model.MealDetails
import kotlinx.coroutines.flow.SharedFlow

private val HEADER_MAX_HEIGHT = 320.dp
private val HEADER_MIN_HEIGHT = 120.dp
private const val HEADER_COLLAPSED_ALPHA = 0.4f
private const val VIDEO_ASPECT_RATIO = 16f / 9f
private val BOTTOM_SPACER_HEIGHT = 32.dp

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

    // Surface stands in for the removed Scaffold: it paints the themed
    // background and provides the matching content color — without it the
    // window background leaks through and text falls back to black.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onEvent(MealDetailsEvent.OnRefresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                MealDetailsContent(state, onEvent)
            }

            // No top bar: the photo owns the whole top of the screen, and
            // the back button floats over it.
            LBFloatingBackButton(
                onClick = { onEvent(MealDetailsEvent.OnBackClick) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 8.dp),
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * Details with a collapsing image header: the photo starts tall enough to
 * show the whole dish and shrinks as the content scrolls up, so it never
 * permanently costs the reading space.
 *
 * The header renders from [MealDetailsState.thumbnailUrl] — known before the
 * sync lands — so the shared element transition has its target from the
 * first frame and the image never reloads when the details arrive.
 */
@Composable
private fun MealDetailsContent(
    state: MealDetailsState,
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
            url = state.thumbnailUrl,
            contentDescription = state.mealName,
            modifier = Modifier
                .lbSharedElement("meal-image-${state.mealId}")
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

        val details = state.details
        when {
            details != null -> MealDetailsList(details, onEvent)

            state.hasSyncFailed -> {
                MealTitle(state.mealName)
                LBEmptyState(
                    message = "Couldn't load this recipe.\nCheck your connection and try again.",
                    actionLabel = "Retry",
                    onActionClick = { onEvent(MealDetailsEvent.OnRefresh) },
                )
            }

            else -> {
                MealTitle(state.mealName)
                LBLoading()
            }
        }
    }
}

@Composable
private fun MealTitle(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
    )
}

@Composable
private fun MealDetailsList(
    details: MealDetails,
    onEvent: (MealDetailsEvent) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // The title scrolls away with the content instead of staying pinned
        // and costing reading space.
        item {
            MealTitle(details.name)
        }

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

        details.youtubeUrl?.let { youtubeUrl ->
            item {
                YoutubePreviewCard(
                    youtubeUrl = youtubeUrl,
                    onClick = { onEvent(MealDetailsEvent.OnYoutubeClick) },
                )
            }
        }

        // Breathing room below the last item: on rounded-corner screens, text
        // or the video thumbnail would otherwise run right into the curved
        // edge and the gesture bar.
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(BOTTOM_SPACER_HEIGHT),
            )
        }
    }
}

/**
 * YouTube preview: the video's own thumbnail (derived from its URL, no API
 * needed) under a play badge and a bottom scrim, opening the video on click.
 * Falls back to a plain button when the video id cannot be parsed.
 */
@Composable
private fun YoutubePreviewCard(
    youtubeUrl: String,
    onClick: () -> Unit,
) {
    val videoId = remember(youtubeUrl) { youtubeVideoId(youtubeUrl) }
    if (videoId == null) {
        WatchOnYoutubeButton(onClick)
        return
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Box {
            // Cropping the letterboxed 4:3 thumbnail to 16:9 removes the
            // black bars exactly.
            LBRemoteImage(
                url = youtubeThumbnailUrl(videoId),
                contentDescription = "Video thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(VIDEO_ASPECT_RATIO),
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        ),
                    ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(color = YoutubeRed, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }

            Text(
                text = "Watch on YouTube",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun WatchOnYoutubeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
