# 🤝 Contributing to Multiplat

First off, thank you for considering contributing to Multiplat! 

## 📜 Code of Conduct
Please be respectful and professional in all interactions. We aim to foster an inclusive and welcoming environment.

## 🛠️ Developing

### Coding Standards
We use **Spotless** (with ktlint) and **Detekt** to maintain code quality. Your code must pass these checks before a PR can be merged.

- **Check formatting**: `./gradlew spotlessCheck`
- **Apply formatting**: `./gradlew spotlessApply`
- **Check code quality**: `./gradlew detekt`

### Git Workflow
1. **Fork** the repository and create your branch from `main`.
2. **Commit** your changes with clear, descriptive messages.
3. **Verify** your changes by running `./gradlew check`.
4. **Push** to your fork and submit a **Pull Request**.

## 🧪 Testing Requirements
- **New Features**: Must include unit tests in the `:composeforms` module.
- **UI Changes**: Should include manual verification on both Android and iOS.
- **Bug Fixes**: Should include a regression test that fails without the fix.

## 📝 Documentation
If you're adding or changing an API, please update the corresponding documentation:
- Update KDocs in the code.
- Update `HELP_REFERENCE.md` if needed.
- Update `ARCHITECTURE.md` for major architectural shifts.
