package io.lb.lbmealsnew.app

import androidx.compose.ui.window.ComposeUIViewController
import io.lb.lbmealsnew.app.di.appModules
import io.lb.lbmealsnew.core.database.sqldelight.di.platformDatabaseModule
import org.koin.core.context.startKoin

private var isKoinStarted = false

/**
 * iOS entry point: starts the Koin container and renders the shared UI.
 *
 * Koin starts here, outside the composable content, and only once: the
 * content lambda passed to [ComposeUIViewController] recomposes on system
 * events such as a light/dark appearance change, and starting Koin twice
 * throws. This mirrors the Android entry point, where Koin starts once in
 * `Application.onCreate`, never inside Compose.
 */
@Suppress("FunctionNaming", "unused")
fun MainViewController() = run {
    if (!isKoinStarted) {
        startKoin {
            modules(appModules + platformDatabaseModule)
        }
        isKoinStarted = true
    }

    ComposeUIViewController {
        LBMealsApp()
    }
}
