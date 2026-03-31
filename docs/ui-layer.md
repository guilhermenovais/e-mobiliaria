# UI Layer instructions

- The UI layer should call only the application layer. It should never use services or repositories directly.
- The UI should be tested using TestFX
- All texts displayed on the UI should be translated (translation message files are located in `src/main/resources/`)

## Feedback

- Every action must respond:
    - success → subtle confirmation
    - error → clear + actionable
- Use toast for non-critical
- Use modal only when blocking decision required

## States

- Design all states:
    - loading (skeleton > spinner)
    - empty
    - error
    - success

## Consistency

- Same action → same position everywhere.
- Primary action always same color/location.
- Icons consistent (don’t mix sets).

## Component Placement

- **Shared components** (`shared/ui/component/`) — generic, reusable across features (e.g., `WizardStepperBar`).
  Must have no feature-specific logic or imports.
- **Feature components** (`<feature>/ui/component/`) — reusable within one feature but not across features
  (e.g., `PhysicalPersonFormPane`, `AddressFormPane`).
- Do **not** put feature-specific components in `shared/`. If a component from one feature is needed in another,
  move it to `shared/` and strip any feature-specific logic.

## CSS Splitting

- Global stylesheets live in `shared/ui/style/`:
  - `forms.css` — `.form-label`, `.form-input*`, `.form-combo`, `.form-hint`, `.form-error-label`
  - `buttons.css` — `.btn-primary*`, `.btn-secondary*`
  - `stepper.css` — `.stepper-dot*`, `.stepper-label*`, `.stepper-connector*`, `.setup-stepper`
- Feature/screen-specific layout styles stay in the feature's view directory (e.g., `initial-setup.css`).
- Every FXML that uses shared classes must reference all relevant shared CSS files via `<stylesheets>`.
- Never duplicate a rule across files — add it once in the most appropriate stylesheet.

## JavaFX-specific

- Prefer `VBox/HBox/GridPane` over absolute positioning.
- Use bindings instead of manual UI updates.
- Use the JMetro theme.
- CSS:
    - centralize theme
    - avoid inline styles
