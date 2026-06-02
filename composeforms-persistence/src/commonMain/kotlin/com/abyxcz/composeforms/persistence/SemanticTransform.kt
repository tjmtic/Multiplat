package com.abyxcz.composeforms.persistence

import kotlinx.serialization.json.JsonObject

/**
 * An optional migration for the *ambiguous* structural changes that a pure reconcile
 * cannot infer: renames, field splits/merges, type coercions, and backfills.
 *
 * It transforms the stored JSON (written against some older fingerprint) into JSON valid
 * for the current structure, BEFORE [FormValuesCodec.decode] reconciles it.
 *
 * This is the seam the on-device LLM ("dbmigration") plugs into: it generates and caches a
 * transform per `(fromVersion -> toVersion)` fingerprint pair, validated out-of-band against
 * the target structure. When no transform is registered, the store falls back to a pure
 * structural reconcile.
 */
fun interface SemanticTransform {
    fun apply(old: JsonObject): JsonObject
}

/** Supplies [SemanticTransform]s keyed by a `(fromVersion -> toVersion)` fingerprint pair. */
interface TransformRegistry {
    /** @return a transform from [fromVersion] to [toVersion], or null to use a pure reconcile. */
    fun find(
        fromVersion: String,
        toVersion: String,
    ): SemanticTransform?

    /** No semantic transforms: every structural change is handled by pure reconcile. */
    object Empty : TransformRegistry {
        override fun find(
            fromVersion: String,
            toVersion: String,
        ): SemanticTransform? = null
    }
}
