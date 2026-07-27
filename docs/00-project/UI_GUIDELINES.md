# UI Guidelines

## Design System Tokens

- **Elevations**: `AppElevations.*` (none=0dp, hairline=1dp, low=2dp, cardRest=3dp, cardRaised=6dp, cardHovered=10dp, dialog=12dp, fab=8dp)
- **Spacing**: `AppSpacing.*` (xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp)
- **Shapes**: `AppShapes.*` (small=12dp, medium=16dp, large=24dp, extraLarge=32dp)
- **Typography**: `AppTypography.*` (PageTitle, SectionTitle, CardTitle, Body, BodySmall, Caption, ButtonText, MetricValue, MetricLabel)
- **Buttons**: `AppButton` (filled), `AppOutlinedButton` (outlined), both minHeight=44dp
- **Cards**: Use `Surface` with borders, NOT `ElevatedCard`
- **Borders**: `outlineVariant` for card borders
- **Animations**: `AppAnimations.smoothTween()` / `AppAnimations.FocusDurationMs`

## Colors

- Use `MaterialTheme.colorScheme.*` for standard Material colors
- Use `PatientsPremiumPalette.*` for custom brand colors (primary=#6C63FF, background=#F5F7FB, etc.)
- Avoid hardcoded `Color(0xFF...)` values

## Screen Patterns

- Loading state: `Box(LoadingIndicator)` while data loads
- Empty state: icon + message + action button
- Error state: inline error message with retry
- Dialogs: `AppSurfaceDialog` for complex forms, `AppBasicDialog` for confirmations
- Page header: title + description + primary action button

## Accessibility

- All `IconButton` must have `contentDescription`
- Icons inside buttons (leadingIcon) use `null` contentDescription (button text conveys action)
- Contrast ratio sufficient for all text/background combinations
