# 🏛️ Architecture

Multiplat follows a clean, modular architecture designed for the Kotlin Multiplatform ecosystem.

## 📦 Module Structure

- **`:composeforms`**: The core library. Contains all form logic, DSL, and UI components. Pure KMP module targeting Android and iOS.
- **`:composeApp`**: The sample application. Demonstrates how to use the `:composeforms` library.

## 🛠️ Tech Stack

- **Kotlin Multiplatform**: Shared logic and UI.
- **Compose Multiplatform**: Declarative UI for all platforms.
- **Material 3**: Design system.
- **Gradle**: Build automation.

## 🧩 Core Patterns

### DSL-Driven Configuration
Forms are defined using a type-safe Kotlin DSL, allowing for readable and maintainable form structures.

### Observer Pattern
Form state is managed using Compose `State` objects, ensuring the UI stays in sync with the underlying data automatically.

### Port-Adapter Strategy
While currently focused on Compose UI, the core logic is separated to allow for potential adaptation to other UI frameworks or platform-specific implementations where necessary.
