# Milestone 1 — Architectural Decisions

## Border Alpha Standardization

**Context:** Different screens used different alpha values for card borders (0.12f, 0.15f, 0.18f, 0.2f, 0.25f, 0.3f).

**Decision:** Use `MaterialTheme.colorScheme.outlineVariant` for all card borders. This is a Material 3 token that automatically adapts to light/dark themes and provides consistent visual weight.

**Consequences:** All hardcoded `BorderStroke(1.dp, color.copy(alpha = Xf))` should be replaced with `BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)`.

## Elevation Token Usage

**Context:** Cards across the app used hardcoded `shadowElevation = 2.dp` or `4.dp`.

**Decision:** Use `AppElevations.*` tokens consistently:
- `AppElevations.low` (2.dp) for filter bars and flat UI elements
- `AppElevations.cardRest` (3.dp) for cards at rest
- `AppElevations.cardRaised` (6.dp) for cards with emphasis
- `AppElevations.cardHovered` (10.dp) for hovered cards

**Consequences:** All hardcoded elevation values must be replaced with the corresponding token.

## Button Standardization

**Context:** Some screens used `FilledTonalButton` or `TextButton` instead of the custom `AppButton`/`AppOutlinedButton`.

**Decision:** All buttons must use `AppButton` or `AppOutlinedButton` which include consistent minHeight (44dp), shape (AppShapes.medium), and hover/press animations.

**Consequences:** `FilledTonalButton` and `TextButton` should be replaced across Doctors, Stock, Payments, and PatientDetail screens.

## Card Style Standardization

**Context:** Doctors and Stock screens used `ElevatedCard` instead of `AppCard` or `Surface + AppShapes.medium + BorderStroke`.

**Decision:** All cards must use either `AppCard` (with optional hoverableCard modifier) or `Surface` with `AppShapes.medium` and `BorderStroke(1.dp, outlineVariant)`.

**Consequences:** `ElevatedCard` should be replaced across Doctors and Stock screens.
