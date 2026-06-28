package com.abyxcz.composeforms.migration

import com.abyxcz.v2core.core.model.form
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaDiffTest {
    @Test
    fun detectsAddedRemovedAndKindChanged() {
        val from = form { section("s") { text("name"); text("secret"); checkbox("old") } }
        val to = form { section("s") { text("name"); password("secret"); checkbox("new") } }

        val diff = diffSchemas(from, to)
        assertEquals(listOf("new"), diff.added.map { it.name })
        assertEquals(listOf("old"), diff.removed.map { it.name })
        assertEquals(listOf("secret"), diff.kindChanged.map { it.second.name })
        assertEquals(listOf("name"), diff.unchanged.map { it.name })
        assertTrue(!diff.isEmpty)
    }

    @Test
    fun identicalSchemasYieldEmptyDiff() {
        val a = form { section("s") { text("name") } }
        val b = form { section("other") { text("name") } }
        assertTrue(diffSchemas(a, b).isEmpty)
    }

    @Test
    fun promptMentionsFieldNamesAndJsonShape() {
        val from = form { section("s") { text("phone_number") } }
        val to = form { section("s") { text("phone") } }
        val prompt = MigrationPrompt.build(from, to)
        assertTrue(prompt.contains("phone_number"))
        assertTrue(prompt.contains("phone"))
        assertTrue(prompt.contains("\"renames\""))
    }
}
