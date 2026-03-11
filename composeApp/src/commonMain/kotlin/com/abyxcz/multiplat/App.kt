package com.abyxcz.multiplat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.abyxcz.composeforms.ui.v2.FormSubmitButton
import com.abyxcz.composeforms.ui.v2.RenderForm
import com.abyxcz.composeforms.ui.v2.rememberFormContext
import com.abyxcz.v2core.core.model.form
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val loginForm = remember {
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

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            RenderForm(
                form = loginForm,
                context = context,
            )
            FormSubmitButton("Login", loginForm, context) { result ->
                println("Form submitted: $result")
            }
        }
    }
}