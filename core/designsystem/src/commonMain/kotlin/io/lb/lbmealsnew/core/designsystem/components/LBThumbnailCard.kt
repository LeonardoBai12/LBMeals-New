package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Card with a remote thumbnail on top and a title below — the shared building
 * block of the category and meal grids. The thumbnail keeps the image's own
 * aspect ratio, so cards vary in height and staggered grids get their
 * masonry look.
 *
 * @param title Card title.
 * @param imageUrl Remote thumbnail URL.
 * @param onClick Called when the card is clicked.
 * @param modifier Optional [Modifier].
 */
@Composable
fun LBThumbnailCard(
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            LBRemoteImage(
                url = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                // Square placeholder while loading; the card then adjusts to
                // the image's real ratio instead of growing from zero height.
                placeholderAspectRatio = 1f,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}
