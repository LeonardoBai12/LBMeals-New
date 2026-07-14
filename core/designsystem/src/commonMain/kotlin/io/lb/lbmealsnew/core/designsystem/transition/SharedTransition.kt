package io.lb.lbmealsnew.core.designsystem.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * The app-wide [SharedTransitionScope], provided by the root navigation
 * host. Null when the composition is not inside a shared transition layout
 * (previews, tests) — the helpers below become no-ops then.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * The [AnimatedVisibilityScope] of the current navigation destination,
 * provided by each screen's `composable` builder.
 */
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks this element as shared between navigation destinations: elements
 * with the same [key] morph position and size into each other during the
 * transition. No-op outside a shared transition layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
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

/**
 * Container-transform variant of [lbSharedElement]: the two elements keep
 * their own content and only their bounds morph — the Material pattern for
 * a card expanding into the screen it opens. No-op outside a shared
 * transition layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.lbSharedBounds(key: Any): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this

    return with(sharedTransitionScope) {
        this@lbSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
