package com.abyxcz.composeforms.persistence

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.abyxcz.composeforms.persistence.db.FormStoreDatabase

actual class DriverFactory(
    private val name: String = "formstore.db",
) {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(FormStoreDatabase.Schema, name)
}
