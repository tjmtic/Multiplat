package com.abyxcz.composeforms.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abyxcz.composeforms.model.ExperimentalFormApi
import com.abyxcz.composeforms.model.FormRunner
import com.abyxcz.composeforms.model.FormSchema
import com.abyxcz.composeforms.model.InputFieldDescriptor
import com.abyxcz.composeforms.model.InternalFormApi

@OptIn(InternalFormApi::class)
@ExperimentalFormApi
@Composable
fun <ResultType> FormSchemaRenderer(
    schema: FormSchema<ResultType>,
    fieldStates: MutableMap<String, Any?>,
    onSubmit: (ResultType, List<String>) -> Unit,
) {
    val bindingMap = remember(fieldStates) { wrapFieldStates(fieldStates) }
    val formRunner = remember { FormRunner(schema, bindingMap) }

    var fieldErrors by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var schemaErrors by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(modifier = Modifier.padding(16.dp)) {
        schema.groups.forEach { group ->
            Text(group.groupLabel, style = MaterialTheme.typography.titleMedium)

            group.fields.forEach { field ->
                @Suppress("UNCHECKED_CAST")
                RenderInputField(
                    field = field as InputFieldDescriptor<Any>,
                    fieldStates = fieldStates,
                    fieldErrors = fieldErrors[field.fieldId] ?: emptyList(),
                    onValueChange = { input -> fieldStates[field.fieldId] = input },
                )
            }
        }

        schemaErrors.forEach {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val (fErrors, sErrors) = formRunner.allErrors()
            fieldErrors = fErrors
            schemaErrors = sErrors

            if (fErrors.isEmpty() && sErrors.isEmpty()) {
                val result = formRunner.buildResult()
                onSubmit(result, emptyList())
            }
        }) {
            Text(schema.submitButtonText)
        }
    }
}
