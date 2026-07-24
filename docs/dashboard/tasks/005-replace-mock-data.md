# Task 005: Replace Dashboard Mock Data with Real DB Queries

## Objective

Replace all hardcoded mock data in the dashboard with real data from the SQLite database via `DentiRepository`.

## Motivation

The dashboard previously used hardcoded values for:
- Activity feed (5 fake items)
- Weekly revenue chart (7 fake values)
- Revenue total label ("€ 12.4k")
- Revenue delta label ("+12%")
- "Pagos pendientes" alert (always shown, no real count)

With the demo data seeder (Task 003) now populating 300+ appointments, 500+ payments, and audit logs, the dashboard can show real data.

## Files Modified

- `src/.../data/DentiRepository.kt` — added 3 new methods
- `src/.../ui/dashboard/ModernDashboardContent.kt` — replaced mock data with repo calls

## New Repository Methods

### `weeklyRevenue(): Pair<List<Double>, Double>`

Returns daily payment totals for the last 7 days plus the weekly sum. Queries `listRecentPayments(1000)` and groups by ISO date prefix.

### `recentActivity(limit: Int = 5): List<Pair<String, String>>`

Returns recent audit log entries as (label, timeLabel) pairs. Queries `AppointmentAuditLogTable` ordered by timestamp DESC. Maps actions like "Appointment created", "Payment registered" to human-readable Spanish labels with elapsed time.

### `countPendingPayments(): Int`

Returns count of active treatments (not cancelled). Used for the "Pagos pendientes" alert.

## Mock Data Removed

| What | Before | After |
|---|---|---|
| `buildMockActivity()` | 5 hardcoded `ActivityFeedItem` | Real audit log entries from DB |
| `defaultWeekRevenue` | `listOf(1200f, 1450f, ...)` | `repo.weeklyRevenue()` |
| Revenue total label | `"€ 12.4k"` | `"Bs. X.Xk"` from real sum |
| Revenue delta label | `"+12%"` | Calculated % from last 2 days |
| "Pagos pendientes" | Always shown | `countPendingPayments()` count |

## Data Flow

```
LaunchedEffect → Dispatchers.IO
  ├── repo.clinicOverview()        → KPI row
  ├── repo.listTodayAppointments() → Appointments card
  ├── repo.sumPaymentsToday()      → Revenue KPI
  ├── repo.countLowStockLines()    → Stock KPI + alert
  ├── repo.listAppointments(300)   → Donut chart
  ├── repo.weeklyRevenue()         → Revenue card (NEW)
  ├── repo.recentActivity(5)       → Activity card (NEW)
  └── repo.countPendingPayments()  → Alert card (NEW)
```

## Currency Fix

KPI row now shows "Bs." instead of "€" for Bolivianos, consistent with the clinic's location.

## Build Verification

- `./gradlew compileKotlin` — passed
- `./gradlew build` — passed (BUILD SUCCESSFUL)
