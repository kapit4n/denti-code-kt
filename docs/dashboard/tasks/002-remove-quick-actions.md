# Task 002: Remove Dashboard Quick Actions Card

## Objective

Remove the `DashboardQuickActionsCard` composable from the dashboard. This card duplicated the quick action buttons already present in the top bar (Nueva cita, Nuevo paciente, Nuevo pago), creating visual clutter and redundant navigation paths.

## Motivation

The top bar already provides three quick action buttons:
- "Nueva cita" → navigates to Appointments
- "Nuevo paciente" → navigates to Patients
- "Nuevo pago" → navigates to Payments

The dashboard's `DashboardQuickActionsCard` offered 6 buttons (the same 3 plus "Nuevo tratamiento", "Buscar paciente", "Reporte diario"), but:
1. The primary actions (cita, paciente, pago) were redundant
2. The secondary actions (tratamiento, buscar, reporte) were low-usage and could be accessed via sidebar
3. The card consumed valuable lower-row space that could be better used by the status and alerts cards

## Files Modified

- `src/.../ui/dashboard/DashboardComponents.kt`

## Components Added

None.

## Components Updated

- `ResponsiveDashboardGrid` — lower row now contains 2 cards (Status + Alerts) instead of 3

## Components Removed

- `DashboardQuickActionsCard` composable (was lines 503-551)
- `QuickActionDef` private data class (was lines 553-557)
- `DashboardQuickActionButton` private composable (was lines 559-593)

## UI Changes

- **Before**: Dashboard lower row had 3 cards of equal width (Status, Alerts, Quick Actions)
- **After**: Dashboard lower row has 2 cards of equal width (Status, Alerts)
- The status donut chart and alerts card now each take 50% of the lower row width instead of 33%
- No functional loss: all navigation actions remain accessible via the top bar and sidebar

## Technical Changes

- Removed 3 unused imports: `clickable`, `Icons.Outlined.Assessment`, `Icons.Outlined.Search`
- File reduced from 722 lines to 624 lines (98 lines removed)
- `onNavigate` parameter in `ResponsiveDashboardGrid` is retained (still used by `DashboardAppointmentsCard`)

## Screenshots

- `dashboard-lower-row-before.png` — 3 cards in lower row
- `dashboard-lower-row-after.png` — 2 cards in lower row (wider status + alerts)

## Notes

- The quick actions in the top bar are always visible regardless of which screen is active (except Appointments, which has its own toolbar)
- Future improvement: consider adding a "create" FAB or floating action for primary actions
