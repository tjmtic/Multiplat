# 🚀 Getting Started

Welcome to **Multiplat**! This guide will help you set up the project locally and start building multiplatform forms.

## 🛠️ Prerequisites
- **Android Studio** (Koala or newer)
- **JDK 17+**
- **Android SDK 34+**
- **macOS** (for iOS development)

## 🏗️ Project Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abyxcz/Multiplat.git
   cd Multiplat
   ```

2. **Open in Android Studio**:
   Open the root directory and wait for Gradle synchronization to finish.

3. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Running Tests

### Unit Tests
Run all unit tests across all modules (Android and iOS):
```bash
./gradlew test
```

Per-module (fast JVM path) for the persistence and migration logic:
```bash
./gradlew :composeforms-persistence:testDebugUnitTest
./gradlew :composeforms-migration:testDebugUnitTest
```

### UI Tests
Run the sample app on an emulator or physical device. The **OTA Migration Demo** tab walks
through saving data, switching backend schema versions, and watching the data survive a rename.

---

## 🎨 Design Guidelines

When adding new fields to the `:composeforms` module:
- **Consistency**: Use `MaterialTheme` color schemes.
- **Multiplatform**: Ensure any UI logic works on both Android and iOS.
- **Accessibility**: Use appropriate semantics and labels.

---

## 🤖 On-Device LLM (optional)

The migration generator runs without any model out of the box — the sample app uses a
`StubLlmEngine` that returns a canned transform. To run a **real** on-device model on Android:

1. Provision a MediaPipe-compatible `.task` model (e.g. a quantized Gemma) onto the device
   (bundle as an asset and copy to internal storage, or download).
2. Pass `MediaPipeLlmEngine(context, modelPath)` to `LlmMigrator` instead of `StubLlmEngine`.

The `:composeforms-persistence` and `:composeforms-migration` logic is fully testable on the JVM
with no model or device required.

## 📦 Usage Example

For a quick reference on how to use the DSL, see the [main README](README.md).
Check out the sample code in `composeApp/src/commonMain/kotlin/com/abyxcz/multiplat/App.kt`, and the
end-to-end persistence + migration flow in `composeApp/src/commonMain/kotlin/com/abyxcz/multiplat/demo/`.
