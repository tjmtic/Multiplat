package com.abyxcz.composeforms.migration

/**
 * Minimal text-in / text-out contract for an on-device language model. Platform engines
 * (Android: MediaPipe; iOS: TODO) implement this; the migration pipeline depends only on it,
 * so the logic is fully testable with a fake engine.
 */
fun interface LlmEngine {
    suspend fun generate(prompt: String): String
}
