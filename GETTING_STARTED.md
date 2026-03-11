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

### UI Tests
Run the sample app on an emulator or physical device.

---

## 🎨 Design Guidelines

When adding new fields to the `:composeforms` module:
- **Consistency**: Use `MaterialTheme` color schemes.
- **Multiplatform**: Ensure any UI logic works on both Android and iOS.
- **Accessibility**: Use appropriate semantics and labels.

---

## 📦 Usage Example

For a quick reference on how to use the DSL, see the [main README](README.md).
Check out the sample code in `composeApp/src/commonMain/kotlin/com/abyxcz/multiplat/App.kt`.
