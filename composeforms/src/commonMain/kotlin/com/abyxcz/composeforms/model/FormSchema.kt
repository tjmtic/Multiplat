package com.abyxcz.composeforms.model

@StableFormApi
data class FormSchema<ResultType>(
    val formName: String,
    val submitButtonText: String = "Submit",
    val groups: List<InputFieldGroup>,
    val buildResult: (Map<String, Any?>) -> ResultType,
    val formValidator: ((Map<String, Any?>) -> List<String>)? = null,
)
