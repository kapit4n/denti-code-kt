# TASK-001: Add Loading States to All Screens

**Milestone:** 1 — Professional UI Polish
**Status:** DONE
**Date:** 2026-07-24

## Objective

Add `LoadingIndicator` composable to every screen that loads data from the repository. Previously screens showed either nothing (blank/empty) or plain text ("Cargando panel…") while data was being fetched.

## Motivation

Users had no visual feedback when data was loading. Screens would appear blank momentarily, causing confusion about whether the app was working. A loading spinner provides clear feedback and feels more professional.

## Files Modified

| File | Change |
|------|--------|
| `src/.../ui/dashboard/ModernDashboardContent.kt` | Replaced `Text("Cargando panel…")` with `LoadingIndicator()` |
| `src/.../ui/PatientsScreen.kt` | Added `loaded` state + `Box(LoadingIndicator)` wrapper + missing `Row` import |
| `src/.../ui/DoctorsScreen.kt` | Added `loaded` state + `Box(LoadingIndicator)` wrapper |
| `src/.../ui/PaymentsScreen.kt` | Added `loaded` state + `Box(LoadingIndicator)` wrapper |
| `src/.../ui/InventoryStockScreen.kt` | Added `loaded` state + `Box(LoadingIndicator)` wrapper |
| `src/.../ui/ProceduresScreen.kt` | Added `loaded` state + `Box(LoadingIndicator)` wrapper |
| `src/.../ui/PatientDetailWindow.kt` | Added `loaded` state + `Box(LoadingIndicator)` wrapper |
| `src/.../ui/DoctorDetailWindow.kt` | Added `Box(LoadingIndicator)` for null `uiState` |

## Components Added

- None (uses existing `LoadingIndicator` composable)

## Components Updated

- **ModernDashboardContent** — loading state changed from plain text to `LoadingIndicator`
- **PatientsScreen** — content wrapped in `loaded` check with `LoadingIndicator`
- **DoctorsScreen** — content wrapped in `loaded` check with `LoadingIndicator`
- **PaymentsScreen** — content wrapped in `loaded` check with `LoadingIndicator`
- **InventoryStockScreen** — content wrapped in `loaded` check with `LoadingIndicator`
- **ProceduresScreen** — content wrapped in `loaded` check with `LoadingIndicator`
- **PatientDetailWindow** — content wrapped in `loaded` check with `LoadingIndicator`
- **DoctorDetailWindow** — content shown only when `uiState != null`, with `LoadingIndicator` while null

## Technical Notes

- Pattern used: `var loaded by remember { mutableStateOf(false) }` → set `loaded = true` after data fetch → `if (!loaded) { LoadingIndicator() } else { content }`
- `DoctorDetailWindow` already used nullable `uiState: DoctorDetailUiState?` — this pattern was preserved, just added `LoadingIndicator` when null
- All screens use `Box(contentAlignment = Alignment.Center)` to center the spinner
- Rebuilt with `./gradlew compileKotlin` and `./gradlew check` — both pass (0 errors)

## Next Task

TASK-002: Elevation standardization — replace hardcoded `shadowElevation = 2.dp/4.dp` with `AppElevations.*` tokens across all screens
