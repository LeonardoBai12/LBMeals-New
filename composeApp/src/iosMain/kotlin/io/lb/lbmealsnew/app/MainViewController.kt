package io.lb.lbmealsnew.app

import androidx.compose.ui.window.ComposeUIViewController
import io.lb.lbmealsnew.app.di.appModules
import io.lb.lbmealsnew.core.database.sqldelight.di.platformDatabaseModule
import org.koin.core.context.startKoin

/**
 * iOS entry point: starts the Koin container and renders the shared UI.
 */
@Suppress("FunctionNaming", "unused")
fun MainViewController() = ComposeUIViewController {
    startKoin {
        modules(appModules + platformDatabaseModule)
    }
    LBMealsApp()
}
