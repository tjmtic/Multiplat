package com.abyxcz.v2core.core.model

@DslMarker
annotation class FormDsl

/**
 * The main entry point for the Compose Forms Kotlin DSL.
 * Use this builder to define sections, fields, and global form properties.
 */
@FormDsl
class FormBuilder {
    private val schema = FormSchema()

    fun section(
        title: String,
        block: SectionBuilder.() -> Unit,
    ) {
        val sectionBuilder = SectionBuilder(title)
        sectionBuilder.block()
        schema.sections += sectionBuilder.build()
    }

    fun submitButton(
        label: String = "Submit",
        block: SubmitButtonBuilder.() -> Unit = {},
    ) {
        val builder =
            SubmitButtonBuilder().apply {
                this.label = label
                block()
            }
        schema.submitButtonLabel = builder.label
    }

    fun build(): FormSchema = schema
}

/**
 * DSL Builder for a form section. Groups related fields together under a single title.
 */
@FormDsl
class SectionBuilder(
    private val title: String,
) {
    private val fields = mutableListOf<FormField<*>>()

    fun text(
        name: String,
        block: TextField.() -> Unit = {},
    ) {
        val field = TextField(name).apply(block)
        fields += field
    }

    fun float(
        name: String,
        block: FloatField.() -> Unit = {},
    ) {
        val field = FloatField(name).apply(block)
        fields += field
    }

    fun password(
        name: String,
        block: PasswordField.() -> Unit = {},
    ) {
        val field = PasswordField(name).apply(block)
        fields += field
    }

    fun checkbox(
        name: String,
        block: CheckboxField.() -> Unit = {},
    ) {
        val field = CheckboxField(name).apply(block)
        fields += field
    }

    fun <T> dropdown(
        name: String,
        options: List<Pair<String, T>>,
        block: DropdownField<T>.() -> Unit = {},
    ) {
        val field =
            DropdownField<T>(name).apply {
                this.options = options
                block()
            }
        fields += field
    }

    fun slider(
        name: String,
        block: SliderField.() -> Unit = {},
    ) {
        val field = SliderField(name).apply(block)
        fields += field
    }

    fun build(): FormSection = FormSection(title).also { it.fields += fields }
}

class SubmitButtonBuilder {
    var label: String = "Submit"
}
