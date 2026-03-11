package com.abyxcz.composeforms.ui.v2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abyxcz.v2core.core.model.FormContext
import com.abyxcz.v2core.core.model.FormSchema
import com.abyxcz.v2core.core.model.validateForm

@Composable
@Suppress("ktlint:standard:function-naming")
fun RenderForm(
    form: FormSchema,
    context: FormContext = rememberFormContext(form),
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        form.sections.forEach { section ->
            Text(text = section.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            section.fields.forEach { field ->
                if (field.isVisible(context.values)) {
                    FieldRenderer(field, context)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun rememberFormContext(form: FormSchema): FormContext =
    remember {
        FormContext(
            values =
                mutableStateOf(
                    form.sections.flatMap { it.fields }.associate { it.name to it.value },
                ),
        )
    }

@Composable
@Suppress("ktlint:standard:function-naming")
fun FormSubmitButton(
    label: String,
    form: FormSchema,
    context: FormContext,
    onSubmit: (Map<String, Any?>) -> Unit,
) {
    Button(onClick = {
        val isValid = validateForm(form, context)
        if (isValid) {
            onSubmit(context.values.value)
        }
    }) {
        Text(label)
    }
}
