package com.abyxcz.multiplat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abyxcz.composeforms.migration.CachingTransformRegistry
import com.abyxcz.composeforms.migration.LlmMigrator
import com.abyxcz.composeforms.persistence.DriverFactory
import com.abyxcz.composeforms.persistence.createFormStore
import com.abyxcz.multiplat.demo.DemoEnvironment
import com.abyxcz.multiplat.demo.StubLlmEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // SQLDelight-backed store so saved data persists across app launches.
        // Swap StubLlmEngine for MediaPipeLlmEngine(this, modelPath) to run a real on-device model.
        val registry = CachingTransformRegistry()
        val env =
            DemoEnvironment(
                registry = registry,
                store = createFormStore(DriverFactory(applicationContext), registry),
                migrator = LlmMigrator(StubLlmEngine()),
            )

        setContent {
            App(env)
        }
    }
}

@Preview
@Composable
@Suppress("ktlint:standard:function-naming")
fun AppAndroidPreview() {
    App()
}
