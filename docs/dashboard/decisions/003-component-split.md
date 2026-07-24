# Decision 003: Dashboard Component Split Strategy

## Context

`DashboardComponents.kt` reached 623 lines containing 12 composables spanning 3 distinct concerns: KPI display, content cards, and responsive grid layout. The ROADMAP identified this as a high-priority refactor target.

## Alternatives Considered

### 1. Split by widget (one file per card)
- **Pros**: Maximum granularity
- **Cons**: 7+ files for small composables, excessive file count for a UI module

### 2. Split by row (top-row cards, bottom-row cards)
- **Pros**: Natural grouping by dashboard position
- **Cons**: Mixed concerns within files (KPI cards mixed with content cards)

### 3. Split by concern (KPI, Cards, Grid) — CHOSEN
- **Pros**: Clear separation of responsibilities, 3 balanced files (~150-300 lines each), aligns with Compose module boundaries
- **Cons**: Some cross-references between files (all in same package)

## Final Decision

Split into 3 files by functional concern:

| File | Responsibility | Composables |
|---|---|---|
| `DashboardKpi.kt` | Header + KPI metrics | `DashboardWelcomeHeader`, `DashboardKpiRow`, `DashboardKpiCard` |
| `DashboardCards.kt` | Content widgets | `DashboardAppointmentsCard`, `DashboardActivityCard`, `DashboardRevenueCard`, `DashboardStatusCard`, `DashboardAlertsCard` |
| `DashboardGrid.kt` | Layout orchestration | `ResponsiveDashboardGrid`, `DashboardGridLayout`, `DashboardStatusBadge` |

All files remain in `com.denticode.kt.ui.dashboard` — no package restructuring needed.

## Consequences

- **Positive**: Each file is focused and independently navigable
- **Positive**: Reduced file sizes from 623 lines max to 293 lines max
- **Positive**: Easier to find and modify specific dashboard components
- **Neutral**: No functional change, only structural
- **Negative**: 3 files instead of 1 (acceptable tradeoff)
