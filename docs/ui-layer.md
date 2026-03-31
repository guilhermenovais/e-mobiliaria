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

## JavaFX-specific

- Prefer `VBox/HBox/GridPane` over absolute positioning.
- Use bindings instead of manual UI updates.
- Use the JMetro theme.
- CSS:
    - centralize theme
    - avoid inline styles
