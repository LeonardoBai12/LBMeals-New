package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Remote image loaded with Coil, with a crossfade. Single place in the app
 * that knows which image loader is used.
 *
 * @param url Remote image URL.
 * @param contentDescription Accessibility description.
 * @param modifier Optional [Modifier].
 * @param contentScale How the image fills its bounds.
 * @param placeholderAspectRatio When set, the composable reserves space at
 * this width/height ratio while the image loads, then adjusts to the image's
 * real ratio once it arrives — the layout never collapses to zero height and
 * never grows in a single jump. Leave null when the caller constrains the
 * size itself.
 */
@Composable
fun LBRemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderAspectRatio: Float? = null,
) {
    var imageAspectRatio by remember(url) { mutableFloatStateOf(0f) }
    val reservedRatio = when {
        placeholderAspectRatio == null -> null
        imageAspectRatio > 0f -> imageAspectRatio
        else -> placeholderAspectRatio
    }

    AsyncImage(
        model = ImageRequest.Builder(coil3.compose.LocalPlatformContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        onState = { state ->
            if (state is AsyncImagePainter.State.Success) {
                val size = state.painter.intrinsicSize
                if (size.width > 0f && size.height > 0f) {
                    imageAspectRatio = size.width / size.height
                }
            }
        },
        contentDescription = contentDescription,
        modifier = if (reservedRatio != null) modifier.aspectRatio(reservedRatio) else modifier,
        contentScale = contentScale,
    )
}
