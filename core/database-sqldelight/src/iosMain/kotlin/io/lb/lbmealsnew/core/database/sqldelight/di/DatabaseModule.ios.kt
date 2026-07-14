package io.lb.lbmealsnew.core.database.sqldelight.di

import io.lb.lbmealsnew.core.database.sqldelight.factory.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single { DatabaseDriverFactory() }
}
