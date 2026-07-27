# TASK-002: Elevation Standardization

**Milestone:** 1 — Professional UI Polish
**Status:** DONE
**Date:** 2026-07-24

## Objective

Replace all hardcoded `shadowElevation = X.dp` values with `AppElevations.*` design tokens across all screens.

## Motivation

Elevation values were inconsistent across screens (1dp, 2dp, 4dp, 6dp, 8dp) with no connection to the design system. Using `AppElevations` tokens ensures visual consistency and makes elevation changes easy to apply globally.

## Files Modified

| File | Changes |
|------|---------|
| `src/.../ui/dashboard/DashboardKpi.kt` | `6.dp → cardRaised`, `2.dp → low` |
| `src/.../ui/patients/PatientListComponents.kt` | `8.dp → cardHovered`, `3.dp → cardRest`, `2.dp → low`, `4.dp → cardRest`, `1.dp → hairline` |
| `src/.../ui/payments/PaymentListComponents.kt` | `6.dp → cardHovered`, `2.dp → low`, `4.dp → cardRest`, `1.dp → hairline` |
| `src/.../ui/inventory/StockListComponents.kt` | `6.dp → cardHovered`, `2.dp → low`, `4.dp → cardRest` |
| `src/.../ui/patientdetail/PatientDetailComponents.kt` | `6.dp → cardHovered`, `4.dp → cardRest`, `3.dp → cardRest`, `2.dp → low`, `0.dp → none` |
| `src/.../ui/doctordetail/ModernDoctorDetailContent.kt` | `4.dp → cardRest`, `2.dp → low` |

## Components Updated

- DashboardKpiCard — KPI card elevation
- PatientMetricCard — animated metric card elevation
- PaymentKpiCard — animated KPI card elevation
- StockSummaryCard — stock summary card elevation
- PatientHeaderCard — patient detail header card
- DoctorProfileCard — doctor profile cards
- AppointmentPanel, PaymentsPanel, TreatmentsPanel — patient detail section cards

## Design Tokens Applied

| Token | Value | Used For |
|-------|-------|----------|
| `AppElevations.hairline` | 1.dp | Table card separators |
| `AppElevations.low` | 2.dp | Filter bars, flat UI elements, card rest state |
| `AppElevations.cardRest` | 3.dp | Cards at rest |
| `AppElevations.cardRaised` | 6.dp | KPI cards, emphasized cards |
| `AppElevations.cardHovered` | 10.dp | Hovered cards |
| `AppElevations.none` | 0.dp | Flat elements, no shadow |

## Verification

- `./gradlew compileKotlin` — BUILD SUCCESSFUL (0 errors)
- `./gradlew check` — BUILD SUCCESSFUL

## Next Task

TASK-003: Border standardization — replace inconsistent `border.copy(alpha = Xf)` with `outlineVariant`
