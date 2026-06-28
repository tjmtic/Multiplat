package com.abyxcz.multiplat.demo

import com.abyxcz.composeforms.migration.CachingTransformRegistry
import com.abyxcz.composeforms.migration.LlmMigrator
import com.abyxcz.composeforms.persistence.FormStore
import com.abyxcz.composeforms.persistence.InMemoryFormStore

/**
 * Bundles the persistence + migration stack for the sample.
 *
 * The [store] and [migrator] MUST share the same [registry] instance — the migrator writes
 * validated transforms into it and the store reads them on the next load. Platform entry points
 * override [store] with a SQLDelight-backed store (persists across launches); the default in-memory
 * store keeps `@Preview` and unconfigured runs working.
 */
class DemoEnvironment(
    val registry: CachingTransformRegistry = CachingTransformRegistry(),
    val store: FormStore = InMemoryFormStore(registry),
    val migrator: LlmMigrator = LlmMigrator(StubLlmEngine()),
)
