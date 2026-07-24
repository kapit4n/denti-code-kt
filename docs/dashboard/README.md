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
| `DashboardWelcomeHeader` | `DashboardKpi.kt` | 37 | Greeting + date display |
| `DashboardKpiRow` | `DashboardKpi.kt` | 53 | Row of 5 KPI cards |
| `DashboardKpiCard` | `DashboardKpi.kt` | 49 | Individual gradient KPI card |
| `ResponsiveDashboardGrid` | `DashboardGrid.kt` | ~90 | Responsive 3/2/1 column grid |
| `DashboardAppointmentsCard` | `DashboardCards.kt` | 80 | Today's appointments table |
| `DashboardActivityCard` | `DashboardCards.kt` | 58 | Activity feed list |
| `DashboardRevenueCard` | `DashboardCards.kt` | 32 | Weekly revenue with line chart |
| `DashboardStatusCard` | `DashboardCards.kt` | 40 | Donut chart for appointment statuses |
| `DashboardAlertsCard` | `DashboardCards.kt` | 59 | Alert list (stock, payments, appointments) |
| `DashboardStatusBadge` | `DashboardGrid.kt` | 14 | Appointment status chip |
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
- [x] Activity feed (real audit log data from DB)
- [x] Weekly revenue line chart (real weekly payment data from DB)
- [x] Appointment status donut chart (real data from DB)
- [x] Alert cards (stock low, payments pending, unconfirmed appointments) — all real data
- [x] Responsive 3/2/1 column layout
- [x] Hover effects on KPI cards and appointment rows
- [x] Navigation from dashboard cards to respective screens
- [x] Appointments page: quick filter toolbar with chips (Hoy, Próximas, Completadas, Confirmadas, Canceladas, En curso)
- [x] Appointments page: 2×2 KPI summary grid
- [x] Appointments page: hover actions on timeline cards
- [x] Appointments page: detail panel split (Patient Info + Technical Details collapsible)
- [x] Appointments page: improved empty state with icon + button

## Pending Improvements

- [ ] Add empty state for when there is no data
- [ ] Add pull-to-refresh or auto-refresh capability
- [ ] Add click-through on KPI cards to relevant screens

## Known Limitations

- No real-time updates (data loaded once on composition)
- No error handling for failed DB queries

## File Inventory

| File | Status |
|---|---|
| `src/.../ui/DashboardScreen.kt` | Wrapper, minimal changes needed |
| `src/.../ui/dashboard/ModernDashboardContent.kt` | Data loading, real DB queries |
| `src/.../ui/dashboard/DashboardKpi.kt` | Welcome header + KPI row |
| `src/.../ui/dashboard/DashboardCards.kt` | 5 content cards |
| `src/.../ui/dashboard/DashboardGrid.kt` | Responsive layout + status badge |
| `src/.../ui/dashboard/DashboardCharts.kt` | Charts, real data |
| `src/.../ui/dashboard/DashboardUiState.kt` | UI models, stable |
