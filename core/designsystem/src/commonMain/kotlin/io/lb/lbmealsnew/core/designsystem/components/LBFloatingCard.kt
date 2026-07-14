package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Card for cut-out artwork: transparent container, so the image floats
 * directly on the screen background, with a centered title underneath.
 * Images that do carry their own background get rounded corners — invisible
 * on transparent cut-outs, and it turns photos into tiles.
 *
 * @param title Card title, centered under the image.
 * @param imageUrl Remote image URL.
 * @param onClick Called when the card is clicked.
 * @param modifier Optional [Modifier].
 * @param imageModifier Optional [Modifier] for the image — the hook for
 * shared element transitions.
 */
@Composable
fun LBFloatingCard(
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            LBRemoteImage(
                url = imageUrl,
                contentDescription = title,
                modifier = imageModifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large),
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
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
