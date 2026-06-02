package com.abyxcz.composeforms.migration

import com.abyxcz.composeforms.persistence.SemanticTransform
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

/**
 * A DECLARATIVE migration the LLM is asked to produce — never executable code. Trusted code
 * in [toTransform] interprets it, so a malformed or malicious model response can at worst
 * produce a bad mapping (caught by validation), never run arbitrary logic on device.
 *
 * @property renames   oldFieldName -> newFieldName (value carried across)
 * @property drops     field names to omit from the migrated object
 * @property constants newFieldName -> constant JSON value (backfill for new/!derivable fields)
 */
@Serializable
data class TransformSpec(
    val renames: Map<String, String> = emptyMap(),
    val drops: List<String> = emptyList(),
    val constants: Map<String, JsonElement> = emptyMap(),
) {
    /** Interpret this spec into a [SemanticTransform] applied to the stored JSON object. */
    fun toTransform(): SemanticTransform =
        SemanticTransform { old: JsonObject ->
            val dropped = drops.toSet()
            val renamedAway = renames.keys
            buildJsonObject {
                // Carry over every untouched field.
                old.forEach { (key, value) ->
                    if (key !in dropped && key !in renamedAway) put(key, value)
                }
                // Apply renames (only when the source field is present).
                renames.forEach { (from, to) -> old[from]?.let { put(to, it) } }
                // Backfill constants (these win over any carried-over value).
                constants.forEach { (key, value) -> put(key, value) }
            }
        }
}
