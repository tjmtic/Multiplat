package com.abyxcz.composeforms.migration

import com.abyxcz.composeforms.persistence.FormValuesCodec
import com.abyxcz.composeforms.persistence.SemanticTransform
import com.abyxcz.v2core.core.model.FormSchema
import kotlinx.serialization.json.JsonObject

/**
 * The safety gate before a candidate [SemanticTransform] is cached and allowed to touch user data.
 * It runs entirely in a sandbox (plain in-memory objects), never against the live database.
 *
 * A candidate is accepted only if, for every [samples] entry:
 *  - the migrated object decodes against [to] without throwing, AND
 *  - every field common to both schemas (by name) is PRESERVED unchanged — i.e. the transform
 *    must not drop or rewrite data that clearly carries forward. (A legitimately-null value is
 *    preserved as null; the test is equality with the source, not non-nullness.)
 *
 * Inherent limit: a field renamed across all of its identity (present in neither schema's
 * intersection) is indistinguishable from a drop+add, so a wrong rename of that kind cannot be
 * rejected here — the model's choice is trusted for those. Common-field loss and decode failures
 * are what this catches.
 */
class MigrationValidator {
    fun validate(
        transform: SemanticTransform,
        from: FormSchema,
        to: FormSchema,
        samples: List<JsonObject>,
    ): Boolean {
        if (samples.isEmpty()) return false // nothing to prove correctness against
        val mustSurvive = from.shapes().map { it.name }.toSet() intersect to.shapes().map { it.name }.toSet()
        return samples.all { sample ->
            runCatching {
                val migrated = transform.apply(sample)
                FormValuesCodec.decode(to, migrated) // must not throw on the target shape
                mustSurvive.all { name -> migrated[name] == sample[name] }
            }.getOrDefault(false)
        }
    }
}
