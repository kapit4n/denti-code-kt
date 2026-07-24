# Dashboard Changelog

All notable changes to the Denti-Code KT dashboard are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/).

## v0.5.0 — 2026-07-23

### Changed

- `DashboardComponents.kt` (623 lines) split into 3 focused files:
  - `DashboardKpi.kt` (183 lines) — welcome header + KPI row
  - `DashboardCards.kt` (293 lines) — 5 content cards
  - `DashboardGrid.kt` (153 lines) — responsive layout + status badge

### Removed

- `RevenueLineChartCard` composable (unused standalone card)
- `AppointmentDonutSection` composable (unused standalone section)
- `defaultWeek` hardcoded revenue data
- 5 unused imports from `DashboardCharts.kt`

### Added

- `DentiRepository.weeklyRevenue()` — real 7-day payment totals
- `DentiRepository.recentActivity()` — real audit log feed
- `DentiRepository.countPendingPayments()` — treatment payment status count

### Fixed

- Activity feed now shows real audit log entries instead of hardcoded items
- Revenue chart now shows real weekly payment data instead of fake values
- Revenue labels now use "Bs." (Bolivianos) instead of "€"
- "Pagos pendientes" alert now shows real pending treatment count
- Revenue delta percentage calculated from actual day-over-day data

## v0.3.0 — 2026-07-23

### Added

- Comprehensive demo data seeder (13 files under `data/seeders/`)
- 150 patients with Bolivian names and Cochabamba addresses
- 300 appointments distributed across past/present/future
- 500+ payments (full, partial, anticipos) in Bolivianos
- 28 dental procedures with realistic pricing (50–1,500 Bs.)
- 35 inventory items across 8 categories
- Treatment records for completed/in-progress appointments
- Appointment notes and audit logs
- `DemoDataConfig` for adjustable data volumes
- Idempotent seeding — safe to call multiple times

### Changed

- `Database.kt` now calls `DemoDataSeeder.seedIfEmpty()` after existing seed functions

### Documentation

- Created `docs/dashboard/tasks/003-demo-data-seeder.md`
- Created `docs/dashboard/decisions/002-seeder-architecture.md`

## v0.2.0 — 2026-07-23

### Removed

- `DashboardQuickActionsCard` — duplicated top bar quick actions (Nueva cita, Nuevo paciente, Nuevo pago)
- `QuickActionDef` private data class
- `DashboardQuickActionButton` private composable
- 3 unused imports (`clickable`, `Icons.Outlined.Assessment`, `Icons.Outlined.Search`)

### Changed

- Lower row now contains 2 cards (Status + Alerts) instead of 3
- Status donut chart and alerts card each take 50% width (was 33%)

### Improved

- `DashboardComponents.kt` reduced from 722 to 624 lines
- Eliminated redundant navigation paths
- More space for status and alerts cards

## v0.1.0 — 2026-07-23

### Added

- Welcome header with greeting ("Hola, equipo clínico") and current date
- 5 KPI cards with gradient backgrounds (Patients, Today's Appointments, Revenue, Treatments, Stock)
- Today's appointments card with table (time, patient, doctor, status)
- Activity feed card (mock data)
- Weekly revenue card with Canvas-based line chart (mock data)
- Appointment status donut chart with legend (real DB data)
- Alert cards (stock low, payments pending, unconfirmed appointments)
- Responsive 3-column / 2-column / 1-column grid layout
- Hover effects on KPI cards (scale + elevation)
- Hover effects on appointment rows (background highlight)
- Status badge component for appointment states
- Dashboard documentation structure

### Architecture

- `DashboardScreen` thin wrapper delegates to `ModernDashboardContent`
- `ModernDashboardContent` handles data loading via `LaunchedEffect` + `Dispatchers.IO`
- `ResponsiveDashboardGrid` uses `BoxWithConstraints` for responsive layout
- All UI models defined in `DashboardUiState.kt`
- Charts implemented via Canvas (no external charting library)

### Known Issues

- Activity feed is hardcoded mock data
- Revenue chart uses hardcoded weekly values
- "Pagos pending" alert is always shown
- `DashboardComponents.kt` needs splitting (624 lines)
- Unused composables: `RevenueLineChartCard`, `AppointmentDonutSection`
