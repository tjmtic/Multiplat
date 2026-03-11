package com.abyxcz.composeforms.model

import androidx.compose.runtime.MutableState

@StableFormApi
class FormRunner<ResultType>(
    private val schema: FormSchema<ResultType>,
    val bindingMap: Map<String, MutableState<*>>,
) {
    fun currentValues(): Map<String, Any?> =
        bindingMap.mapValues { it.value.value }

    fun fieldErrors(): Map<String, List<String>> {
        val values = currentValues()

        return schema.groups
            .flatMap { it.fields }
            .associate { field ->
                val value = values[field.fieldId]

                @Suppress("UNCHECKED_CAST")
                val errors = validateField(field as InputFieldDescriptor<Any?>, value)
                field.fieldId to errors
            }
            .filterValues { it.isNotEmpty() }
    }

    fun groupErrors(): MutableList<FormValidationError> {
        val values = currentValues()
        val errors = mutableListOf<FormValidationError>()

        schema.groups.forEach { group ->
            group.fields.forEach { field ->
                val value = values[field.fieldId]
                val messages =
                    (field as? InputFieldDescriptor<Any?>)
                        ?.validators?.mapNotNull { it(value) }
                        .orEmpty()
                if (messages.isNotEmpty()) {
                    errors += FormValidationError.FieldError(field.fieldId, messages)
                }
            }

            group.groupValidator?.invoke(values)?.let { groupMessages ->
                if (groupMessages.isNotEmpty()) {
                    errors += FormValidationError.GroupError(group.groupLabel, groupMessages)
                }
            }
        }
        return errors
    }

    fun schemaErrors(): List<String> =
        schema.formValidator?.invoke(currentValues()) ?: emptyList()

    fun allErrors(): Pair<Map<String, List<String>>, List<String>> =
        fieldErrors() to schemaErrors()

    fun isValid(): Boolean = fieldErrors().isEmpty() && groupErrors().isEmpty() && schemaErrors().isEmpty()

    fun buildResult(): ResultType = schema.buildResult(currentValues())

    fun validateForm(
        schema: FormSchema<*>,
        values: Map<String, Any?>,
    ): ValidationResult {
        val errors = mutableListOf<FormValidationError>()

        schema.groups.forEach { group ->
            group.fields.forEach { field ->
                val value = values[field.fieldId]
                val messages =
                    (field as? InputFieldDescriptor<Any?>)
                        ?.validators
                        ?.mapNotNull { it(value) }
                        .orEmpty()
                if (messages.isNotEmpty()) {
                    errors += FormValidationError.FieldError(field.fieldId, messages)
                }
            }

            group.groupValidator?.invoke(values)?.let { groupMessages ->
                if (groupMessages.isNotEmpty()) {
                    errors += FormValidationError.GroupError(group.groupLabel, groupMessages)
                }
            }
        }

        schema.formValidator?.invoke(values)?.let { schemaMessages ->
            if (schemaMessages.isNotEmpty()) {
                errors += FormValidationError.SchemaError(schemaMessages)
            }
        }

        return ValidationResult(errors)
    }
}

fun <T> validateField(field: InputFieldDescriptor<T>, value: Any?): List<String> {
    @Suppress("UNCHECKED_CAST")
    return field.validators.mapNotNull { rule ->
        rule.invoke(value as? T ?: field.initialValue)
    }
}
