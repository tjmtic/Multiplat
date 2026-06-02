package com.abyxcz.composeforms.persistence

import app.cash.sqldelight.db.SqlDriver
import com.abyxcz.composeforms.persistence.db.FormStoreDatabase

/**
 * Platform entry point for creating the SQLite [SqlDriver]. The Android/iOS drivers apply
 * (and migrate) [FormStoreDatabase.Schema] themselves on open, so callers never run schema
 * creation manually.
 *
 * Android: `DriverFactory(context)`. iOS: `DriverFactory()`.
 */
expect class DriverFactory {
    fun createDriver(): SqlDriver
}

/** Convenience: build a SQLDelight-backed [FormStore] from a [DriverFactory]. */
fun createFormStore(
    factory: DriverFactory,
    transforms: TransformRegistry = TransformRegistry.Empty,
): FormStore = SqlDelightFormStore(FormStoreDatabase(factory.createDriver()), transforms)
