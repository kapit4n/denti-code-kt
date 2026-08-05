# TASK-002: Reports — doctor & patient analytics

## Objective

Extend the Reports screen (TASK-001) with doctor and patient dimensions using the same date-range filter, chart components and CSV export: **top doctors by revenue**, **patient acquisition trend** (new patients per month) and **appointment no-show rate**.

## What was added

### Data layer
- **`data/Models.kt`** — new models `TopDoctorRow(doctorName, count, revenue)`, `PatientsPerMonthPoint(month "yyyy-MM", count)`; `ReportsOverview` extended with `noShowCount`, `topDoctors`, `patientsPerMonth`.
- **`data/DentiRepository.kt`** — `reportsOverview` now also:
  - Attributes each payment to a doctor: payment → `appointments.primary_doctor_id` (when `appointment_id` present), else → `performed_actions.performing_doctor_id` (when `performed_action_id` present), else bucket **"Sin asignar"**.
  - `topDoctors`: payments grouped by attributed doctor, sorted by revenue desc (top 5).
  - `patientsPerMonth`: patients whose `created_at` falls in the range, bucketed by `YearMonth` and sorted chronologically.
  - `noShowCount`: appointments in range with status `NO_SHOW`.

### UI (`ui/reports/ReportsContent.kt`)
- **KPI row** now has 5 `MetricCard`s: + **Tasa de inasistencia** (e.g. `11.7%`, secondary `"35 de 300 citas sin asistir"`).
- New charts row (2 cards): **Pacientes nuevos por mes** (`EnterpriseBarChart` from `patientsPerMonth`, Spanish month labels) and **Ingresos por doctor** (progress bars scaled to max revenue, count + revenue per doctor, "Sin asignar" bucket in slate). Empty states per section.

### Export (`export/ReportsExport.kt`)
- RESUMEN: + `Citas sin asistir` and `Tasa de inasistencia`.
- New sections **INGRESO POR DOCTOR** (`Doctor;Cantidad;Ingreso`) and **PACIENTES NUEVOS POR MES** (`Mes;Cantidad`).

## Components added / updated

| Component | Status |
|-----------|--------|
| `TopDoctorRow`, `PatientsPerMonthPoint` + `ReportsOverview` fields | Added |
| `reportsOverview()` doctor attribution + monthly buckets + no-show | Updated |
| `TopDoctors` card + `Pacientes nuevos por mes` chart in ReportsContent | Added |
| KPI row 5th card (Tasa de inasistencia) | Updated |
| `renderReportsCsv` new sections/fields | Updated |

## Reused components

`MetricCard`, `AppCard`, `EnterpriseBarChart`, progress-bar pattern (as in `AppointmentsByStatus`), `renderReportsCsv` — no new layout/chart primitives.

## Verification

- Runtime probe (temporary, removed): full-range report → 5 doctors (Dr. Patricia López Fernández 59 pagos / Bs 16 430,47 … Dr. Ana Torres Ríos 29 / Bs 5 178,69), 4 monthly buckets (2026-04…2026-07, 6+57+53+34 = 150 new patients, matches `newPatientsCount`), `noShowCount=35` equals the NO_SHOW status slice, doctors sorted by revenue desc, months sorted asc, CSV contains all new sections (`Citas sin asistir;35`, `Tasa de inasistencia;11.7%`) — **ALL REPORT2 CHECKS OK**.
- Smoke-run booted directly into Reports: 75 s, no exceptions.
- ✅ `./gradlew build` — 0 errors.

## Next Task

TASK-003 (provisional): treatment & inventory analytics — top procedures by category, revenue split between catalog price vs negotiated price, stock movement report (entries/exits per product per period) with the same filter + CSV export.
