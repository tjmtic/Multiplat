package com.abyxcz.v2core.core.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

typealias FormState = MutableState<Map<String, Any?>>

abstract class FormField<T>(
    val name: String,
    var label: String = "",
    var value: T? = null,
    var enabled: Boolean = true,
    var visible: Boolean = true,
    var validationRules: MutableList<ValidationRule<T>> = mutableListOf(),
    var condition: ((FormState) -> Boolean)? = null,
) {
    fun required(message: String = "This field is required") {
        validationRules += RequiredRule(message)
    }

    fun addRule(rule: ValidationRule<T>) {
        validationRules += rule
    }

    fun isVisible(state: FormState): Boolean = condition?.invoke(state) ?: visible

    @Suppress("UNCHECKED_CAST")
    open fun validate(value: Any?): List<String> {
        @Suppress("UNCHECKED_CAST")
        return validationRules.mapNotNull { it.validate(value as? T) }
    }

    fun visibleIf(predicate: (FormState) -> Boolean) {
        this.condition = predicate
    }
}

class FloatField(name: String) : FormField<Float>(name) {
    var placeholder: Float = 0F
}

class TextField(name: String) : FormField<String>(name) {
    var placeholder: String = ""
    var minLength: Int? = null
}

class PasswordField(name: String) : FormField<String>(name) {
    var minLength: Int? = null
}

class CheckboxField(name: String) : FormField<Boolean>(name)

class DropdownField<T>(name: String) : FormField<T>(name) {
    var options: List<Pair<String, T>> = emptyList()
}
