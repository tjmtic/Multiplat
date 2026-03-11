package com.abyxcz.composeforms.model

@StableFormApi
data class InputFieldGroup(
    val groupLabel: String,
    val fields: List<InputFieldDescriptor<*>>,
    val groupValidator: ((Map<String, Any?>) -> List<String>)? = null,
)
