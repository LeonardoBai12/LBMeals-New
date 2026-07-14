package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.lb.lbmealsnew.core.designsystem.resources.Res
import io.lb.lbmealsnew.core.designsystem.resources.ic_lbio_meals
import org.jetbrains.compose.resources.painterResource

/**
 * The LB.io Meals wordmark. Resolves the light or dark variant through the
 * `drawable`/`drawable-dark` resource qualifiers, following the system theme just
 * like the color scheme does.
 *
 * @param modifier Optional [Modifier]; constrain the height and the width
 * follows the wordmark's aspect ratio.
 */
@Composable
fun LBLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.ic_lbio_meals),
        contentDescription = "LB Meals",
        modifier = modifier.aspectRatio(LOGO_ASPECT_RATIO),
    )
}

private const val LOGO_ASPECT_RATIO = 800f / 300f
