package io.lb.lbmealsnew.core.database.sqldelight.factory

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun create(): SqlDriver {
        // Not "lbmeals.db": installs upgraded from the Room build carry that
        // file at a higher user_version, which SQLite refuses to downgrade.
        // The database is a disposable cache, so a fresh file beats migrating.
        return AndroidSqliteDriver(
            LBMealsDatabase.Schema,
            context,
            "lbmeals_sqldelight.db",
        )
    }
}
