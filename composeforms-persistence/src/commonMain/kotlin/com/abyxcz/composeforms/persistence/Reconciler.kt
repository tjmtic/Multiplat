package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.FormSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Shared read path for every [FormStore]: turn a stored JSON envelope into a values map
 * reconciled to the current [schema].
 *
 *  1. If the stored [storedVersion] equals the current schema fingerprint, decode directly.
 *  2. Otherwise apply a registered [SemanticTransform] for that version pair if one exists
 *     (renames/splits/coercions), then decode.
 *  3. With no transform, [FormValuesCodec.decode] still reconciles structurally
 *     (added field → default, removed field → dropped).
 */
internal object Reconciler {
    private val parser = Json { ignoreUnknownKeys = true }

    fun reconcile(
        transforms: TransformRegistry,
        storedVersion: String,
        json: String,
        schema: FormSchema,
    ): Map<String, Any?> {
        val stored = parser.parseToJsonElement(json).jsonObject
        val target = schema.fingerprint()
        val migrated =
            if (storedVersion == target) {
                stored
            } else {
                transforms.find(storedVersion, target)?.apply(stored) ?: stored
            }
        return FormValuesCodec.decode(schema, migrated)
    }
}
