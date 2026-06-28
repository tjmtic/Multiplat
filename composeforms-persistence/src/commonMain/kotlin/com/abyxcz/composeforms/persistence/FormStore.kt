package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.FormSchema

/** Stable identity for a stored form-object instance: a [type] (object kind) plus an [id]. */
data class ObjectKey(
    val type: String,
    val id: String,
)

/**
 * Persistence port for dynamically-structured form objects.
 *
 * Reads REQUIRE the current [FormSchema] because the schema is what supplies field
 * types and defaults. Values are reconciled and type-coerced to the live structure on
 * the way out, which is what makes backend-driven (over-the-air) structural changes
 * free for additive / removal / reorder edits — no migration step required.
 *
 * The only changes a [FormStore] cannot infer on its own are the *ambiguous* ones
 * (renames, splits/merges, type coercions, backfills); those are supplied out-of-band
 * via a [TransformRegistry].
 */
interface FormStore {
    /** Persist [values] (a `FormState` snapshot) for [key], stamped with [schema]'s fingerprint. */
    suspend fun put(
        key: ObjectKey,
        schema: FormSchema,
        values: Map<String, Any?>,
    )

    /** Load the object for [key], reconciled + coerced to [schema], or null if absent. */
    suspend fun get(
        key: ObjectKey,
        schema: FormSchema,
    ): Map<String, Any?>?

    /** Load every object of [type], each reconciled + coerced to [schema]. */
    suspend fun getByType(
        type: String,
        schema: FormSchema,
    ): List<Pair<ObjectKey, Map<String, Any?>>>

    /** Remove the object for [key]. No-op if absent. */
    suspend fun delete(key: ObjectKey)
}
