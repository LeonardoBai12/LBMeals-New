package io.lb.lbmealsnew.app

import android.app.Application
import io.lb.lbmealsnew.app.di.appModules
import io.lb.lbmealsnew.core.database.sqldelight.di.platformDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Process entry point: starts the Koin container.
 */
class LBMealsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LBMealsApplication)
            modules(appModules + platformDatabaseModule)
        }
    }
}
