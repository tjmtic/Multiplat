package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.FormSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Reference [FormStore] that keeps the serialized envelope in memory.
 *
 * It stores exactly what the SQLDelight adapter will persist — `(type, id, schemaVersion,
 * json, updatedAt)` — so its read/reconcile behavior is identical. That makes it the
 * canonical implementation for tests, previews, and ephemeral caches. Not thread-safe.
 */
class InMemoryFormStore(
    private val transforms: TransformRegistry = TransformRegistry.Empty,
) : FormStore {
    private data class Row(
        val schemaVersion: String,
        val json: String,
        val updatedAt: Long,
    )

    private val rows = mutableMapOf<ObjectKey, Row>()
    private val parser = Json { ignoreUnknownKeys = true }

    override suspend fun put(
        key: ObjectKey,
        schema: FormSchema,
        values: Map<String, Any?>,
    ) {
        rows[key] = Row(schema.fingerprint(), FormValuesCodec.encode(schema, values), nowMillis())
    }

    override suspend fun get(
        key: ObjectKey,
        schema: FormSchema,
    ): Map<String, Any?>? = rows[key]?.let { reconcile(it, schema) }

    override suspend fun getByType(
        type: String,
        schema: FormSchema,
    ): List<Pair<ObjectKey, Map<String, Any?>>> =
        rows
            .filterKeys { it.type == type }
            .map { (key, row) -> key to reconcile(row, schema) }

    override suspend fun delete(key: ObjectKey) {
        rows.remove(key)
    }

    private fun reconcile(
        row: Row,
        schema: FormSchema,
    ): Map<String, Any?> {
        val stored: JsonObject = parser.parseToJsonElement(row.json).jsonObject
        val target = schema.fingerprint()
        val migrated =
            if (row.schemaVersion == target) {
                stored
            } else {
                transforms.find(row.schemaVersion, target)?.apply(stored) ?: stored
            }
        return FormValuesCodec.decode(schema, migrated)
    }
}
