# 📖 Help Reference

A detailed guide to the primary APIs and components in Multiplat.

## 🗝️ Core API Reference (`:composeforms` module)

The `:composeforms` module provides the multiplatform foundation for form definitions.

### `FormSchema<ResultType>`
The blueprint for a form.
- **`formName`**: Name of the form (used for IDs/headers).
- **`groups`**: List of `InputFieldGroup`.
- **`buildResult`**: A lambda that converts the field map into your desired result type.
- **`formValidator`**: A global validator for cross-field rules.

### `FormRunner<ResultType>`
The execution engine for a `FormSchema`.
- **`isValid()`**: Returns true if all field, group, and schema rules pass.
- **`fieldErrors()`**: Returns a map of field IDs to their current error messages.
- **`buildResult()`**: Executes the schema's result builder.

### `InputFieldDescriptor<T>`
Base class for defining fields.
- **Subtypes**: `TextFieldDescriptor`, `IntFieldDescriptor`, `BooleanFieldDescriptor`, `DropdownFieldDescriptor`, etc.
- **`validators`**: A list of `(T) -> String?` lambdas.

---

## ⚡ v2Core API Reference

The next-gen engine with a richer DSL.

### `FormBuilder`
The entry point for the DSL.
- **`text(name, block)`**: Adds a `TextField`.
- **`checkbox(name, block)`**: Adds a `CheckboxField`.
- **`section(title, block)`**: Groups fields under a title.

### `FormContext`
Holds the live state of a v2 form.
- **`values`**: `MutableState<Map<String, Any?>>`
- **`errors`**: `MutableState<Map<String, String?>>`

---

## 🎨 UI Component Reference

### `RenderForm(form, context)`
The main entry point for rendering a v2 form in Compose Multiplatform.
- Automatically handles field visibility and layout.
- Uses `FieldRenderer` internally for each field type.

### `FormSubmitButton(label, form, context, onSubmit)`
A pre-configured button that:
1. Validates the form.
2. Updates error states if invalid.
3. Calls `onSubmit` with values if valid.

---

## 🛠️ Validation Rules

- **`RequiredRule(message)`**: Ensures a field is not null or blank.
- **`MinLengthRule(min, message)`**: Ensures a string meets a minimum length.
- **`ValidationRule<T>`**: Interface for creating custom rules. Implement `validate(value: T?): String?`.

---

## 💾 Persistence API Reference (`:composeforms-persistence` module)

Stores form objects as a JSON envelope and reconciles them to the current schema on read.

### `FormStore`
The persistence port. Reads require the current `FormSchema` (it supplies types and defaults).
- **`put(key, schema, values)`**: Persist a `FormState` snapshot (`Map<String, Any?>`).
- **`get(key, schema)`**: Load one object, reconciled + coerced to `schema` (or null).
- **`getByType(type, schema)`**: Load every object of a type.
- **`delete(key)`**: Remove an object.

### `ObjectKey(type, id)`
Stable identity for a stored instance — `type` is the object kind, `id` the instance.

### `createFormStore(factory, transforms)` / `DriverFactory`
Builds a SQLDelight-backed `FormStore`. `DriverFactory(context)` on Android, `DriverFactory()` on iOS. `transforms` defaults to `TransformRegistry.Empty`.

### `InMemoryFormStore(transforms)`
Reference store with identical behavior — for tests, previews, and ephemeral caches.

### `FormSchema.fingerprint()`
A stable, order-independent structural hash (field name + kind) — the *implicit type version* used as `schemaVersion`.

### `FormValuesCodec`
`encode(schema, values)` → JSON; `decode(schema, json)` → reconciled map (added field → default, removed field → dropped, dropdowns by stable option key).

### `SemanticTransform` / `TransformRegistry`
`SemanticTransform` maps stored JSON to JSON valid for the target schema. `TransformRegistry.find(fromVersion, toVersion)` returns one (or null for a pure reconcile). `TransformRegistry.Empty` is the default.

---

## 🤖 Migration API Reference (`:composeforms-migration` module)

Generates the ambiguous transforms (renames, backfills) using an on-device LLM.

### `LlmEngine`
`fun interface { suspend fun generate(prompt): String }`. The only platform-specific dependency.
- **`MediaPipeLlmEngine(context, modelPath, maxTokens)`** (Android): on-device inference via `com.google.mediapipe:tasks-genai`. `close()` releases native resources.
- **`SwiftLlmEngine(generator)`** (iOS): bridges to a Swift completion-handler closure, so the Swift app owns the model (Foundation Models / MLX / llama.cpp). Inject via the `MainViewController(llmGenerate:)` overload.

### `TransformSpec`
The declarative migration the model emits (never code): `renames` (old→new), `drops`, `constants` (backfills). `toTransform()` interprets it into a `SemanticTransform`.

### `diffSchemas(from, to)` → `SchemaDiff`
Deterministic structural delta: `added`, `removed`, `kindChanged`, `unchanged`, `isEmpty`.

### `MigrationPrompt.build(from, to)`
Builds the constrained, JSON-only prompt from the diff.

### `MigrationValidator`
Sandbox safety gate. Accepts a candidate only if it decodes against the target and every field common to both schemas is **preserved** (no silent data loss). Never touches the live DB.

### `LlmMigrator(engine, validator, maxAttempts)`
- **`prepare(from, to, samples)`**: generate → parse → validate → retry; returns a `SemanticTransform` or null.
- **`prepareInto(registry, from, to, samples)`**: as above, then registers the result. Returns whether one validated.

### `CachingTransformRegistry`
A `TransformRegistry` of validated transforms keyed by `(fromFingerprint, toFingerprint)`. `find` is a pure synchronous lookup, so the LLM never runs on the read path.
