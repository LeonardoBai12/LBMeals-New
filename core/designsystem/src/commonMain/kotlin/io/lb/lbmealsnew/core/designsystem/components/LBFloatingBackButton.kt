package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.lb.lbmealsnew.core.designsystem.resources.Res
import io.lb.lbmealsnew.core.designsystem.resources.ic_back_arrow
import org.jetbrains.compose.resources.painterResource

/**
 * Back button meant to float over imagery: a low-opacity black circle with
 * the white LB.io arrow inside, readable on any picture.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier Optional [Modifier].
 */
@Composable
fun LBFloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(color = Color.Black.copy(alpha = SCRIM_ALPHA), shape = CircleShape),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_back_arrow),
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

private const val SCRIM_ALPHA = 0.35f
