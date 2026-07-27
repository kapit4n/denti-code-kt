# Milestone 1 — Changelog

All notable changes to the Professional UI Polish milestone.

## TASK-009 — Accessibility Pass
- **Reviewed:** All interactive icons have proper content descriptions

## TASK-008 — Color Token Audit
- **Changed:** Hardcoded `Color(0xFFF5F7FB)` in PatientDetailWindow replaced with `PatientsPremiumPalette.background`

## TASK-007 — Dialog Standardization
- **Changed:** Date picker and time picker `AlertDialog` → `AppBasicDialog`

## TASK-006 — Animation Standardization
- **Changed:** `tween(160)`, `tween(140)`, `tween(180)` → `AppAnimations.smoothTween(AppAnimations.FocusDurationMs)` / `AppAnimations.smoothTween()`

## TASK-005 — Shape Standardization
- **Changed:** `RoundedCornerShape(12.dp)` → `AppShapes.small` across 4 files
- **Changed:** `RoundedCornerShape(16.dp)` → `AppShapes.medium` across 2 files

## TASK-004 — Button Standardization
- **Changed:** `FilledTonalButton` → `AppButton` in DoctorListComponents (×2)
- **Changed:** `FilledTonalButton` → `AppButton` in StockListComponents (×2)
- **Changed:** `TextButton` → `AppOutlinedButton` in PaymentListComponents
- **Changed:** `TextButton` → `AppOutlinedButton` in PatientDetailComponents (×2)

## TASK-003 — Border Standardization
- **Changed:** All `outline.copy(alpha = Xf)` → `MaterialTheme.colorScheme.outlineVariant` across 5 files (26 instances)

## TASK-002 — Elevation Standardization
- **Changed:** All hardcoded `shadowElevation = X.dp` replaced with `AppElevations.*` tokens across 6 files

## TASK-001 — Loading States
- **Added:** LoadingIndicator to all screens while data loads

## Pre-existing (Tasks 006+007)
- **Added:** QuickFilterChip, AppointmentStatusChip, AppointmentHoverActions, KpiCard composables
- **Changed:** AppointmentTimelineCard with 4dp elevation, 2dp primary border, typography hierarchy
- **Changed:** DaySummaryCard to 2x2 KPI grid layout
- **Changed:** Filter toolbar with quick filter chips + doctor dropdown
- **Changed:** Detail panel split into Patient Info + Technical Details collapsible
- **Improved:** Empty state with icon + message + action button on Appointments screen
- **Improved:** Calendar compact layout (32dp cells, 12dp padding)
