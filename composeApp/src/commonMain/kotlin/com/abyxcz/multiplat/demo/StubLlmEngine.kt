package com.abyxcz.multiplat.demo

import com.abyxcz.composeforms.migration.LlmEngine

/**
 * Stand-in for an on-device model so the demo runs without a model file. A real app passes a
 * `MediaPipeLlmEngine` (Android) here instead. This returns the declarative [TransformSpec] JSON the
 * model would be expected to produce for the sample v2 → v3 rename; the migration pipeline still
 * parses, validates, and caches it exactly as it would a real response.
 */
class StubLlmEngine : LlmEngine {
    override suspend fun generate(prompt: String): String = """{"renames":{"phone_number":"phone"}}"""
}
