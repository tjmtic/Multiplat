# Multiplat

**Multiplat** is the Kotlin Multiplatform successor to the `ComposeForms` library. It provides a powerful, type-safe DSL for building beautiful, responsive forms that run seamlessly on **Android** and **iOS** — and now a **local persistence + over-the-air (OTA) schema-evolution** stack so a backend can change a form's structure and existing on-device data keeps working **without an app update**.

## ✨ Features

- 🚀 **Kotlin Multiplatform**: 100% shared code for logic and UI.
- 📝 **Type-Safe DSL**: Define complex forms with simple, readable Kotlin code.
- 🎨 **Compose Multiplatform**: Native performance and look-and-feel on all platforms.
- ✅ **Built-in Validation**: Powerful validation engine with extensible rules.
- 📱 **Responsive Design**: Adapts beautifully to different screen sizes.
- 💾 **Dynamic persistence**: Save form objects to a local SQLite DB (SQLDelight) as a JSON envelope, reconciled to the live schema on read — add/remove/reorder fields needs **zero migration**.
- 🤖 **On-device LLM migrations**: For the *ambiguous* changes (renames, backfills), an embedded model proposes a **declarative, sandbox-validated** transform — no app update, no cloud round-trip.

## 📦 Modules

| Module | Responsibility |
|---|---|
| **`:composeforms`** | The type-safe form DSL, model (`FormSchema`/`FormField`), and Compose Multiplatform renderer. |
| **`:composeforms-persistence`** | `FormStore` over SQLDelight; schema fingerprinting + JSON codec that reconciles stored data to the current schema. |
| **`:composeforms-migration`** | On-device LLM (`LlmEngine`) that generates + validates `TransformSpec`s for the ambiguous schema changes a pure reconcile can't infer. |
| **`:composeApp`** | Sample app: the form examples plus an end-to-end **OTA Migration Demo**. |

## 📖 Documentation

- [🚀 Getting Started](GETTING_STARTED.md)
- [📖 Help Reference](HELP_REFERENCE.md)
- [🏛️ Architecture](ARCHITECTURE.md)
- [🤝 Contributing](CONTRIBUTING.md)
- Module guides: [persistence](composeforms-persistence/README.md) · [migration](composeforms-migration/README.md)

## 🛠️ Quick Start

### 1. Define your form

```kotlin
val loginForm = remember {
    form {
        section("Login") {
            text("username") {
                label = "Username"
                required("Username is required")
            }
            password("password") {
                label = "Password"
                required("Password is required")
            }
        }
        submitButton("Login")
    }
}
```

### 2. Render it in Compose

```kotlin
RenderForm(
    form = loginForm,
    context = rememberFormContext(loginForm)
)
```

### 3. Persist data — and survive backend schema changes

```kotlin
// Android (iOS: DriverFactory() with no Context)
val registry = CachingTransformRegistry()
val store = createFormStore(DriverFactory(context), registry)

// Save a form snapshot (the FormState map) under a typed key.
store.put(ObjectKey("Contact", "1"), schemaV1, context.values.value)

// Backend later renames a field. Generate + validate a transform ONCE, on-device:
LlmMigrator(MediaPipeLlmEngine(context, modelPath))
    .prepareInto(registry, from = schemaV1, to = schemaV2, samples = recentRows)

// Reads now reconcile old data to the new schema automatically — no app update.
val values: Map<String, Any?>? = store.get(ObjectKey("Contact", "1"), schemaV2)
```

Additive/removal/reorder changes need none of the migration step — `store.get` reconciles them on its own. See the runnable **OTA Migration Demo** in `:composeApp`.

## 📦 Installation

*Installation instructions for Maven Central coming soon.*

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.