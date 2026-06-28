package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.CheckboxField
import com.abyxcz.v2core.core.model.DropdownField
import com.abyxcz.v2core.core.model.FloatField
import com.abyxcz.v2core.core.model.FormField
import com.abyxcz.v2core.core.model.FormSchema
import com.abyxcz.v2core.core.model.PasswordField
import com.abyxcz.v2core.core.model.SliderField
import com.abyxcz.v2core.core.model.TextField
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Serializes a `FormState` snapshot (`Map<String, Any?>`) to JSON and back, using the
 * [FormSchema] for type information.
 *
 * [decode] walks the CURRENT schema's fields, so it doubles as the structural reconciler:
 *  - a field present in the schema but missing from the stored JSON falls back to the
 *    field's default value (this is how an added field "just works");
 *  - a stored key absent from the current schema is simply never read (forward-compatible).
 *
 * Dropdowns are persisted by their stable option KEY (the `String` in `Pair<String, T>`),
 * never the raw `T`, so enum / object option values round-trip safely as plain JSON.
 */
object FormValuesCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(
        schema: FormSchema,
        values: Map<String, Any?>,
    ): String {
        val obj =
            buildJsonObject {
                schema.fields().forEach { field -> put(field.name, encodeField(field, values[field.name])) }
            }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    fun decode(
        schema: FormSchema,
        text: String,
    ): Map<String, Any?> = decode(schema, json.parseToJsonElement(text).jsonObject)

    fun decode(
        schema: FormSchema,
        obj: JsonObject,
    ): Map<String, Any?> = schema.fields().associate { field -> field.name to decodeField(field, obj[field.name]) }

    private fun encodeField(
        field: FormField<*>,
        value: Any?,
    ): JsonElement =
        when (field) {
            is TextField -> JsonPrimitive(value as? String)
            is PasswordField -> JsonPrimitive(value as? String)
            is FloatField -> JsonPrimitive((value as? Number)?.toFloat())
            is SliderField -> JsonPrimitive((value as? Number)?.toFloat())
            is CheckboxField -> JsonPrimitive(value as? Boolean)
            is DropdownField<*> -> JsonPrimitive(field.keyForValue(value))
            else -> JsonNull
        }

    private fun decodeField(
        field: FormField<*>,
        element: JsonElement?,
    ): Any? {
        val prim = (element as? JsonPrimitive)?.takeUnless { it is JsonNull }
        return when (field) {
            is TextField -> prim?.contentOrNull ?: field.value
            is PasswordField -> prim?.contentOrNull ?: field.value
            is FloatField -> prim?.floatOrNull ?: field.value ?: field.placeholder
            is SliderField -> prim?.floatOrNull ?: field.value ?: field.valueRange.start
            is CheckboxField -> prim?.booleanOrNull ?: field.value ?: false
            is DropdownField<*> -> field.valueForKey(prim?.contentOrNull) ?: field.value
            else -> null
        }
    }
}

/** Resolves the stable option key for a selected dropdown value (or a value that is already a key). */
private fun DropdownField<*>.keyForValue(value: Any?): String? {
    options.firstOrNull { it.second == value }?.let { return it.first }
    return options.firstOrNull { it.first == value }?.first
}

/** Resolves the option value (`T`) for a stored key. */
private fun DropdownField<*>.valueForKey(key: String?): Any? {
    if (key == null) return null
    return options.firstOrNull { it.first == key }?.second
}
