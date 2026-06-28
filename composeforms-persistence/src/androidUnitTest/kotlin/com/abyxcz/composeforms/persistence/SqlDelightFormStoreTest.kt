package com.abyxcz.composeforms.persistence

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.abyxcz.composeforms.persistence.db.FormStoreDatabase
import com.abyxcz.v2core.core.model.form
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlDelightFormStoreTest {
    private fun newStore(transforms: TransformRegistry = TransformRegistry.Empty): SqlDelightFormStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FormStoreDatabase.Schema.create(driver)
        return SqlDelightFormStore(FormStoreDatabase(driver), transforms)
    }

    private val profile =
        form {
            section("profile") {
                text("name")
                checkbox("active")
                dropdown("role", options = listOf("Admin" to 1, "User" to 2))
            }
        }

    @Test
    fun putThenGetRoundTrips() = runBlocking {
        val store = newStore()
        val key = ObjectKey("Person", "1")
        store.put(key, profile, mapOf("name" to "Tim", "active" to true, "role" to 2))

        val loaded = store.get(key, profile)
        assertEquals("Tim", loaded?.get("name"))
        assertEquals(true, loaded?.get("active"))
        assertEquals(2, loaded?.get("role"))
    }

    @Test
    fun getMissingReturnsNull() = runBlocking {
        assertNull(newStore().get(ObjectKey("Person", "nope"), profile))
    }

    @Test
    fun getByTypeReturnsEveryInstance() = runBlocking {
        val store = newStore()
        store.put(ObjectKey("Person", "1"), profile, mapOf("name" to "A"))
        store.put(ObjectKey("Person", "2"), profile, mapOf("name" to "B"))
        store.put(ObjectKey("Other", "1"), profile, mapOf("name" to "C"))

        val people = store.getByType("Person", profile)
        assertEquals(2, people.size)
        assertEquals(setOf("A", "B"), people.map { it.second["name"] }.toSet())
    }

    @Test
    fun deleteRemoves() = runBlocking {
        val store = newStore()
        val key = ObjectKey("Person", "1")
        store.put(key, profile, mapOf("name" to "Tim"))
        store.delete(key)
        assertNull(store.get(key, profile))
    }

    @Test
    fun addedFieldReconcilesWithoutMigration() = runBlocking {
        // Stored against an older structure (name only)...
        val v1 = form { section("s") { text("name") } }
        val store = newStore()
        val key = ObjectKey("Person", "1")
        store.put(key, v1, mapOf("name" to "Tim"))

        // ...read against a newer structure that added a checkbox: no migration needed.
        val v2 = form { section("s") { text("name"); checkbox("active") } }
        val loaded = store.get(key, v2)
        assertEquals("Tim", loaded?.get("name"))
        assertEquals(false, loaded?.get("active"))
    }

    @Test
    fun semanticTransformHandlesRename() = runBlocking {
        val v1 = form { section("s") { text("phone_number") } }
        val v2 = form { section("s") { text("phone") } }

        val renameRegistry =
            object : TransformRegistry {
                override fun find(fromVersion: String, toVersion: String): SemanticTransform? =
                    if (fromVersion == v1.fingerprint() && toVersion == v2.fingerprint()) {
                        SemanticTransform { old: JsonObject ->
                            buildJsonObject {
                                put("phone", JsonPrimitive(old["phone_number"]?.jsonPrimitive?.contentOrNull))
                            }
                        }
                    } else {
                        null
                    }
            }

        val store = newStore(renameRegistry)
        val key = ObjectKey("Person", "1")
        store.put(key, v1, mapOf("phone_number" to "555-1234"))

        val loaded = store.get(key, v2)
        assertEquals("555-1234", loaded?.get("phone"))
        assertTrue(loaded?.containsKey("phone_number") == false)
    }
}
