package com.abyxcz.v2core.core.model

class MinLengthRule(
    private val min: Int,
    private val message: String = "Too short",
) : ValidationRule<String> {
    override fun validate(value: String?): String? = if (value == null || value.length < min) message else null
}
