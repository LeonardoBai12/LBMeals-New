package io.lb.lbmealsnew.core.database.sqldelight.factory

import app.cash.sqldelight.db.SqlDriver

/**
 * Creates the platform-specific SQLite driver behind [io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase].
 */
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
