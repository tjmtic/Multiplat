package com.abyxcz.multiplat.demo

import com.abyxcz.v2core.core.model.FormSchema
import com.abyxcz.v2core.core.model.form

/** One version of the backend-defined structure for the same logical object. */
data class BackendVersion(
    val label: String,
    val note: String,
    val schema: FormSchema,
)

/**
 * A fake backend that serves three evolving versions of a "Contact" object's structure — the kind
 * of change that would normally require an app update, here applied at runtime:
 *
 *  - v1 → v2: adds `email` (additive → handled by pure reconcile, no LLM).
 *  - v2 → v3: renames `phone_number` → `phone` and adds `subscribe` (the rename is ambiguous →
 *             needs a validated [TransformSpec] from the migration pipeline).
 */
object SampleBackend {
    val versions: List<BackendVersion> =
        listOf(
            BackendVersion(
                label = "v1",
                note = "Original structure: name + phone_number.",
                schema =
                    form {
                        section("Contact") {
                            text("name") { label = "Name" }
                            text("phone_number") { label = "Phone number" }
                        }
                    },
            ),
            BackendVersion(
                label = "v2",
                note = "Added 'email' — additive, so zero migration (pure reconcile).",
                schema =
                    form {
                        section("Contact") {
                            text("name") { label = "Name" }
                            text("phone_number") { label = "Phone number" }
                            text("email") { label = "Email" }
                        }
                    },
            ),
            BackendVersion(
                label = "v3",
                note = "Renamed phone_number → phone, added 'subscribe' — rename needs an LLM transform.",
                schema =
                    form {
                        section("Contact") {
                            text("name") { label = "Name" }
                            text("phone") { label = "Phone" }
                            text("email") { label = "Email" }
                            checkbox("subscribe") { label = "Subscribe to updates" }
                        }
                    },
            ),
        )
}
