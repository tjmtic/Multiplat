package com.abyxcz.composeforms.migration

import com.abyxcz.v2core.core.model.FormSchema

/**
 * Builds the LLM prompt for a migration. It hands the model the structural diff and the full
 * field lists, and constrains the output to a single [TransformSpec] JSON object — no prose,
 * no code. Pure and deterministic, so it can be unit-tested without a model.
 */
object MigrationPrompt {
    fun build(
        from: FormSchema,
        to: FormSchema,
    ): String {
        val diff = diffSchemas(from, to)
        fun list(shapes: List<FieldShape>) =
            if (shapes.isEmpty()) "(none)" else shapes.joinToString(", ") { "${it.name}:${it.kind}" }

        return buildString {
            appendLine("You migrate stored form data from an OLD structure to a NEW structure.")
            appendLine("Decide how each OLD field maps to the NEW structure.")
            appendLine()
            appendLine("OLD fields: ${list(from.shapes())}")
            appendLine("NEW fields: ${list(to.shapes())}")
            appendLine()
            appendLine("Added (new, no old source): ${list(diff.added)}")
            appendLine("Removed (gone from new): ${list(diff.removed)}")
            appendLine(
                "Type changed: " +
                    if (diff.kindChanged.isEmpty()) {
                        "(none)"
                    } else {
                        diff.kindChanged.joinToString(", ") { (b, a) -> "${b.name}:${b.kind}->${a.kind}" }
                    },
            )
            appendLine()
            appendLine("Respond with ONLY a JSON object of this exact shape (omit empty members):")
            appendLine("""{"renames":{"oldName":"newName"},"drops":["oldName"],"constants":{"newName":<jsonValue>}}""")
            appendLine("- renames: an old field that is the same data under a new name.")
            appendLine("- drops: old fields with no place in the new structure.")
            appendLine("- constants: new fields needing a sensible default/backfill value.")
            append("Output only the JSON. No explanation.")
        }
    }
}
