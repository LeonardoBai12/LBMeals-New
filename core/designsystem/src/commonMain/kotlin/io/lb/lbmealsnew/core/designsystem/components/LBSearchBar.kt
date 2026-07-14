package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Standard search bar: leading search icon, trailing clear button when the
 * query is not empty, single line. The field's container is slightly
 * translucent, so it floats over scrolling content without fully hiding it.
 *
 * @param query Current query text.
 * @param onQueryChange Called on every text change; clearing calls it with "".
 * @param placeholder Hint shown while the query is empty.
 * @param modifier Optional [Modifier].
 */
@Composable
fun LBSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    // A tonal step above the background, so the field reads as a field on
    // plain surfaces too — light gray on light theme, dark gray on dark.
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = CONTAINER_ALPHA)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        // Borderless, filled and rounded — the iOS look.
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
    )
}

private const val CONTAINER_ALPHA = 0.95f
