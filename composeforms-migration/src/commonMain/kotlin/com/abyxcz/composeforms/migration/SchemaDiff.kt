package com.abyxcz.composeforms.migration

import com.abyxcz.composeforms.persistence.FieldKind
import com.abyxcz.composeforms.persistence.fields
import com.abyxcz.composeforms.persistence.kind
import com.abyxcz.v2core.core.model.FormSchema

/** One field's name + storage kind — the structural unit a migration reasons about. */
data class FieldShape(
    val name: String,
    val kind: FieldKind,
)

/** The deterministic structural delta between two schemas. The LLM only has to resolve the ambiguous bits. */
data class SchemaDiff(
    val added: List<FieldShape>,
    val removed: List<FieldShape>,
    val kindChanged: List<Pair<FieldShape, FieldShape>>,
    val unchanged: List<FieldShape>,
) {
    /** True when nothing changed structurally (caller can skip migration entirely). */
    val isEmpty: Boolean
        get() = added.isEmpty() && removed.isEmpty() && kindChanged.isEmpty()
}

fun FormSchema.shapes(): List<FieldShape> = fields().map { FieldShape(it.name, it.kind()) }

/** Compute the structural diff from [from] to [to], keyed by field name. */
fun diffSchemas(
    from: FormSchema,
    to: FormSchema,
): SchemaDiff {
    val fromByName = from.shapes().associateBy { it.name }
    val toByName = to.shapes().associateBy { it.name }

    val added = toByName.values.filter { it.name !in fromByName }
    val removed = fromByName.values.filter { it.name !in toByName }
    val kindChanged =
        toByName.values.mapNotNull { now ->
            val before = fromByName[now.name]
            if (before != null && before.kind != now.kind) before to now else null
        }
    val unchanged =
        toByName.values.filter { now ->
            fromByName[now.name]?.kind == now.kind
        }
    return SchemaDiff(added = added, removed = removed, kindChanged = kindChanged, unchanged = unchanged)
}
