# UI Layer instructions

- The UI layer should call only the application layer. It should never use services or repositories directly.
- The UI should be tested using TestFX
- All texts displayed on the UI should be translated (translation message files are located in `src/main/resources/`)

## Error Handling & Logging

- Controllers must never build `Alert` dialogs inline — delegate to `ErrorHandler.handle(t, bundle)`.
- `ErrorHandler` logs the exception via SLF4J and shows a localized Alert on the FX thread.
- Use a `private static final Logger log = LoggerFactory.getLogger(Foo.class)` in every controller.
- Log level guide: `ERROR` for unexpected failures, `WARN` for recoverable/expected failures (e.g. network), `INFO` for
  lifecycle events.

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

## Form Input Masks

- Use `MaskedTextField` (`shared/ui/component/`) for any field with a fixed digit format (CPF, CNPJ, CEP, phone, etc.).
- Mask syntax: `0` = digit slot, any other character = literal (e.g. `"000.000.000-00"`).
- Always call `getValue()` (digits only) when passing the value to the application layer — never `getText()`.

## Form Validation

- **Real-time (focus-loss):** fields backed by a domain rule (CPF, CNPJ) must validate on focus-loss via their
  corresponding `Validate*Interactor`. Show the error inline (label below the field) — never an Alert dialog.
- **On submit:** `validate()` in every form pane must re-run all checks, including domain rules, and aggregate
  results into a single boolean. Navigation is blocked if `validate()` returns `false`.
- **Error display:** add a `Label` with style class `form-error-label` directly below the field (`managed=false`
  when hidden). Toggle `form-input-error` on the field and show/hide the label together.
- Domain validation must flow through an application-layer use case (e.g. `ValidateCpfInteractor`) that returns
  `Optional<ErrorMessage>`. The UI resolves the translation key via the resource bundle.

## JavaFX-specific

- Prefer `VBox/HBox/GridPane` over absolute positioning.
- Use bindings instead of manual UI updates.
- Use the JMetro theme.
- CSS:
    - centralize theme
    - avoid inline styles
