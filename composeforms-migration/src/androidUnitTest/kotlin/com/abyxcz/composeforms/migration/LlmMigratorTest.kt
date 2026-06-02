package com.abyxcz.composeforms.migration

import com.abyxcz.composeforms.persistence.FormValuesCodec
import com.abyxcz.composeforms.persistence.fingerprint
import com.abyxcz.v2core.core.model.form
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Returns queued responses in order; clamps to the last once exhausted. Records call count. */
private class FakeLlmEngine(private val responses: List<String>) : LlmEngine {
    var calls = 0
        private set

    override suspend fun generate(prompt: String): String {
        val response = responses[minOf(calls, responses.lastIndex)]
        calls++
        return response
    }
}

class LlmMigratorTest {
    private val from = form { section("s") { text("phone_number") } }
    private val to = form { section("s") { text("phone") } }
    private val sample: JsonObject = buildJsonObject { put("phone_number", JsonPrimitive("555")) }

    @Test
    fun generatesValidatedRenameTransform() = runBlocking {
        val engine = FakeLlmEngine(listOf("""{"renames":{"phone_number":"phone"}}"""))
        val transform = LlmMigrator(engine).prepare(from, to, listOf(sample))

        assertNotNull(transform)
        assertEquals("555", FormValuesCodec.decode(to, transform.apply(sample))["phone"])
    }

    @Test
    fun retriesUntilACandidateValidates() = runBlocking {
        val engine = FakeLlmEngine(listOf("not json at all", """{"renames":{"phone_number":"phone"}}"""))
        val transform = LlmMigrator(engine).prepare(from, to, listOf(sample))

        assertNotNull(transform)
        assertEquals(2, engine.calls)
    }

    @Test
    fun returnsNullWhenCandidateLosesCommonFieldData() = runBlocking {
        // "name" exists in both schemas, so dropping it is real data loss -> rejected every attempt.
        val fromTwo = form { section("s") { text("name"); text("phone_number") } }
        val toTwo = form { section("s") { text("name"); text("phone") } }
        val sampleTwo = buildJsonObject { put("name", JsonPrimitive("Tim")); put("phone_number", JsonPrimitive("555")) }
        val engine = FakeLlmEngine(listOf("""{"renames":{"phone_number":"phone"},"drops":["name"]}"""))

        val transform = LlmMigrator(engine, maxAttempts = 2).prepare(fromTwo, toTwo, listOf(sampleTwo))

        assertNull(transform)
        assertEquals(2, engine.calls)
    }

    @Test
    fun preservesCommonFieldThatIsEmpty() = runBlocking {
        // 'note' is common to both schemas but null in the data — preservation, not non-nullness,
        // is the rule, so the rename must still validate.
        val fromTwo = form { section("s") { text("note"); text("phone_number") } }
        val toTwo = form { section("s") { text("note"); text("phone") } }
        val sampleTwo = buildJsonObject { put("note", JsonNull); put("phone_number", JsonPrimitive("555")) }
        val engine = FakeLlmEngine(listOf("""{"renames":{"phone_number":"phone"}}"""))

        assertNotNull(LlmMigrator(engine).prepare(fromTwo, toTwo, listOf(sampleTwo)))
        Unit
    }

    @Test
    fun identicalSchemasSkipTheModel() = runBlocking {
        val engine = FakeLlmEngine(listOf("should not be called"))
        val same = form { section("s") { text("phone") } }
        val transform = LlmMigrator(engine).prepare(to, same, listOf(buildJsonObject { put("phone", JsonPrimitive("x")) }))

        assertNotNull(transform)
        assertEquals(0, engine.calls)
    }

    @Test
    fun prepareIntoRegistersUnderFingerprintPair() = runBlocking {
        val engine = FakeLlmEngine(listOf("""{"renames":{"phone_number":"phone"}}"""))
        val registry = CachingTransformRegistry()

        val ok = LlmMigrator(engine).prepareInto(registry, from, to, listOf(sample))

        assertTrue(ok)
        assertTrue(registry.has(from.fingerprint(), to.fingerprint()))
        assertNotNull(registry.find(from.fingerprint(), to.fingerprint()))
        assertFalse(registry.has(to.fingerprint(), from.fingerprint()))
    }
}
