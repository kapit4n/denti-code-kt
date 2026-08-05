# Milestone 5 — Changelog

All notable changes to the Reports milestone.

## TASK-002 — Doctor & patient analytics

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- New models: `TopDoctorRow`, `PatientsPerMonthPoint`; `ReportsOverview` extended with `noShowCount`, `topDoctors`, `patientsPerMonth`
- `reportsOverview` now attributes payments to doctors (appointment → `primary_doctor_id`, else performed action → `performing_doctor_id`, else "Sin asignar"), computes top-5 doctors by revenue, monthly new-patient buckets (`YearMonth` from `patients.created_at`) and the NO_SHOW count

### UI (`ui/reports/ReportsContent.kt`)
- 5th KPI card **Tasa de inasistencia** (rate % + n de X citas sin asistir)
- New charts row: **Pacientes nuevos por mes** (bar chart) + **Ingresos por doctor** (progress bars with count/revenue, "Sin asignar" in slate); per-section empty states

### Export (`export/ReportsExport.kt`)
- RESUMEN: `Citas sin asistir`, `Tasa de inasistencia`
- New sections: INGRESO POR DOCTOR (`Doctor;Cantidad;Ingreso`), PACIENTES NUEVOS POR MES (`Mes;Cantidad`)

### Verification
- Runtime probe: 5 doctors attributed (Bs 47 650,72 split), monthly buckets sum to `newPatientsCount` (150), `noShowCount` matches NO_SHOW slice, ordering assertions, CSV sections present (`Tasa de inasistencia;11.7%`) — **ALL REPORT2 CHECKS OK**
- Smoke-run booted into Reports: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)

## TASK-001 — Reports screen: revenue & appointment KPIs

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- Added report models: `ReportsOverview`, `RevenueDayPoint`, `AppointmentDayPoint`, `RevenueByMethodSlice`, `AppointmentStatusSlice`, `TopProcedureRow`
- Added `reportsOverview(startDate, endDate)` — revenue by day/method, appointments by day/status, new patients, top 5 billed procedures; all read in one transaction from `payments`, `appointments`, `patients`, `procedure_types`

### UI (`ui/reports/ReportsScreen.kt`, `ui/reports/ReportsContent.kt`, `ui/layout/AppShell.kt`)
- Reports route now renders a real screen (was a placeholder stub)
- Date-range filter: quick chips (7 días / 30 días / Este mes / 3 meses / Todo) + custom Desde/Hasta `AppDatePickerField`
- KPI row (MetricCard): ingreso total, citas, nuevos pacientes, promedio por pago
- Charts (reused enterprise components): line = ingresos por día, bar = citas por día, donut = ingresos por método with legend; appointments-by-status bars; top procedures list; per-section empty states

### Export (`export/ReportsExport.kt`)
- `renderReportsCsv` — multi-section `;`-separated CSV (resumen, ingreso por día/método, citas por estado, procedimientos top); locale-safe money, Excel-compatible quoting; saved via native dialog

### Verification
- Temporary data-layer probe: real totals over 2000→today (47 650.72 Bs / 210 pagos / 300 citas / 150 pacientes nuevos), series and CSV sections correct — **ALL REPORT CHECKS OK**
- Smoke-run booted directly into Reports: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)
