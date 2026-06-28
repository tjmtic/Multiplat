package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.CheckboxField
import com.abyxcz.v2core.core.model.DropdownField
import com.abyxcz.v2core.core.model.FloatField
import com.abyxcz.v2core.core.model.FormField
import com.abyxcz.v2core.core.model.FormSchema
import com.abyxcz.v2core.core.model.PasswordField
import com.abyxcz.v2core.core.model.SliderField
import com.abyxcz.v2core.core.model.TextField

/** The persisted storage shape of a field, independent of its UI/validation config. */
enum class FieldKind { TEXT, PASSWORD, FLOAT, SLIDER, CHECKBOX, DROPDOWN }

/** Maps a v2 [FormField] to its storage [FieldKind]. */
fun FormField<*>.kind(): FieldKind =
    when (this) {
        is TextField -> FieldKind.TEXT
        is PasswordField -> FieldKind.PASSWORD
        is FloatField -> FieldKind.FLOAT
        is SliderField -> FieldKind.SLIDER
        is CheckboxField -> FieldKind.CHECKBOX
        is DropdownField<*> -> FieldKind.DROPDOWN
        else -> error("Unmapped FormField type: ${this::class.simpleName}")
    }

/** All fields across all sections, in declaration order. */
fun FormSchema.fields(): List<FormField<*>> = sections.flatMap { it.fields }

/**
 * A stable hash of the form's STRUCTURE (each field's name + kind), independent of values
 * and field ordering. Acts as the "implicitly defined type version": the same structure
 * yields the same fingerprint across app launches and platforms, and any structural change
 * yields a different one.
 *
 * Uses FNV-1a (64-bit) to stay dependency-free and identical on every target — this is an
 * identity/versioning hash, not a security primitive.
 */
fun FormSchema.fingerprint(): String =
    fields()
        .map { it.name to it.kind() }
        .sortedBy { it.first }
        .joinToString("|") { (name, kind) -> "$name:$kind" }
        .fnv1a64Hex()

private fun String.fnv1a64Hex(): String {
    var hash = -0x340d631b7bdddcdbL // 0xCBF29CE484222325 FNV offset basis
    val prime = 0x100000001B3L
    for (ch in this) {
        hash = hash xor ch.code.toLong()
        hash *= prime
    }
    return hash.toULong().toString(16).padStart(16, '0')
}
