package com.abyxcz.v2core.core.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class FormContext(
    val values: MutableState<Map<String, Any?>>,
    val errors: MutableState<Map<String, String?>> = mutableStateOf(emptyMap()),
)
