package com.abyxcz.composeforms.migration

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device [LlmEngine] backed by MediaPipe LLM Inference (e.g. a quantized Gemma `.task` model).
 *
 * The model file must be provisioned onto the device separately (bundled asset copied to internal
 * storage, or downloaded) and its absolute path passed as [modelPath]. Inference is synchronous in
 * MediaPipe, so it is dispatched to [Dispatchers.IO]. Call [close] when done to release native
 * resources.
 */
class MediaPipeLlmEngine(
    context: Context,
    modelPath: String,
    maxTokens: Int = 1024,
) : LlmEngine, AutoCloseable {
    private val inference: LlmInference =
        LlmInference.createFromOptions(
            context,
            LlmInference.LlmInferenceOptions
                .builder()
                .setModelPath(modelPath)
                .setMaxTokens(maxTokens)
                .build(),
        )

    override suspend fun generate(prompt: String): String =
        withContext(Dispatchers.IO) {
            inference.generateResponse(prompt)
        }

    override fun close() {
        inference.close()
    }
}
