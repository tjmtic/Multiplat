package com.abyxcz.v2core.core.model

class FormSection(
    val title: String,
) {
    val fields = mutableListOf<FormField<*>>()
}

class FormSchema {
    val sections = mutableListOf<FormSection>()
    var submitButtonLabel: String = "Submit"
}

fun form(block: FormBuilder.() -> Unit): FormSchema {
    val builder = FormBuilder()
    builder.block()
    return builder.build()
}
