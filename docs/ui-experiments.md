# UI experiments

Small notes-to-future-self on a handful of Compose Multiplatform techniques used
in this app's screens. Each one solves a concrete, common problem — the goal
here is to remember *why* each piece exists, not just that it does.

## 1. Frosted-glass top bars (Haze)

**Problem:** an opaque top bar over a scrolling list looks flat; a fully
transparent one makes the text behind it unreadable once content scrolls under
it.

**Solution:** [Haze](https://github.com/chrisbanes/haze) blurs whatever
scrolls behind a composable, in real time. The pattern has two sides:

- the scrolling content is marked as a blur *source*
- the bar reads that source and renders a blurred backdrop *tint* of it

```kotlin
// core/designsystem: shared style for every top bar in the app
@Composable
fun lbFrostedBarStyle(): HazeStyle = HazeStyle(
    backgroundColor = MaterialTheme.colorScheme.surface,
    tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))),
    blurRadius = 20.dp,
)
```

```kotlin
// screen: one HazeState shared between the source and the bar
val hazeState = remember { HazeState() }

LazyColumn(modifier = Modifier.hazeSource(hazeState)) { /* ... */ }

TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    modifier = Modifier.hazeEffect(state = hazeState, style = lbFrostedBarStyle()),
)
```

**Gotcha:** the bar's own container color must be transparent — Haze paints
the blur itself, so a solid `containerColor` just hides it underneath.

## 2. Search bar that hides on scroll

**Problem:** a search bar pinned at the top of a listing screen permanently
costs vertical space that could show content instead.

**Solution:** a small, reusable `NestedScrollConnection` that tracks scroll
direction and exposes a single boolean — no accumulator, no threshold tuning,
just "did we just scroll down or up":

```kotlin
@Stable
class HideOnScrollState {
    var isVisible by mutableStateOf(true)
        private set

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y < -SCROLL_DEADZONE_PX) isVisible = false
            if (available.y > SCROLL_DEADZONE_PX) isVisible = true
            return Offset.Zero
        }
    }
}
```

The screen attaches it to the scrolling container and drives an
`AnimatedVisibility` around the search bar:

```kotlin
val searchBarState = rememberHideOnScrollState()

Box(modifier = Modifier.nestedScroll(searchBarState.nestedScrollConnection)) {
    /* ...scrolling list... */

    AnimatedVisibility(
        visible = searchBarState.isVisible,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
    ) {
        LBSearchBar(/* ... */)
    }
}
```

**Design note (learned by trial and error):** the first version tried a
scroll-distance accumulator with a hysteresis threshold, aiming for a more
"deliberate" show/hide gesture. In practice the simple deadzone above felt
better — it reacts immediately to direction changes, which is what users
actually expect from this pattern on iOS. Resist the urge to over-engineer a
scroll heuristic before checking whether the simplest version already feels
right.

**Design note 2:** the field's container uses `surfaceContainerHigh` (a tonal
step above the background) instead of a near-transparent `surface`. A field
that is *too* translucent disappears against a plain background and only
reads as a search bar when it happens to float over an image — the tonal
fill keeps it legible everywhere.

## 3. Shared element transitions

**Problem:** navigating from a list item to its details screen with a hard
cut feels disconnected; the user loses track of which item they tapped.

**Solution:** Compose's `SharedTransitionLayout`, wrapped once around the
whole navigation graph, plus two small extension functions so every screen
opts in with a one-liner:

```kotlin
// composeApp: the whole NavHost lives inside one shared transition scope
SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
        NavHost(navController, startDestination = CategoriesRoute) { /* ... */ }
    }
}
```

```kotlin
// core/designsystem/transition: no-op outside a shared transition layout,
// which keeps previews and tests working without any extra setup
@Composable
fun Modifier.lbSharedElement(key: Any): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this

    return with(sharedTransitionScope) {
        this@lbSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
```

Two flavors are used, for two different visual effects:

- **`lbSharedElement`** — the *same* element morphs position/size between
  screens. Used for the meal photo: it's the same `LBRemoteImage`
  composable, keyed by `"meal-image-${mealId}"`, on both the list and the
  details screen.
