package com.abyxcz.composeforms.ui.v2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.abyxcz.v2core.core.model.CheckboxField
import com.abyxcz.v2core.core.model.DropdownField
import com.abyxcz.v2core.core.model.FormContext
import com.abyxcz.v2core.core.model.FormField
import com.abyxcz.v2core.core.model.PasswordField
import com.abyxcz.v2core.core.model.TextField

@Composable
@Suppress("ktlint:standard:function-naming")
fun FieldRenderer(
    field: FormField<*>,
    context: FormContext,
) {
    when (field) {
        is TextField -> RenderTextField(field, context)
        is PasswordField -> RenderPasswordField(field, context)
        is CheckboxField -> RenderCheckboxField(field, context)
        is DropdownField -> RenderDropdownField(field, context)
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun RenderTextField(
    field: TextField,
    context: FormContext,
) {
    val value = context.values.value[field.name] as? String ?: ""
    val error = context.errors.value[field.name]

    OutlinedTextField(
        value = value,
        onValueChange = {
            context.values.value =
                context.values.value.toMutableMap().apply {
                    put(field.name, it)
                }
        },
        label = { Text(field.label) },
        placeholder = { Text(field.placeholder) },
        isError = error != null,
        modifier = Modifier.fillMaxWidth(),
    )

    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun RenderPasswordField(
    field: PasswordField,
    context: FormContext,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val value = context.values.value[field.name] as? String ?: ""
    val error = context.errors.value[field.name]

    OutlinedTextField(
        value = value,
        onValueChange = {
            context.values.value =
                context.values.value.toMutableMap().apply {
                    put(field.name, it)
                }
        },
        label = { Text(field.label) },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                )
            }
        },
        isError = error != null,
        modifier = Modifier.fillMaxWidth(),
    )

    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun RenderCheckboxField(
    field: CheckboxField,
    context: FormContext,
) {
    val checked = context.values.value[field.name] as? Boolean ?: false
    val error = context.errors.value[field.name]

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = {
                        context.values.value =
                            context.values.value.toMutableMap().apply {
                                put(field.name, it)
                            }
                    },
                ),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // handled by toggleable
        )
        Spacer(Modifier.width(8.dp))
        Text(text = field.label)
    }

    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun <T> RenderDropdownField(
    field: DropdownField<T>,
    context: FormContext,
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedValue = context.values.value[field.name] as? T
    val displayLabel = field.options.firstOrNull { it.second == selectedValue }?.first ?: "Select..."

    Column {
        Text(text = field.label)
        Box {
            OutlinedTextField(
                value = displayLabel,
                onValueChange = {},
                readOnly = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            )

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                field.options.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            context.values.value =
                                context.values.value.toMutableMap().apply {
                                    put(field.name, value)
                                }
                            expanded = false
                        },
                    )
                }
            }
        }

        context.errors.value[field.name]?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
