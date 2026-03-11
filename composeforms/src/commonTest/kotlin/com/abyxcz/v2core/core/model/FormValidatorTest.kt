package com.abyxcz.v2core.core.model

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormValidatorTest {
    @Test
    fun requiredRuleReturnsErrorForNull() {
        val rule = RequiredRule<String>("Error")
        assertEquals("Error", rule.validate(null))
    }

    @Test
    fun requiredRuleReturnsErrorForBlankString() {
        val rule = RequiredRule<String>("Error")
        assertEquals("Error", rule.validate(""))
        assertEquals("Error", rule.validate("  "))
    }

    @Test
    fun requiredRuleReturnsNullForValidString() {
        val rule = RequiredRule<String>("Error")
        assertEquals(null, rule.validate("Valid"))
    }

    @Test
    fun validateFormReturnsFalseWhenRequiredFieldIsMissing() {
        val schema =
            FormSchema().apply {
                sections +=
                    FormSection("Section").apply {
                        fields +=
                            TextField("name").apply {
                                required("Name is required")
                            }
                    }
            }
        val context =
            FormContext(
                values = mutableStateOf(emptyMap()),
                errors = mutableStateOf(emptyMap()),
            )

        val result = validateForm(schema, context)

        assertFalse(result)
        assertEquals("Name is required", context.errors.value["name"])
    }

    @Test
    fun validateFormReturnsTrueWhenRequiredFieldIsPresent() {
        val schema =
            FormSchema().apply {
                sections +=
                    FormSection("Section").apply {
                        fields +=
                            TextField("name").apply {
                                required("Name is required")
                            }
                    }
            }
        val context =
            FormContext(
                values = mutableStateOf(mapOf("name" to "John Doe")),
                errors = mutableStateOf(emptyMap()),
            )

        val result = validateForm(schema, context)

        assertTrue(result)
        assertTrue(context.errors.value.isEmpty())
    }

    @Test
    fun validateFormIgnoresInvisibleFields() {
        val schema =
            FormSchema().apply {
                sections +=
                    FormSection("Section").apply {
                        fields +=
                            TextField("name").apply {
                                required("Name is required")
                                visible = false
                            }
                    }
            }
        val context =
            FormContext(
                values = mutableStateOf(emptyMap()),
                errors = mutableStateOf(emptyMap()),
            )

        val result = validateForm(schema, context)

        assertTrue(result)
        assertTrue(context.errors.value.isEmpty())
    }
}
