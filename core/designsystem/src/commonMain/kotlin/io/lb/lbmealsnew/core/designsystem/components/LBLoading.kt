package io.lb.lbmealsnew.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Full-size centered loading indicator. Only shown when there is no cached
 * content to display — a refresh over existing content uses the pull-to-refresh
 * indicator instead.
 *
 * @param modifier Optional [Modifier].
 */
@Composable
fun LBLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(TEST_TAG_LB_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Semantics tag identifying [LBLoading] in UI tests.
 */
const val TEST_TAG_LB_LOADING = "LBLoading"