- **`lbSharedBounds`** (Material's *container transform*) — two *different*
  composables keep their own content, only their bounds morph into each
  other. Used for the category card expanding into the meals screen: the
  card and the destination screen are different layouts, so only the
  container should animate.

**Gotcha:** both helpers need `LocalNavAnimatedVisibilityScope`, which each
`composable {}` destination must provide from its own `AnimatedContentScope` —
without it, the modifiers silently no-op instead of crashing, which is
correct for tests/previews but easy to misdiagnose ("why isn't it animating?")
in a real screen if the local was simply never set.

## 4. Collapsing header with a fading image

**Problem:** the recipe photo deserves to be the hero of the details screen,
but a fixed-height photo either wastes space once the user starts reading, or
a small photo undersells the dish on first open.

**Solution:** a second, purpose-built `NestedScrollConnection` that drives the
header's *height* directly (not via a separate scroll offset state), consuming
scroll delta before the list gets it:

```kotlin
val collapsingConnection = remember(minHeightPx, maxHeightPx) {
    object : NestedScrollConnection {
        // Scrolling up shrinks the header first (pre-scroll) — the list only
        // starts moving once the header is fully collapsed.
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y >= 0) return Offset.Zero
            val newHeight = (headerHeightPx + available.y).coerceAtLeast(minHeightPx)
            val consumed = newHeight - headerHeightPx
            headerHeightPx = newHeight
            return Offset(0f, consumed)
        }

        // Scrolling down only re-expands the header with what the list
        // didn't need (post-scroll) — i.e. only once the list is already at
        // its own top.
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (available.y <= 0) return Offset.Zero
            val newHeight = (headerHeightPx + available.y).coerceAtMost(maxHeightPx)
            val used = newHeight - headerHeightPx
            headerHeightPx = newHeight
            return Offset(0f, used)
        }
    }
}
```

The image's alpha is derived from the *same* height state via `graphicsLayer`,
so the fade and the collapse are always perfectly in sync — there's no
separate animation to desync from the gesture:

```kotlin
.graphicsLayer {
    val collapseFraction = (maxHeightPx - headerHeightPx) / (maxHeightPx - minHeightPx)
    alpha = lerp(1f, HEADER_COLLAPSED_ALPHA, collapseFraction)
}
```

**Design note:** the header never disappears (`HEADER_COLLAPSED_ALPHA = 0.4f`,
`HEADER_MIN_HEIGHT = 120.dp`) — the photo stays as a small, dimmed strip
rather than vanishing, so the screen never fully loses its visual anchor.

## 5. Stable image layout with Coil (no empty-space jump)

**Problem:** a remote image with an unconstrained height collapses to zero
height while loading, then suddenly jumps to its final size once it arrives —
the classic content-layout-shift.

**Solution:** `LBRemoteImage` accepts an optional `placeholderAspectRatio`. It
reserves that ratio immediately (so there's never a zero-height gap), reads
the real image's intrinsic size once Coil reports success, and switches the
reserved ratio over to the real one — one placeholder jump instead of a
"grows from nothing" jump, and zero jump at all when the caller already knows
the right ratio:

```kotlin
var imageAspectRatio by remember(url) { mutableFloatStateOf(0f) }
val reservedRatio = when {
    placeholderAspectRatio == null -> null
    imageAspectRatio > 0f -> imageAspectRatio
    else -> placeholderAspectRatio
}

AsyncImage(
    model = ImageRequest.Builder(LocalPlatformContext.current).data(url).crossfade(true).build(),
    onState = { state ->
        if (state is AsyncImagePainter.State.Success) {
            val size = state.painter.intrinsicSize
            if (size.width > 0f && size.height > 0f) {
                imageAspectRatio = size.width / size.height
            }
        }
    },
    modifier = if (reservedRatio != null) modifier.aspectRatio(reservedRatio) else modifier,
)
```

**Design note:** this only matters when the caller doesn't already constrain
the size (e.g. a fixed `.height()`) — callers that do their own sizing (like
the collapsing header above) simply don't pass `placeholderAspectRatio`.

## 6. A Pinterest-style masonry grid

**Problem:** TheMealDB's thumbnails come in different aspect ratios. A
regular `LazyVerticalGrid` forces every cell to the same height, so most
images get cropped or letterboxed to fit a uniform box — a fixed grid fights
the images instead of showing them as they are.

**Solution:** `LazyVerticalStaggeredGrid`, with cards that keep the image's
own aspect ratio instead of a fixed one — the same masonry look as
Pinterest's feed, where taller photos simply take a taller cell:

```kotlin
LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Fixed(2),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = contentTopPadding, bottom = 16.dp),
    verticalItemSpacing = 16.dp,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
) {
    items(state.meals, key = { it.id }) { meal ->
        LBThumbnailCard(title = meal.name, imageUrl = meal.thumbnailUrl, /* ... */)
    }
}
```

The masonry effect isn't in the grid at all — it falls entirely out of
`LBThumbnailCard` never forcing an aspect ratio on its image:

```kotlin
LBRemoteImage(
    url = imageUrl,
    modifier = imageModifier.fillMaxWidth(),
    contentScale = ContentScale.FillWidth,
    // Square placeholder while loading; the card then adjusts to the
    // image's real ratio instead of growing from zero height.
    placeholderAspectRatio = 1f,
)
```

Because `LBRemoteImage` already reserves `placeholderAspectRatio` and swaps
in the real ratio once Coil reports the image's intrinsic size (see §5
above), each card in the grid settles into its own natural height with no
layout jump — the staggered grid and the stable-image trick are the same
mechanism solving two problems at once: no forced uniform aspect ratio, and
no empty-space flash while loading.

**Gotcha:** `StaggeredGridCells.Fixed(2)` still needs `key = { it.id }` on
`items` — without a stable key, item reuse across a staggered layout can
mismatch heights on recomposition (e.g. after a search filters the list),
causing cards to visibly jump.

## 7. Pull-to-refresh that never hijacks the screen

**Problem:** the obvious first implementation is one `isLoading` boolean that
turns true whenever a sync is in flight. That breaks pull-to-refresh in a
very visible way: the user pulls down expecting the small spinner gesture
they just did, and instead the whole screen swaps its content for a
full-screen loading state — the list they were looking at disappears for no
reason, since it's still perfectly valid cached data.

**Solution:** two separate flags in state, each answering a different
question, so a refresh in flight and "there is genuinely nothing to show yet"
never collapse into the same signal:

```kotlin
data class CategoriesState(
    val categories: List<Category> = emptyList(),
    // Nothing stored while the first emission or a sync is pending. An
    // empty list by itself never means loading.
    val isLoading: Boolean = true,
    val hasSyncFailed: Boolean = false,
    // A sync the user asked for is running — drives the pull-to-refresh
    // indicator only.
    val isRefreshing: Boolean = false,
)
```

```kotlin
// screen content: isLoading only ever short-circuits to the full-screen
// spinner when there is nothing else to render
when {
    state.isLoading -> LBLoading()
    state.categories.isEmpty() && ... -> LBEmptyState(...)
    else -> LazyVerticalStaggeredGrid(...) { /* the real content */ }
}
```

```kotlin
// the pull gesture itself only ever toggles isRefreshing
PullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = { onEvent(CategoriesEvent.OnRefresh) },
) { CategoriesContent(state, onEvent, ...) }
```

Since `categories` already holds the cached list, `state.isLoading` is
`false` the moment there's anything to show — so pulling to refresh on a
populated screen always renders the grid underneath the pull gesture's own
spinner, never `LBLoading()`. The full write-up of *why* `isLoading` can make
that guarantee (it only looks at the sync status when the database is
already empty) is in [docs/offline-first.md](offline-first.md#combine-merging-the-read-path-the-sync-status-and-local-ui-flags).

**Second detail, purely visual:** the indicator itself needs to be told where
to sit. `PullToRefreshBox`'s default indicator is top-aligned to the box's own
bounds — on these screens that's *behind* the frosted top bar and the
floating search bar, so left alone it renders half-hidden under them:

```kotlin
PullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = { onEvent(CategoriesEvent.OnRefresh) },
    state = pullState,
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
) { /* ... */ }
```

**Third detail:** the automatic sync fired from the ViewModel's `init` (every
time the screen is first opened) must never touch `isRefreshing` — only a
sync started from `OnRefresh` (the pull gesture, or a Retry button) does. See
[docs/offline-first.md](offline-first.md#sync-on-init-fetch-without-ever-blocking-the-read-path)
for the `userInitiated` flag that keeps these apart. Without it, opening any
screen would flash the pull-to-refresh spinner on its own, which reads as the
UI refreshing itself unprompted — a small thing, but it's exactly the kind of
detail that makes an app feel handmade instead of assembled.
