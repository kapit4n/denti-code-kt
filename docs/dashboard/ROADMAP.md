# Dashboard Roadmap

Progress tracking for dashboard cleanup and improvements.

## Completed

- [x] Documentation bootstrap (README, CHANGELOG, ROADMAP, decisions, screenshots)
- [x] Remove `DashboardQuickActionsCard` — duplicates top bar quick actions (v0.2.0)
- [x] Demo data seeder — comprehensive Bolivian clinic data (v0.3.0)
- [x] Split `DashboardComponents.kt` into smaller files (v0.5.0)
- [x] Remove unused composables (`RevenueLineChartCard`, `AppointmentDonutSection`) (v0.5.0)
- [x] Replace `buildMockActivity()` with real activity data from DB (v0.5.0)
- [x] Replace `defaultWeekRevenue` with real weekly revenue aggregation (v0.5.0)
- [x] Replace hardcoded "Pagos pendientes" alert with real data (v0.5.0)

## In Progress

- (none)

## Planned

### High Priority

- (none)

### Medium Priority

- [ ] Add empty state component for dashboard when no data exists

### Low Priority

- [ ] Add click-through on KPI cards to navigate to relevant screens
- [ ] Add auto-refresh capability (periodic data reload)
- [ ] Add error handling for failed DB queries
- [ ] Add loading skeleton instead of "Cargando panel..." text
- [ ] Improve dashboard accessibility (semantics, content descriptions)

## Deferred

- [ ] Dashboard customization (user-configurable widget layout)
- [ ] Real-time updates via coroutine polling
- [ ] Dashboard widgets as composable slots (plugin architecture)
