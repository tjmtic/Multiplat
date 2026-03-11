package com.abyxcz.composeforms.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

data class CustomComposableFieldDescriptor<T>(
    override val fieldId: String,
    override val label: String,
    override val initialValue: T,
    val content: @Composable (MutableState<T>) -> Unit,
) : InputFieldDescriptor<T>()
