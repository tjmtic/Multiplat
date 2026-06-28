# 🏛️ Architecture

Multiplat follows a clean, modular architecture designed for the Kotlin Multiplatform ecosystem.

## 📦 Module Structure

The libraries layer cleanly — each depends only on the one below it, so you can adopt just the forms, forms+persistence, or the whole stack.

- **`:composeforms`**: The core library. Form logic, the type-safe DSL, the model (`FormSchema`/`FormField`), and the Compose Multiplatform renderer. Pure KMP, Android + iOS.
- **`:composeforms-persistence`**: Local persistence. A `FormStore` port with a SQLDelight adapter; schema *fingerprinting* and a JSON codec that reconciles stored data to the current schema on read. `api`-depends on `:composeforms`.
- **`:composeforms-migration`**: On-device migration generation. An `LlmEngine` (Android: MediaPipe) produces a declarative, sandbox-validated `TransformSpec` for the ambiguous changes a pure reconcile can't infer. `api`-depends on `:composeforms-persistence`.
- **`:composeApp`**: The sample application. The form examples plus an end-to-end OTA Migration Demo.

```
:composeApp
    │  uses
    ▼
:composeforms-migration ──▶ :composeforms-persistence ──▶ :composeforms
   (LLM transforms)            (FormStore / SQLite)         (DSL + renderer)
```

## 💾 Data flow: OTA schema evolution

The key insight: the UI is already schema-driven, so the only thing that must adapt when a backend changes a structure is **storage**. Objects are stored as a JSON envelope `(type, id, schemaVersion, json, updatedAt)`; `schemaVersion` is a structural **fingerprint** (order-independent hash of each field's name + kind).

```
write:  FormState map ──encode(schema)──▶ JSON envelope ──▶ SQLite (SQLDelight)

read:   SQLite row ──▶ storedFingerprint == currentFingerprint ?
            │ yes → decode(currentSchema)                         ── structural reconcile
            └ no  → TransformRegistry.find(stored, current) ?
                       │ hit  → apply validated transform, then decode
                       └ miss → decode(currentSchema)             ── add=default, remove=dropped
```

- **Add / remove / reorder** fields → handled entirely by `decode` walking the current schema. No migration.
- **Rename / merge / type-coerce** → an `LlmMigrator.prepare(old, new)` step (run once when a new schema is adopted) generates a `TransformSpec`, validates it in a sandbox against real samples, and caches it. The read path then finds it synchronously — the LLM is **never** on the hot path.

## 🛠️ Tech Stack

- **Kotlin Multiplatform**: Shared logic and UI.
- **Compose Multiplatform**: Declarative UI for all platforms.
- **Material 3**: Design system.
- **SQLDelight**: Typed, multiplatform SQLite for the persistence envelope.
- **kotlinx.serialization**: JSON codec for stored values and the transform spec.
- **MediaPipe LLM Inference** (Android): on-device model for migration generation.
- **Gradle**: Build automation.

## 🧩 Core Patterns

### DSL-Driven Configuration
Forms are defined using a type-safe Kotlin DSL, allowing for readable and maintainable form structures.

### Observer Pattern
Form state is managed using Compose `State` objects, ensuring the UI stays in sync with the underlying data automatically.

### Port-Adapter Strategy
While currently focused on Compose UI, the core logic is separated to allow for potential adaptation to other UI frameworks or platform-specific implementations where necessary. The same strategy drives persistence (`FormStore` port; SQLDelight + in-memory adapters) and migration (`LlmEngine` port; MediaPipe adapter on Android, an in-memory/fake engine in tests).

### Fingerprint as Implicit Type Version
`FormSchema.fingerprint()` derives a stable structural identity from the field set — no manual version numbers. The same structure always yields the same fingerprint across launches and platforms; any structural change yields a different one. This is what lets stored data be matched to (and reconciled against) a schema it was *not* written with.

### Declarative-Transform Safety Model
The on-device LLM never emits executable code. It emits a declarative `TransformSpec` (renames / drops / constants) which trusted code interprets. A malformed or hostile response can at worst produce a bad *mapping*, which the `MigrationValidator` rejects by applying it to real samples in a sandbox and checking that fields common to both schemas are preserved — it can never run arbitrary logic on the device or touch the live DB unvalidated.
