package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Full-size empty state with a message and an optional action button.
 *
 * @param message Message explaining why there is nothing to show.
 * @param modifier Optional [Modifier].
 * @param actionLabel Label of the action button; no button when null.
 * @param onActionClick Called when the action button is clicked.
 */
@Composable
fun LBEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        actionLabel?.let {
            Button(
                onClick = onActionClick,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(it)
            }
        }
    }
}
