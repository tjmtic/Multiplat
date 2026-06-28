package com.abyxcz.composeforms.migration

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransformSpecTest {
    @Test
    fun renameCarriesValueUnderNewKey() {
        val spec = TransformSpec(renames = mapOf("phone_number" to "phone"))
        val out =
            spec.toTransform().apply(
                buildJsonObject { put("phone_number", JsonPrimitive("555")) },
            )
        assertEquals("555", out["phone"]?.jsonPrimitive?.contentOrNull)
        assertFalse(out.containsKey("phone_number"))
    }

    @Test
    fun dropsRemoveField() {
        val spec = TransformSpec(drops = listOf("legacy"))
        val out =
            spec.toTransform().apply(
                buildJsonObject {
                    put("keep", JsonPrimitive("a"))
                    put("legacy", JsonPrimitive("b"))
                },
            )
        assertTrue(out.containsKey("keep"))
        assertFalse(out.containsKey("legacy"))
    }

    @Test
    fun constantsBackfillNewField() {
        val spec = TransformSpec(constants = mapOf("active" to JsonPrimitive(true)))
        val out = spec.toTransform().apply(buildJsonObject { put("name", JsonPrimitive("x")) })
        assertEquals(true, out["active"]?.jsonPrimitive?.content?.toBoolean())
        assertEquals("x", out["name"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun untouchedFieldsAreCarriedOver() {
        val out = TransformSpec().toTransform().apply(buildJsonObject { put("a", JsonPrimitive(1)) })
        assertEquals(1, out["a"]?.jsonPrimitive?.content?.toInt())
    }
}
