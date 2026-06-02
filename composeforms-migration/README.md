# composeforms-migration

On-device LLM migration generation for **Multiplat** — fills the `TransformRegistry` seam left by
`:composeforms-persistence` so that the *ambiguous* schema changes a pure reconcile can't infer
(renames, field merges/splits, backfills) are handled automatically, without an app update.

## Key idea: the model proposes, trusted code disposes

The LLM never emits executable code. It emits a **declarative `TransformSpec`** (renames / drops /
constants) which trusted, deterministic code interprets into a `SemanticTransform`. A malformed or
hostile response can at worst produce a bad *mapping*, which is then caught by validation — it can
never run arbitrary logic on the device.

## Flow

```
adopt new backend schema
   │
   ▼
LlmMigrator.prepareInto(registry, oldSchema, newSchema, samples)
   │   1. diffSchemas(old, new)               ─ deterministic structural delta
   │   2. MigrationPrompt.build(...)           ─ constrained "emit only a TransformSpec JSON"
   │   3. engine.generate(prompt)              ─ on-device model (Android: MediaPipe)
   │   4. parse -> TransformSpec -> transform
   │   5. MigrationValidator.validate(...)     ─ sandbox: apply to samples, decode vs target,
   │                                             reject common-field data loss / decode failure
   │   └─ retry up to maxAttempts, else give up (store falls back to pure reconcile)
   ▼
CachingTransformRegistry  ── find(fromFp, toFp) is a pure sync lookup; the LLM is NEVER on the read path
   ▼
SqlDelightFormStore(db, registry)  ── reads now apply the validated transform before reconcile
```

## Pieces

| Type | Role |
|---|---|
| `LlmEngine` | text-in/text-out contract; the only platform-specific dependency |
| `MediaPipeLlmEngine` (androidMain) | on-device inference via `com.google.mediapipe:tasks-genai` |
| `TransformSpec` | serializable, declarative migration; `toTransform()` interprets it |
| `diffSchemas` / `SchemaDiff` | deterministic structural delta between two `FormSchema`s |
| `MigrationPrompt` | builds the constrained, JSON-only prompt |
| `MigrationValidator` | sandbox safety gate (no live DB) |
| `LlmMigrator` | orchestrates generate → parse → validate → retry |
| `CachingTransformRegistry` | validated transforms keyed by fingerprint pair; sync `find` |

## Status

- ✅ Core pipeline + Android `MediaPipeLlmEngine` (14 tests green via `testDebugUnitTest`).
- ⬜ **iOS engine** — an `LlmEngine` impl (llama.cpp via cinterop, or Foundation Models). The module
  already compiles for iOS; only a concrete engine is missing.
- ⬜ Model provisioning helper (bundle/download the `.task` model to device storage).
- ⬜ Richer `TransformSpec` ops (concat/split, value maps) as real migrations demand them.

## Android usage (sketch)

```kotlin
val engine = MediaPipeLlmEngine(context, modelPath = "/data/.../gemma.task")
val registry = CachingTransformRegistry()
LlmMigrator(engine).prepareInto(registry, oldSchema, newSchema, samples = recentRows)
val store = SqlDelightFormStore(db, registry)   // reads now self-heal across the rename
```
