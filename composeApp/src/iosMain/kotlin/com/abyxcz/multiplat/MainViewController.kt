package com.abyxcz.multiplat

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.abyxcz.composeforms.migration.CachingTransformRegistry
import com.abyxcz.composeforms.migration.LlmEngine
import com.abyxcz.composeforms.migration.LlmMigrator
import com.abyxcz.composeforms.migration.SwiftLlmEngine
import com.abyxcz.composeforms.persistence.DriverFactory
import com.abyxcz.composeforms.persistence.createFormStore
import com.abyxcz.multiplat.demo.DemoEnvironment
import com.abyxcz.multiplat.demo.StubLlmEngine

/** Default entry point — uses the stub engine so the demo runs without an on-device model. */
@Suppress("ktlint:standard:function-naming")
fun MainViewController() = composeApp(StubLlmEngine())

/**
 * Entry point that runs a real on-device model. The Swift app supplies [llmGenerate], a closure that
 * invokes its model (Foundation Models / MLX / llama.cpp) and calls back with the result or an error.
 * See [SwiftLlmEngine] for a Swift wiring example.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewController(
    llmGenerate: (prompt: String, onResult: (String) -> Unit, onError: (Throwable) -> Unit) -> Unit,
) = composeApp(SwiftLlmEngine(llmGenerate))

private fun composeApp(engine: LlmEngine) =
    ComposeUIViewController {
        // SQLDelight (NativeSqliteDriver) backed store; persists across launches.
        val env =
            remember {
                val registry = CachingTransformRegistry()
                DemoEnvironment(
                    registry = registry,
                    store = createFormStore(DriverFactory(), registry),
                    migrator = LlmMigrator(engine),
                )
            }
        App(env)
    }
