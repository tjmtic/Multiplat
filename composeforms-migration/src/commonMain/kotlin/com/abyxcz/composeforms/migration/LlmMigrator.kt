package com.abyxcz.composeforms.migration

import com.abyxcz.composeforms.persistence.SemanticTransform
import com.abyxcz.composeforms.persistence.fingerprint
import com.abyxcz.v2core.core.model.FormSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Orchestrates on-device migration generation:
 *  1. build a constrained prompt from the structural diff,
 *  2. ask the [LlmEngine] for a [TransformSpec],
 *  3. interpret it into a [SemanticTransform] and VALIDATE it against samples,
 *  4. retry up to [maxAttempts]; return null if no candidate validates (caller falls back to
 *     pure structural reconcile).
 *
 * The LLM never runs on the read hot path — call [prepareInto] when a new schema is adopted,
 * then reads use the cached, validated transform.
 */
class LlmMigrator(
    private val engine: LlmEngine,
    private val validator: MigrationValidator = MigrationValidator(),
    private val maxAttempts: Int = 3,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Generate + validate a transform from [from] to [to], or null if none validates. */
    suspend fun prepare(
        from: FormSchema,
        to: FormSchema,
        samples: List<JsonObject>,
    ): SemanticTransform? {
        if (from.fingerprint() == to.fingerprint()) return SemanticTransform { it }

        val prompt = MigrationPrompt.build(from, to)
        repeat(maxAttempts) {
            val spec = runCatching { json.decodeFromString<TransformSpec>(extractJson(engine.generate(prompt))) }
                .getOrNull()
            if (spec != null) {
                val transform = spec.toTransform()
                if (validator.validate(transform, from, to, samples)) return transform
            }
        }
        return null
    }

    /**
     * Convenience: [prepare] then register the result in [registry] under the schema fingerprints.
     * @return true if a validated transform was registered.
     */
    suspend fun prepareInto(
        registry: CachingTransformRegistry,
        from: FormSchema,
        to: FormSchema,
        samples: List<JsonObject>,
    ): Boolean {
        val transform = prepare(from, to, samples) ?: return false
        registry.register(from.fingerprint(), to.fingerprint(), transform)
        return true
    }
}

/** Pull the first balanced JSON object out of a model response (tolerates code fences / prose). */
internal fun extractJson(raw: String): String {
    val start = raw.indexOf('{')
    if (start < 0) return raw.trim()
    var depth = 0
    for (i in start until raw.length) {
        when (raw[i]) {
            '{' -> depth++
            '}' -> {
                depth--
                if (depth == 0) return raw.substring(start, i + 1)
            }
        }
    }
    return raw.substring(start)
}
