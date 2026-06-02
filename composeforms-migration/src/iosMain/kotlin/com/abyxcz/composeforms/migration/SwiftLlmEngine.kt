package com.abyxcz.composeforms.migration

import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * iOS [LlmEngine] that bridges to a Swift-supplied generator.
 *
 * On-device model APIs on iOS — Apple **Foundation Models**, **MLX**, or a **llama.cpp** wrapper —
 * are Swift/C++ with no Kotlin-callable surface, so the model is owned by the Swift app and invoked
 * here through a completion-handler closure. Kotlin's `suspend` is bridged to that callback, so the
 * migration pipeline stays identical to Android's.
 *
 * Swift wiring (Foundation Models example):
 * ```swift
 * MainViewControllerKt.MainViewController { prompt, onResult, onError in
 *     Task {
 *         do {
 *             let session = LanguageModelSession()
 *             let reply = try await session.respond(to: prompt)
 *             onResult(reply.content)
 *         } catch { onError(KotlinThrowable(message: "\(error)")) }
 *     }
 * }
 * ```
 *
 * @param generator called with the prompt plus success/failure callbacks; invoke exactly one.
 */
class SwiftLlmEngine(
    private val generator: (prompt: String, onResult: (String) -> Unit, onError: (Throwable) -> Unit) -> Unit,
) : LlmEngine {
    override suspend fun generate(prompt: String): String =
        suspendCancellableCoroutine { continuation ->
            generator(
                prompt,
                { result -> continuation.resumeWith(Result.success(result)) },
                { error -> continuation.resumeWith(Result.failure(error)) },
            )
        }
}
