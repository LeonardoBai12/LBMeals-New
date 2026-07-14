package io.lb.lbmealsnew.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Color(0xFF8B5000),
    secondary = Color(0xFF755845),
    tertiary = Color(0xFF606134),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB870),
    secondary = Color(0xFFE5BFA8),
    tertiary = Color(0xFFCACA93),
)

/**
 * The platform's own color scheme when it has one — dynamic color on
 * Android 12+ (Material You) — or null to use the branded schemes.
 */
@Composable
internal expect fun platformColorScheme(darkTheme: Boolean): ColorScheme?

/**
 * App theme for LBMeals. Uses the platform color scheme when available,
 * falling back to the branded color schemes.
 */
@Composable
fun LBMealsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = platformColorScheme(darkTheme)
        ?: if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
