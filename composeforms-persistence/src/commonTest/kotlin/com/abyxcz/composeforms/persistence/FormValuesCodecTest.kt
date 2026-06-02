package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.form
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormValuesCodecTest {
    private fun sampleSchema() =
        form {
            section("profile") {
                text("name")
                checkbox("active")
                slider("level") { valueRange = 0f..10f }
                dropdown("role", options = listOf("Admin" to 1, "User" to 2))
            }
        }

    @Test
    fun roundTripsAllSupportedKinds() {
        val schema = sampleSchema()
        val values = mapOf("name" to "Tim", "active" to true, "level" to 5f, "role" to 1)

        val decoded = FormValuesCodec.decode(schema, FormValuesCodec.encode(schema, values))

        assertEquals("Tim", decoded["name"])
        assertEquals(true, decoded["active"])
        assertEquals(5f, decoded["level"])
        assertEquals(1, decoded["role"]) // rehydrated to the option value T, not its key
    }

    @Test
    fun dropdownPersistsByStableKeyNotRawValue() {
        val schema = sampleSchema()
        val json = FormValuesCodec.encode(schema, mapOf("role" to 2))
        // Stored as the option label/key, so enum/object values stay JSON-safe and stable.
        assertTrue(json.contains("\"User\""))
        assertFalse(json.contains("\"role\":2"))
    }

    @Test
    fun addedFieldReconcilesToDefaultWhenMissing() {
        val before = form { section("s") { text("name") } }
        val after = form { section("s") { text("name"); checkbox("active") } }

        val storedByOldSchema = FormValuesCodec.encode(before, mapOf("name" to "Tim"))
        val decoded = FormValuesCodec.decode(after, storedByOldSchema)

        assertEquals("Tim", decoded["name"])
        assertEquals(false, decoded["active"]) // added field gets its default
    }

    @Test
    fun removedFieldIsDroppedOnRead() {
        val before = form { section("s") { text("name"); checkbox("active") } }
        val after = form { section("s") { text("name") } }

        val stored = FormValuesCodec.encode(before, mapOf("name" to "Tim", "active" to true))
        val decoded = FormValuesCodec.decode(after, stored)

        assertEquals(setOf("name"), decoded.keys)
        assertFalse(decoded.containsKey("active"))
    }

    @Test
    fun coercesStoredNumberToFloatForSlider() {
        val schema = form { section("s") { slider("level") { valueRange = 0f..10f } } }
        // Raw JSON integer should coerce to Float, not Int/Double.
        val decoded = FormValuesCodec.decode(schema, """{"level":5}""")
        assertEquals(5f, decoded["level"])
    }

    @Test
    fun missingDropdownKeyFallsBackToFieldDefault() {
        val schema = form { section("s") { dropdown("role", options = listOf("Admin" to 1, "User" to 2)) } }
        val decoded = FormValuesCodec.decode(schema, """{}""")
        assertNull(decoded["role"]) // no default value set on the field
    }
}
