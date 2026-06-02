package com.abyxcz.composeforms.persistence

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.abyxcz.composeforms.persistence.db.FormStoreDatabase

actual class DriverFactory(
    private val context: Context,
    private val name: String = "formstore.db",
) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(FormStoreDatabase.Schema, context, name)
}
