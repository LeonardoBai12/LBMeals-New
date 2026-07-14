package io.lb.lbmealsnew.app

import androidx.compose.runtime.Composable
import io.lb.lbmealsnew.app.navigation.AppNavHost
import io.lb.lbmealsnew.core.designsystem.theme.LBMealsTheme

/**
 * Root of the shared UI: the theme plus the navigation graph. Every platform
 * entry point renders this composable.
 */
@Composable
fun LBMealsApp() {
    LBMealsTheme {
        AppNavHost()
    }
}
