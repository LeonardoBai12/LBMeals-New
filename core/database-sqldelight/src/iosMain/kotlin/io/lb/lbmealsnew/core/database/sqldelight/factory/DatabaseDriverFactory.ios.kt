package io.lb.lbmealsnew.core.database.sqldelight.factory

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.lb.lbmealsnew.core.database.sqldelight.LBMealsDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        return NativeSqliteDriver(
            LBMealsDatabase.Schema,
            "lbmeals.db",
        )
    }
}
