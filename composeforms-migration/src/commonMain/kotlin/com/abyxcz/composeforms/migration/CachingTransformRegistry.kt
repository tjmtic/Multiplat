package com.abyxcz.composeforms.migration

import com.abyxcz.composeforms.persistence.SemanticTransform
import com.abyxcz.composeforms.persistence.TransformRegistry

/**
 * A [TransformRegistry] whose entries are validated transforms keyed by `(fromFingerprint ->
 * toFingerprint)`. [find] is a pure synchronous lookup, so the store's read path never blocks on
 * the LLM — generation happens earlier via [LlmMigrator.prepareInto].
 */
class CachingTransformRegistry : TransformRegistry {
    private val cache = mutableMapOf<Pair<String, String>, SemanticTransform>()

    fun register(
        fromVersion: String,
        toVersion: String,
        transform: SemanticTransform,
    ) {
        cache[fromVersion to toVersion] = transform
    }

    fun has(
        fromVersion: String,
        toVersion: String,
    ): Boolean = cache.containsKey(fromVersion to toVersion)

    override fun find(
        fromVersion: String,
        toVersion: String,
    ): SemanticTransform? = cache[fromVersion to toVersion]
}
