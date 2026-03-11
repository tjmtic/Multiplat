package com.abyxcz.composeforms.model

import androidx.compose.runtime.MutableState

@StableFormApi
sealed class InputFieldDescriptor<T> {
    abstract val fieldId: String
    abstract val label: String
    abstract val initialValue: T
    open val validators: List<(T) -> String?> = emptyList()
    open val visibleIf: ((Map<String, Any?>) -> Boolean)? = null
    open val binding: MutableState<T>? = null

    @StableFormApi
    data class IntFieldDescriptor(
        override val fieldId: String,
        override val label: String,
        override val initialValue: Int = 0,
        override val validators: List<(Int) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<Int>? = null,
    ) : InputFieldDescriptor<Int>()

    @StableFormApi
    data class DoubleFieldDescriptor(
        override val fieldId: String,
        override val label: String,
        override val initialValue: Double = 0.0,
        override val validators: List<(Double) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<Double>? = null,
    ) : InputFieldDescriptor<Double>()

    @StableFormApi
    data class FloatFieldDescriptor(
        override val fieldId: String,
        override val label: String,
        override val initialValue: Float = 0f,
        override val validators: List<(Float) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<Float>? = null,
    ) : InputFieldDescriptor<Float>()

    @StableFormApi
    data class TextFieldDescriptor(
        override val fieldId: String,
        override val label: String,
        override val initialValue: String = "",
        override val validators: List<(String) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<String>? = null,
    ) : InputFieldDescriptor<String>()

    @StableFormApi
    data class BooleanFieldDescriptor(
        override val fieldId: String,
        override val label: String,
        override val initialValue: Boolean = false,
        override val validators: List<(Boolean) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<Boolean>? = null,
    ) : InputFieldDescriptor<Boolean>()

    @StableFormApi
    data class DropdownFieldDescriptor<T : Enum<T>>(
        override val fieldId: String,
        override val label: String,
        override val initialValue: T,
        val options: List<T>,
        override val validators: List<(T) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<T>? = null,
    ) : InputFieldDescriptor<T>()

    @StableFormApi
    data class SliderFieldDescriptor(
        override val fieldId: String,
        override val label: String,
        override val initialValue: Float,
        val valueRange: ClosedFloatingPointRange<Float>,
        val step: Float = 1f,
        val valueLabels: Map<Float, String> = emptyMap(),
        override val validators: List<(Float) -> String?> = emptyList(),
        override val visibleIf: ((Map<String, Any?>) -> Boolean)? = null,
        override val binding: MutableState<Float>? = null,
    ) : InputFieldDescriptor<Float>()
}
