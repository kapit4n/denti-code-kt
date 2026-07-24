# Dashboard

## Purpose

The Dashboard is the default landing screen of Denti-Code KT. It provides a clinic overview with key performance indicators, today's appointments, activity feed, revenue chart, appointment status donut, and alert cards. It is designed to give clinical staff a quick operational snapshot upon login.

## Current Architecture

```
DashboardScreen (thin wrapper)
  └── ModernDashboardContent (data loading + layout)
        ├── DashboardWelcomeHeader
        ├── DashboardKpiRow
        │     └── DashboardKpiCard ×5 (Pacientes, Citas hoy, Ingresos, Tratamientos, Stock)
        └── ResponsiveDashboardGrid (BoxWithConstraints → 3/2/1 column layout)
              ├── Row (Main Row)
              │     ├── DashboardAppointmentsCard (today's appointments table)
              │     ├── DashboardActivityCard (activity feed)
              │     └── DashboardRevenueCard (weekly revenue line chart)
              └── Row (Lower Row)
                    ├── DashboardStatusCard (donut chart + legend)
                    └── DashboardAlertsCard (stock, payments, appointments alerts)
```

## Widget Hierarchy

| Widget | File | Lines | Purpose |
|---|---|---|---|
| `DashboardScreen` | `DashboardScreen.kt` | 16 | Thin routing wrapper |
| `ModernDashboardContent` | `ModernDashboardContent.kt` | 138 | Data loading + orchestration |
| `DashboardWelcomeHeader` | `DashboardComponents.kt` | 37 | Greeting + date display |
| `DashboardKpiRow` | `DashboardComponents.kt` | 53 | Row of 5 KPI cards |
| `DashboardKpiCard` | `DashboardComponents.kt` | 49 | Individual gradient KPI card |
| `ResponsiveDashboardGrid` | `DashboardComponents.kt` | ~90 | Responsive 3/2/1 column grid |
| `DashboardAppointmentsCard` | `DashboardComponents.kt` | 80 | Today's appointments table |
| `DashboardActivityCard` | `DashboardComponents.kt` | 58 | Activity feed list |
| `DashboardRevenueCard` | `DashboardComponents.kt` | 32 | Weekly revenue with line chart |
| `DashboardStatusCard` | `DashboardComponents.kt` | 40 | Donut chart for appointment statuses |
| `DashboardAlertsCard` | `DashboardComponents.kt` | 59 | Alert list (stock, payments, appointments) |
| `DashboardStatusBadge` | `DashboardComponents.kt` | 14 | Appointment status chip |
| `RevenueLineChart` | `DashboardCharts.kt` | 52 | Canvas-based line chart |
| `AppointmentStatusDonutChart` | `DashboardCharts.kt` | 33 | Canvas-based donut chart |
| `DonutLegend` | `DashboardCharts.kt` | 26 | Legend for donut chart |

## Layout Structure

### Responsive Grid

The dashboard uses a `BoxWithConstraints` to determine the layout:

- **Three-column** (≥1200dp): Appointments (45%), Activity (25%), Revenue (30%)
- **Two-column** (≥800dp): Appointments full width top; Activity + Revenue side by side below
- **Single-column** (<800dp): All three cards stacked vertically

### Vertical Layout

```
┌─────────────────────────────────────────────────┐
│ Welcome Header (greeting + date)                │
├─────────────────────────────────────────────────┤
│ KPI Row (5 gradient cards)                      │
├─────────────────────────────────────────────────┤
│ Main Row (appointments + activity + revenue)    │  weight(0.58)
├─────────────────────────────────────────────────┤
│ Lower Row (status + alerts)                     │  weight(0.42)
└─────────────────────────────────────────────────┘
```

## Navigation Flow

- Dashboard is the default route (`ScreenRoute.Dashboard`)
- Sidebar navigation: click "Dashboard" in the sidebar
- Command palette: Ctrl+K → select Dashboard
- Quick actions in top bar navigate to Appointments, Patients, Payments

## Current Implemented Features

- [x] Welcome header with greeting and current date
- [x] 5 KPI cards (Patients, Today's Appointments, Revenue, Treatments, Low Stock)
- [x] Today's appointments table with time, patient, doctor, status
- [x] Activity feed (hardcoded mock data)
- [x] Weekly revenue line chart (hardcoded mock data)
- [x] Appointment status donut chart (real data from DB)
- [x] Alert cards (stock low, payments pending, unconfirmed appointments)
- [x] Responsive 3/2/1 column layout
- [x] Hover effects on KPI cards and appointment rows
- [x] Navigation from dashboard cards to respective screens

## Pending Improvements

- [ ] Replace `buildMockActivity()` with real activity data
- [ ] Replace `defaultWeekRevenue` with real revenue data
- [ ] Replace hardcoded "Pagos pendientes" alert with real data
- [ ] Split `DashboardComponents.kt` into smaller files
- [ ] Remove unused `RevenueLineChartCard` and `AppointmentDonutSection`
- [ ] Add empty state for when there is no data
- [ ] Add pull-to-refresh or auto-refresh capability
- [ ] Add click-through on KPI cards to relevant screens

## Known Limitations

- Activity feed is entirely mock/hardcoded data
- Revenue chart uses hardcoded weekly values
- "Pagos pendientes" alert is always shown regardless of actual payment status
- No real-time updates (data loaded once on composition)
- No error handling for failed DB queries
- `DashboardComponents.kt` is 624 lines, making maintenance difficult

## File Inventory

| File | Status |
|---|---|
| `src/.../ui/DashboardScreen.kt` | Wrapper, minimal changes needed |
| `src/.../ui/dashboard/ModernDashboardContent.kt` | Data loading, mock data to replace |
| `src/.../ui/dashboard/DashboardComponents.kt` | Large file, candidates for splitting |
| `src/.../ui/dashboard/DashboardCharts.kt` | Charts, some unused composables |
| `src/.../ui/dashboard/DashboardUiState.kt` | UI models, stable |
