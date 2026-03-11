# Multiplat

**Multiplat** is the Kotlin Multiplatform successor to the `ComposeForms` library. It provides a powerful, type-safe DSL for building beautiful, responsive forms that run seamlessly on **Android** and **iOS**.

## ✨ Features

- 🚀 **Kotlin Multiplatform**: 100% shared code for logic and UI.
- 📝 **Type-Safe DSL**: Define complex forms with simple, readable Kotlin code.
- 🎨 **Compose Multiplatform**: Native performance and look-and-feel on all platforms.
- ✅ **Built-in Validation**: Powerful validation engine with extensible rules.
- 📱 **Responsive Design**: Adapts beautifully to different screen sizes.

## 📖 Documentation

- [🚀 Getting Started](GETTING_STARTED.md)
- [📖 Help Reference](HELP_REFERENCE.md)
- [🏛️ Architecture](ARCHITECTURE.md)
- [🤝 Contributing](CONTRIBUTING.md)

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

## 📦 Installation

*Installation instructions for Maven Central coming soon.*

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.