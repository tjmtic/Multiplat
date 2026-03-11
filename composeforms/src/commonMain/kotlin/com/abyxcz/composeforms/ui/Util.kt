package com.abyxcz.composeforms.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.abyxcz.composeforms.model.FormSchema
import com.abyxcz.composeforms.model.InputFieldDescriptor
import com.abyxcz.composeforms.model.InternalFormApi

fun buildBindingMap(schema: FormSchema<*>): MutableMap<String, MutableState<*>> =
    schema.groups
        .flatMap { it.fields }
        .associate { it.fieldId to mutableStateOf(it.initialValue) }
        .toMutableMap()

@InternalFormApi
fun wrapFieldStates(fieldStates: MutableMap<String, Any?>): MutableMap<String, MutableState<*>> =
    fieldStates
        .mapValues { (_, value) ->
            value as? MutableState<*> ?: mutableStateOf(value)
        }.toMutableMap()

@Composable
fun <T> resolveFieldState(
    field: InputFieldDescriptor<T>,
    bindingMap: Map<String, MutableState<*>> = emptyMap(),
): MutableState<T> =
    bindingMap[field.fieldId] as? MutableState<T>
        ?: remember(field.fieldId) { mutableStateOf(field.initialValue ?: error("Missing initialValue for ${field.fieldId}")) }
