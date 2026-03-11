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
