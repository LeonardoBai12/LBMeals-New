package io.lb.lbmealsnew.core.designsystem.scroll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * Tracks the scroll direction of a nested scrollable: scrolling down the
 * content hides, scrolling back up shows. Drive an `AnimatedVisibility`
 * with [isVisible] and attach [nestedScrollConnection] to any parent of
 * the scrollable.
 */
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

/**
 * Remembers a [HideOnScrollState] across recompositions.
 */
@Composable
fun rememberHideOnScrollState(): HideOnScrollState = remember { HideOnScrollState() }

private const val SCROLL_DEADZONE_PX = 4f
