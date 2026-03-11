package com.abyxcz.composeforms.validation

import com.abyxcz.composeforms.model.FormSchema

interface Validator {
    fun validateData(
        schema: FormSchema<*>,
        values: Map<String, Any?>,
    ): ValidationResult
}

data class ValidationResult(
    val fieldErrors: Map<String, List<String>>,
    val schemaErrors: List<String>,
    val isValid: Boolean,
)
