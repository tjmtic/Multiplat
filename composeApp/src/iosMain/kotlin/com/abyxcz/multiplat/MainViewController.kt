package com.abyxcz.multiplat

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.abyxcz.composeforms.migration.CachingTransformRegistry
import com.abyxcz.composeforms.migration.LlmMigrator
import com.abyxcz.composeforms.persistence.DriverFactory
import com.abyxcz.composeforms.persistence.createFormStore
import com.abyxcz.multiplat.demo.DemoEnvironment
import com.abyxcz.multiplat.demo.StubLlmEngine

@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    ComposeUIViewController {
        // SQLDelight (NativeSqliteDriver) backed store. iOS still uses StubLlmEngine until a
        // concrete iOS LlmEngine (llama.cpp / Foundation Models) is wired up.
        val env =
            remember {
                val registry = CachingTransformRegistry()
                DemoEnvironment(
                    registry = registry,
                    store = createFormStore(DriverFactory(), registry),
                    migrator = LlmMigrator(StubLlmEngine()),
                )
            }
        App(env)
    }
