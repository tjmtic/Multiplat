package com.abyxcz.composeforms.persistence

import com.abyxcz.v2core.core.model.form
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SchemaFingerprintTest {
    @Test
    fun identicalStructureProducesSameFingerprint() {
        val a = form { section("s") { text("name"); checkbox("active") } }
        val b = form { section("other") { text("name"); checkbox("active") } }
        // Section titles and field ordering must not affect the structural fingerprint.
        assertEquals(a.fingerprint(), b.fingerprint())
    }

    @Test
    fun fieldOrderDoesNotMatter() {
        val a = form { section("s") { text("name"); checkbox("active") } }
        val b = form { section("s") { checkbox("active"); text("name") } }
        assertEquals(a.fingerprint(), b.fingerprint())
    }

    @Test
    fun addingFieldChangesFingerprint() {
        val a = form { section("s") { text("name") } }
        val b = form { section("s") { text("name"); checkbox("active") } }
        assertNotEquals(a.fingerprint(), b.fingerprint())
    }

    @Test
    fun changingKindOfSameNameChangesFingerprint() {
        val a = form { section("s") { text("secret") } }
        val b = form { section("s") { password("secret") } }
        assertNotEquals(a.fingerprint(), b.fingerprint())
    }
}
