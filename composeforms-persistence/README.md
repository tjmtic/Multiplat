# composeforms-persistence

Dynamic local persistence for the **Multiplat** forms library. Stores form objects whose
structure is defined by a `FormSchema` (v2 `com.abyxcz.v2core.core.model`), and keeps working
when the backend evolves that structure **without an app update**.

## Why it exists

The forms library already renders any schema dynamically, so new backend fields display with no
code change. The gap was the **storage** layer: this module closes it. Objects are stored as a
JSON envelope, and reads reconcile the stored data to the *current* schema — so adding, removing,
or reordering fields needs **no migration at all**.

## Design

```
ViewModel ──(FormState snapshot: Map<String, Any?>)──▶ FormStore.put(key, schema, values)
                                                            │
                                                  encode via FormSchema ──▶ JSON envelope
                                                            ▼
                                          (type, id, schemaVersion, json, updatedAt)
                                                            │
ViewModel ◀──(reconciled Map)── FormStore.get(key, schema) ─┘
                                   │
                                   ├─ schemaVersion == current?  → decode against current schema
                                   └─ differs? → TransformRegistry.find(from, to)?  (optional)
                                                   ├─ hit  → apply semantic transform, then decode
                                                   └─ miss → pure structural reconcile
```

- **`FormStore`** — the persistence port. Reads require the current `FormSchema`.
- **`SchemaFingerprint`** — `FormSchema.fingerprint()` is the *implicitly defined type version*:
  a stable hash of field name+kind, order-independent. Same structure → same fingerprint.
- **`FormValuesCodec`** — JSON encode/decode. Decode walks the current schema, so it also
  reconciles (missing field → default, unknown stored field → dropped). Dropdowns persist by
  stable option key.
- **`InMemoryFormStore`** — reference implementation; stores the same envelope the SQLDelight
  adapter will, so behavior is identical. Good for tests/previews.
- **`SemanticTransform` / `TransformRegistry`** — optional seam for the *ambiguous* changes a
  reconcile can't infer (renames, splits/merges, coercions, backfills). This is where the
  on-device LLM migration generator ("dbmigration") plugs in. Defaults to `Empty` (pure reconcile).

## What's handled with zero LLM

| Backend change            | Needs a transform? |
|---------------------------|--------------------|
| Add a field               | No — defaults in   |
| Remove a field            | No — dropped       |
| Reorder fields/sections   | No — fingerprint is order-independent |
| Rename / split / merge    | Yes — `SemanticTransform` |
| Type change w/ data shift | Yes — `SemanticTransform` |

## Roadmap

1. ✅ Core: port, fingerprint, codec, in-memory store, optional transform seam.
2. ✅ SQLDelight adapter (`(type, id, schemaVersion, json, updatedAt)` envelope) + platform
   drivers (`DriverFactory` expect/actual: `AndroidSqliteDriver` / `NativeSqliteDriver`).
   Build one via `createFormStore(DriverFactory(...))`.
3. ✅ `:composeforms-migration` `TransformRegistry` backed by the on-device LLM (Android via
   MediaPipe), with declarative `TransformSpec` + sandbox validation. iOS engine still TODO.
