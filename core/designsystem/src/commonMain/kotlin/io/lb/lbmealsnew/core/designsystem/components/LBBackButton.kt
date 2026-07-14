package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.lb.lbmealsnew.core.designsystem.resources.Res
import io.lb.lbmealsnew.core.designsystem.resources.ic_back_arrow
import org.jetbrains.compose.resources.painterResource

/**
 * Top bar back navigation button with the LB.io arrow, tinted with the
 * current content color so it adapts to the theme.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier Optional [Modifier].
 */
@Composable
fun LBBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_back_arrow),
            contentDescription = "Back",
            modifier = Modifier.size(24.dp),
        )
    }
}
