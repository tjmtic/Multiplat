package com.abyxcz.composeforms.model

sealed class FormValidationError {
    data class FieldError(
        val fieldId: String,
        val messages: List<String>,
    ) : FormValidationError()

    data class GroupError(
        val groupLabel: String,
        val messages: List<String>,
    ) : FormValidationError()

    data class SchemaError(
        val messages: List<String>,
    ) : FormValidationError()
}
