# Task 006: Appointments Page UI Refinement

## Status: DONE
## Date: 2026-07-24

## Goal
Standardize visual hierarchy, spacing, borders, elevation, and typography across all appointment UI components. Eliminate overlapping elements and ensure consistent Material 3 design language.

## Changes

### 1. AppointmentTimelineCard (AppointmentComponents.kt)
- **Selection state**: Simplified from 12dp shadow + 4dp glow border to 2dp primary border + 6dp elevation, no glow
- **Status badge**: Moved inside card content area (right column), 40dp touch target on three-dot menu
- **Typography**: Consistent sizing — time (CardTitle), patient name (Body/SemiBold), treatment (BodySmall), doctor (Caption). Removed conditional sizing/weight changes on selection
- **Overflow**: All text fields use `maxLines = 1, overflow = TextOverflow.Ellipsis`
- **Removed**: `timeScale` animation, `SelectedAppointmentPill` (redundant with border), left accent bar
- **End padding**: Standardized to 16dp

### 2. SummaryMetric (AppointmentComponents.kt)
- Added 3dp colored left indicator bar for visual category identification
- Icon reduced from 22dp to 20dp for tighter layout
- Count and label moved to right side with `Spacer(weight(1f))` for proper alignment
- Uses `surfaceVariant` background at 30% alpha instead of tint-specific backgrounds

### 3. MiniCalendar (AppointmentComponents.kt)
- Surface changed from `shadowElevation = 2.dp` to `BorderStroke(1.dp, outlineVariant)`
- Cell padding increased from 2dp to 3dp for better touch targets
- Cell size increased from 32dp to 34dp

### 4. DaySummaryCard (AppointmentComponents.kt)
- Surface changed from `shadowElevation = 2.dp` to `BorderStroke(1.dp, outlineVariant)`

### 5. Border Standardization (AppointmentComponents.kt)
- `HeaderActionButton`: `outline.copy(alpha=0.35f)` → `outlineVariant`
- `FilterDropdown`: `outline.copy(alpha=0.4f)` → `outlineVariant`
- `AppointmentViewModeToggle`: `outline.copy(alpha=0.35f)` → `outlineVariant`
- `ReminderCard`: Added `BorderStroke(1.dp, primary.copy(alpha=0.2f))`, reduced bg alpha from 0.1f to 0.06f
- `AppointmentTimelineCard`: Rest state border changed from `outline.copy(alpha=0.22f)` to `outlineVariant`

### 6. AppointmentDetailPanel (AppointmentDetailPanel.kt)
- **DetailRow**: Label now uppercase, font changed from Caption (12sp) to Caption.copy(fontSize=11sp), icon reduced from 20dp to 18dp, vertical spacing reduced from sm to 10dp, icon+label gap reduced from md to sm
- **Avatar**: Reduced from 52dp to 44dp for consistency with card avatar
- **Close button**: Added 36dp size constraint with 4dp end/top padding, icon 20dp
- **Panel border**: Linked state reduced from 8dp shadow + 0.45f alpha to 4dp shadow + 0.35f alpha; unlinked state changed from outline.copy(alpha=0.18f) to outlineVariant
- **Empty state**: Changed from SectionTitle to Body style for less visual weight
- **Patient name**: Added `maxLines = 1, overflow = TextOverflow.Ellipsis`
- **"Cita seleccionada" label**: Changed from Caption to Caption (consistent)

## Files Modified
- `src/main/kotlin/com/denticode/kt/ui/appointments/AppointmentComponents.kt`
- `src/main/kotlin/com/denticode/kt/ui/appointments/AppointmentDetailPanel.kt`

## Design Tokens Used
- **Borders**: `MaterialTheme.colorScheme.outlineVariant` (standard), `AppointmentPremiumPalette.primary` (selected)
- **Elevation**: 0dp (rest), 2dp (standard), 4dp (hover/linked panel), 6dp (selected card)
- **Spacing**: AppSpacing.xs (4), sm (8), md (16), lg (24), xl (32)
- **Typography**: AppTypography.CardTitle, Body, BodySmall, Caption

## Verification
- `./gradlew compileKotlin` — BUILD SUCCESSFUL
- `./gradlew check` — BUILD SUCCESSFUL
