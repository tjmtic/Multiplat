package com.abyxcz.v2core.core.model

data class FieldError(
    val fieldName: String,
    val errors: List<String>,
)

fun validateForm(
    form: FormSchema,
    context: FormContext,
): Boolean {
    val newErrors = mutableMapOf<String, String?>()

    form.sections.forEach { section ->
        section.fields.forEach { field ->
            if (field.isVisible(context.values)) {
                val rawValue = context.values.value[field.name]

                @Suppress("UNCHECKED_CAST")
                val errors = (field as FormField<Any?>).validationRules.mapNotNull { it.validate(rawValue) }

                if (errors.isNotEmpty()) {
                    newErrors[field.name] = errors.first() // For now, show only one error
                }
            }
        }
    }

    context.errors.value = newErrors
    return newErrors.isEmpty()
}

interface ValidationRule<T> {
    fun validate(value: T?): String? // Return error message or null if valid
}

class RequiredRule<T>(
    private val message: String = "Required",
) : ValidationRule<T> {
    override fun validate(value: T?): String? = if (value == null || (value is String && value.isBlank())) message else null
}
