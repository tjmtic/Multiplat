package com.abyxcz.multiplat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abyxcz.composeforms.ui.v2.FormSubmitButton
import com.abyxcz.composeforms.ui.v2.RenderForm
import com.abyxcz.composeforms.ui.v2.rememberFormContext
import com.abyxcz.multiplat.demo.DemoEnvironment
import com.abyxcz.multiplat.demo.MigrationDemoScreen
import com.abyxcz.v2core.core.model.form
import org.jetbrains.compose.ui.tooling.preview.Preview

private enum class Screen(val title: String) {
    Examples("Form Examples"),
    OtaDemo("OTA Migration Demo"),
}

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App(env: DemoEnvironment = remember { DemoEnvironment() }) {
    MaterialTheme {
        var screen by remember { mutableStateOf(Screen.Examples) }

        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Screen.entries.forEach { target ->
                    if (target == screen) {
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors()) { Text(target.title) }
                    } else {
                        OutlinedButton(onClick = { screen = target }) { Text(target.title) }
                    }
                }
            }

            when (screen) {
                Screen.Examples -> ExamplesScreen()
                Screen.OtaDemo -> MigrationDemoScreen(env)
            }
        }
    }
}

/** The original DSL example: a validated login form. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun ExamplesScreen() {
    val loginForm =
        remember {
            form {
                section("Login") {
                    text("username") {
                        label = "Username"
                        required("Username is required")
                    }
                    password("password") {
                        label = "Password"
                        required("Password is required")
                    }
                }
                submitButton("Login")
            }
        }

    val context = rememberFormContext(loginForm)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        RenderForm(form = loginForm, context = context)
        FormSubmitButton("Login", loginForm, context) { result ->
            println("Form submitted: $result")
        }
    }
}
