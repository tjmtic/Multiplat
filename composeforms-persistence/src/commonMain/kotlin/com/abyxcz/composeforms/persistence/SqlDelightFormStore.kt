package com.abyxcz.composeforms.persistence

import com.abyxcz.composeforms.persistence.db.FormStoreDatabase
import com.abyxcz.v2core.core.model.FormSchema

/**
 * SQLDelight-backed [FormStore]. Persists the same `(type, id, schemaVersion, json, updatedAt)`
 * envelope as [InMemoryFormStore] and shares the [Reconciler] read path, so behavior is
 * identical across the two. Construct via [createFormStore] with a [DriverFactory].
 */
class SqlDelightFormStore(
    private val db: FormStoreDatabase,
    private val transforms: TransformRegistry = TransformRegistry.Empty,
) : FormStore {
    private val queries get() = db.formObjectQueries

    override suspend fun put(
        key: ObjectKey,
        schema: FormSchema,
        values: Map<String, Any?>,
    ) {
        queries.upsert(
            type = key.type,
            id = key.id,
            schema_version = schema.fingerprint(),
            json = FormValuesCodec.encode(schema, values),
            updated_at = nowMillis(),
        )
    }

    override suspend fun get(
        key: ObjectKey,
        schema: FormSchema,
    ): Map<String, Any?>? =
        queries.selectOne(key.type, key.id).executeAsOneOrNull()?.let { row ->
            Reconciler.reconcile(transforms, row.schema_version, row.json, schema)
        }

    override suspend fun getByType(
        type: String,
        schema: FormSchema,
    ): List<Pair<ObjectKey, Map<String, Any?>>> =
        queries.selectByType(type).executeAsList().map { row ->
            ObjectKey(row.type, row.id) to Reconciler.reconcile(transforms, row.schema_version, row.json, schema)
        }

    override suspend fun delete(key: ObjectKey) {
        queries.deleteOne(key.type, key.id)
    }
}
