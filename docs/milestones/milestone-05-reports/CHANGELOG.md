# Milestone 5 — Changelog

All notable changes to the Reports milestone.

## TASK-004 — Report filters & saved ranges

### Data / persistence (`ui/reports/ReportSettingsStore.kt`)
- New `ReportSettingsStore` — persists last used date range + named presets to `~/.denti-code-kt/reports-settings.properties` (local/offline); models `ReportSettings`/`SavedReportRange`; `load` with defaults + per-preset validation, `save` best-effort

### Screen (`ui/reports/ReportsScreen.kt`)
- Loads saved range/presets on init (IO) and recomputes the overview for the restored range; persists every range/preset change automatically (guarded until initial load)
- `savePreset` (dedupe by name, cap 12) + `removePreset` with messenger feedback

### UI (`ui/reports/ReportsContent.kt`)
- Range bar: quick chips → preset chips (trailing ✕ delete icon) → «Guardar rango» chip; `SaveRangeDialog` (`AppBasicDialog` + `AppTextField`)
- Keyboard shortcuts: `Alt+1…5` quick ranges, `Alt+Mayús+1…9` presets; caption hint below the chips

### Verification
- Runtime probe (temp dir `user.home`): defaults, save→load round-trip (range + 2 presets), preset clear, file written, `reportsOverview` OK — **ALL REPORT4 CHECKS OK**
- Smoke-run booted into Reports: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)

## TASK-003 — Treatment & inventory analytics

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- New models: `ProcedureCategorySlice`, `RevenueVsCatalog` (with derived `discount` + `discountRate`), `StockMovementSlice`, `StockMovements`; `ReportsOverview` extended with `revenueByCategory`, `revenueVsCatalog`, `stockMovements`
- `reportsOverview` now also resolves each procedure's category (`treatment_categories` via `category_id`, legacy `category` column fallback, else "Sin categoría"), buckets payments by category, computes catalog-vs-charged from `performed_actions` (`standard_price` vs `total_price` in range) and the stock movement report from `inventory_product_movements` (`created_at_epoch_ms` in range, in/out units + values by type via `inventoryMovementLabel`)

### UI (`ui/reports/ReportsContent.kt`)
- New cards row: **Ingresos por categoría** (donut + legend with count/revenue), **Ingresos vs catálogo** (3 `MetricCard`s: valor catálogo, total cobrado, descuento con %), **Movimientos de stock** (totals `MetricCard` + per-type progress bars); per-section empty states

### Export (`export/ReportsExport.kt`)
- RESUMEN: `Valor de catálogo`, `Total cobrado`, `Acciones realizadas`, `Descuento aplicado`, `Descuento medio`
- New sections: INGRESO POR CATEGORÍA (`Categoría;Cantidad;Ingreso`), MOVIMIENTOS DE STOCK (totals + per-type `Tipo;Cantidad;Entradas;Salidas;Valor entradas;Valor salidas`)

### Verification
- Runtime probe: 10 categories sum to `totalRevenue` (Bs 47 650,72) and `paymentCount` (210); catalog 48 100,00 vs charged 47 915,53 Bs (discount 184,47 Bs / 0,4%) over 181 actions; 72 stock movements (2014 in / 96 out, Inventario inicial + Consumo tratamiento) — **ALL REPORT3 CHECKS OK**
- Smoke-run booted into Reports: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)

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
