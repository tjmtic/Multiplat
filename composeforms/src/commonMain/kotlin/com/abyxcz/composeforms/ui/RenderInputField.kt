package com.abyxcz.composeforms.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.abyxcz.composeforms.model.CustomComposableFieldDescriptor
import com.abyxcz.composeforms.model.InputFieldDescriptor
import com.abyxcz.composeforms.model.InternalFormApi

@InternalFormApi
@Composable
@Suppress("ktlint:standard:function-naming")
fun <T : Any> RenderInputField(
    field: InputFieldDescriptor<T>,
    fieldStates: MutableMap<String, Any?>,
    fieldErrors: List<String> = emptyList(),
    onValueChange: ((T) -> Unit)? = null,
) {
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val error = fieldErrors.firstOrNull()

    // Implicit local state created if none is passed in
    @Suppress("UNCHECKED_CAST")
    val state =
        remember(field.fieldId) {
            field.binding ?: mutableStateOf(
                fieldStates[field.fieldId] as? T ?: field.initialValue,
            )
        }

    when (field) {
        is CustomComposableFieldDescriptor<*> -> {
            @Suppress("UNCHECKED_CAST")
            val stateVal = (field.binding?.value ?: fieldStates[field.fieldId] ?: field.initialValue)

            val typedField = field as CustomComposableFieldDescriptor<T>

            @Suppress("UNCHECKED_CAST")
            val typedState = stateVal as MutableState<T>
            typedField.content(typedState)
        }

        is InputFieldDescriptor.IntFieldDescriptor -> {
            val value = (field.binding?.value ?: fieldStates[field.fieldId] ?: field.initialValue).toString()

            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    val intVal = input.toIntOrNull() ?: 0
                    field.binding?.value = intVal
                    if (field.binding == null) {
                        fieldStates[field.fieldId] = intVal
                    }
                    errorMessage.value = field.validators.mapNotNull { it(intVal) }.firstOrNull()
                    @Suppress("UNCHECKED_CAST")
                    onValueChange?.invoke(intVal as T)
                },
                label = { Text(field.label) },
                isError = errorMessage.value != null || error != null,
                supportingText = {
                    errorMessage.value?.let { Text(it, color = Color.Red) }
                    error?.let { Text(it, color = Color.Red) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        is InputFieldDescriptor.DoubleFieldDescriptor -> {
            val value = (field.binding?.value ?: fieldStates[field.fieldId] ?: field.initialValue).toString()

            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    val doubleVal = input.toDoubleOrNull() ?: 0.0
                    field.binding?.value = doubleVal
                    if (field.binding == null) {
                        fieldStates[field.fieldId] = doubleVal
                    }
                    errorMessage.value = field.validators.mapNotNull { it(doubleVal) }.firstOrNull()
                    @Suppress("UNCHECKED_CAST")
                    onValueChange?.invoke(doubleVal as T)
                },
                label = { Text(field.label) },
                isError = errorMessage.value != null || error != null,
                supportingText = {
                    errorMessage.value?.let { Text(it, color = Color.Red) }
                    error?.let { Text(it, color = Color.Red) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        is InputFieldDescriptor.TextFieldDescriptor -> {
            @Suppress("UNCHECKED_CAST")
            val inputState =
                remember(field.fieldId) {
                    field.binding ?: mutableStateOf(
                        fieldStates[field.fieldId] as? String ?: field.initialValue,
                    )
                }

            OutlinedTextField(
                value = inputState.value,
                onValueChange = { input ->
                    field.binding?.value = input
                    if (field.binding == null) {
                        fieldStates[field.fieldId] = input
                    }
                    inputState.value = input
                    errorMessage.value = field.validators.mapNotNull { it(input) }.firstOrNull()
                    @Suppress("UNCHECKED_CAST")
                    onValueChange?.invoke(input as T)
                },
                label = { Text(field.label) },
                isError = errorMessage.value != null || error != null,
                supportingText = {
                    errorMessage.value?.let { Text(it, color = Color.Red) }
                    error?.let { Text(it, color = Color.Red) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        is InputFieldDescriptor.FloatFieldDescriptor -> {
            val value = (field.binding?.value ?: fieldStates[field.fieldId] ?: field.initialValue).toString()

            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    val floatVal = input.toFloatOrNull() ?: 0f
                    field.binding?.value = floatVal
                    if (field.binding == null) {
                        fieldStates[field.fieldId] = floatVal
                    }
                    errorMessage.value = field.validators.mapNotNull { it(floatVal) }.firstOrNull()
                    @Suppress("UNCHECKED_CAST")
                    onValueChange?.invoke(floatVal as T)
                },
                label = { Text(field.label) },
                isError = errorMessage.value != null || error != null,
                supportingText = {
                    errorMessage.value?.let { Text(it, color = Color.Red) }
                    error?.let { Text(it, color = Color.Red) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        is InputFieldDescriptor.BooleanFieldDescriptor -> {
            val boolState = remember { mutableStateOf(fieldStates[field.fieldId] as? Boolean ?: field.initialValue) }
            fieldStates[field.fieldId] = boolState.value

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .toggleable(
                            value = boolState.value,
                            onValueChange = {
                                boolState.value = it
                                fieldStates[field.fieldId] = it
                                @Suppress("UNCHECKED_CAST")
                                onValueChange?.invoke(it as T)
                            },
                        ),
            ) {
                Checkbox(
                    checked = boolState.value,
                    onCheckedChange = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = field.label)
            }
        }

        is InputFieldDescriptor.DropdownFieldDescriptor<*> -> {
            var expanded by remember { mutableStateOf(false) }
            val selectedOption = field.binding?.value ?: fieldStates[field.fieldId] ?: field.initialValue

            @Suppress("UNCHECKED_CAST")
            val dropdownField = field as InputFieldDescriptor.DropdownFieldDescriptor<Enum<*>>

            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("${field.label}: ${(selectedOption as Enum<*>).name}")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    dropdownField.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                dropdownField.binding?.value = option
                                if (field.binding == null) {
                                    fieldStates[field.fieldId] = option
                                }
                                expanded = false
                                @Suppress("UNCHECKED_CAST")
                                onValueChange?.invoke(option as T)
                            },
                        )
                    }
                }
            }
        }
        is InputFieldDescriptor.SliderFieldDescriptor -> {
            val value = (field.binding?.value ?: fieldStates[field.fieldId] ?: field.initialValue) as Float
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            ) {
                Text(text = field.label)
                Slider(
                    value = value,
                    onValueChange = { input ->
                        field.binding?.value = input
                        if (field.binding == null) {
                            fieldStates[field.fieldId] = input
                        }
                        errorMessage.value = field.validators.mapNotNull { it(input) }.firstOrNull()
                        @Suppress("UNCHECKED_CAST")
                        onValueChange?.invoke(input as T)
                    },
                    valueRange = field.valueRange,
                    steps = if (field.step > 0) ((field.valueRange.endInclusive - field.valueRange.start) / field.step).toInt() - 1 else 0,
                )
                field.valueLabels[value]?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall)
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
