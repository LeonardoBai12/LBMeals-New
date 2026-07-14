package io.lb.lbmealsnew.core.database.sqldelight.factory

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun create(): SqlDriver {
        return AndroidSqliteDriver(
            LBMealsDatabase.Schema,
            context,
            "lbmeals.db",
        )
    }
}
