package com.abyxcz.multiplat.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.abyxcz.composeforms.migration.diffSchemas
import com.abyxcz.composeforms.persistence.FormValuesCodec
import com.abyxcz.composeforms.persistence.ObjectKey
import com.abyxcz.composeforms.persistence.fields
import com.abyxcz.composeforms.persistence.fingerprint
import com.abyxcz.composeforms.ui.v2.RenderForm
import com.abyxcz.v2core.core.model.FormContext
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

private val CONTACT = ObjectKey(type = "Contact", id = "1")

/**
 * Demonstrates the full stack: the backend changes a structure (SampleBackend versions), the data
 * is saved locally, and on reload under the new structure it survives — additive changes for free,
 * the rename via a validated, cached LLM transform.
 */
@Composable
@Suppress("ktlint:standard:function-naming")
fun MigrationDemoScreen(env: DemoEnvironment) {
    val scope = rememberCoroutineScope()
    var versionIndex by remember { mutableStateOf(0) }
    var context by remember { mutableStateOf(FormContext(mutableStateOf(emptyMap()))) }
    var status by remember { mutableStateOf("Pick a version, edit, Save — then switch versions to see data survive.") }

    val current = SampleBackend.versions[versionIndex]

    // Reload persisted data whenever the active schema changes; seed defaults if nothing stored yet.
    LaunchedEffect(versionIndex) {
        val loaded = env.store.get(CONTACT, current.schema)
            ?: current.schema.fields().associate { it.name to it.value }
        context = FormContext(mutableStateOf(loaded))
    }

    fun switchTo(target: Int) {
        if (target == versionIndex) return
        val from = current.schema
        val to = SampleBackend.versions[target].schema
        scope.launch {
            // 1) Persist current edits under the structure they were entered with.
            env.store.put(CONTACT, from, context.values.value)
            // 2) Ambiguous change (rename / type change)? Generate + validate + cache a transform.
            val diff = diffSchemas(from, to)
            status =
                if ((diff.removed.isNotEmpty() || diff.kindChanged.isNotEmpty()) &&
                    !env.registry.has(from.fingerprint(), to.fingerprint())
                ) {
                    val sample = Json.parseToJsonElement(FormValuesCodec.encode(from, context.values.value)).jsonObject
                    if (env.migrator.prepareInto(env.registry, from, to, listOf(sample))) {
                        "LLM transform prepared & cached for ${from.fingerprint().take(6)} → ${to.fingerprint().take(6)}"
                    } else {
                        "No transform validated — falling back to pure reconcile."
                    }
                } else {
                    "Additive/structural change — pure reconcile, no LLM needed."
                }
            // 3) Switch; LaunchedEffect reloads the stored data under the new structure.
            versionIndex = target
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("OTA Schema Migration", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SampleBackend.versions.forEachIndexed { i, v ->
                if (i == versionIndex) {
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors()) { Text(v.label) }
                } else {
                    OutlinedButton(onClick = { switchTo(i) }) { Text(v.label) }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Backend ${current.label}", style = MaterialTheme.typography.titleMedium)
                Text(current.note, style = MaterialTheme.typography.bodySmall)
                Text(
                    "fingerprint ${current.schema.fingerprint().take(12)}…",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        HorizontalDivider()

        // The dynamic form for the current backend structure.
        RenderForm(form = current.schema, context = context)

        Button(
            onClick = {
                scope.launch {
                    env.store.put(CONTACT, current.schema, context.values.value)
                    status = "Saved under ${current.label}: ${context.values.value}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save to local DB") }

        HorizontalDivider()

        Text("Loaded values", style = MaterialTheme.typography.labelLarge)
        Text(context.values.value.toString(), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}
