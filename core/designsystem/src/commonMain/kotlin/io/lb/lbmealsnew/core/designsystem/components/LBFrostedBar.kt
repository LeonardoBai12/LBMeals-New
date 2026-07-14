package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

/**
 * Style of the app's frosted-glass top bars: a translucent tint of the
 * theme's surface over a backdrop blur of whatever scrolls behind the bar.
 *
 * Usage: the screen keeps a `HazeState`, marks its scrolling content with
 * `Modifier.hazeSource(state)` and applies
 * `Modifier.hazeEffect(state, lbFrostedBarStyle())` to a top bar with a
 * transparent container color.
 */
@Composable
fun lbFrostedBarStyle(): HazeStyle = HazeStyle(
    backgroundColor = MaterialTheme.colorScheme.surface,
    tints = listOf(HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = BAR_TINT_ALPHA))),
    blurRadius = 20.dp,
)

private const val BAR_TINT_ALPHA = 0.78f
