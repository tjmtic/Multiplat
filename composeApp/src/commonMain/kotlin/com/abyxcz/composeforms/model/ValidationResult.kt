package com.abyxcz.composeforms.model

data class ValidationResult(
    val errors: List<FormValidationError>,
    val isValid: Boolean = errors.isEmpty(),
)
