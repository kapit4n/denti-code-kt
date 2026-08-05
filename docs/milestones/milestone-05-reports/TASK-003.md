# TASK-003: Reports — treatment & inventory analytics

## Objective

Extend the Reports screen (TASK-001 + TASK-002) with treatment and inventory dimensions using the same date-range filter, chart components and CSV export: **revenue by procedure category**, **revenue vs catalog price** (per performed action) and a **stock movement report** (entries/exits per period by movement type).

## What was added

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- **`data/Models.kt`** — new models:
  - `ProcedureCategorySlice(category, count, revenue)` — a category bucket with payment count + revenue.
  - `RevenueVsCatalog(totalCatalog, totalCharged, actionCount)` — derived `discount` (`totalCatalog − totalCharged`, floored at 0) and `discountRate` (%). `totalCatalog` sums `standard_price` (falls back to `total_price` when null), `totalCharged` sums `total_price` from `performed_actions`.
  - `StockMovementSlice(label, count, unitsIn, unitsOut, valueIn, valueOut)` and `StockMovements(movementCount, unitsIn, unitsOut, valueIn, valueOut, byType)`.
  - `ReportsOverview` extended with `revenueByCategory`, `revenueVsCatalog`, `stockMovements`.
- **`data/DentiRepository.kt`** — `reportsOverview` (same single `transaction {}`) now also:
  - Resolves each procedure's category: `procedure_types.category_id → treatment_categories.name`, falling back to the legacy `category` column, else `"Sin categoría"`.
  - `revenueByCategory`: payments grouped by resolved category (sorted by revenue desc). Sum of category revenues/counts equals `totalRevenue`/`paymentCount` (no uncategorized payments in the seed).
  - `revenueVsCatalog`: `performed_actions` rows whose `action_at` day is in range; sums `standard_price` vs `total_price`, counts charged actions.
  - `stockMovements`: `inventory_product_movements` rows filtered by `created_at_epoch_ms` in range; positive `quantity_change` → units/values in, negative → out; grouped `byType` via `inventoryMovementLabel` (enum `labelEs` → known seed values like `INITIAL`/`CONSUMPTION` → readable Spanish → raw fallback), sorted by count desc.

### UI (`ui/reports/ReportsContent.kt`)
- New cards row (3 cards):
  - **Ingresos por categoría** — `EnterpriseDonutChart` (donut) + legend rows with count and revenue per category.
  - **Ingresos vs catálogo** — three `MetricCard`s: Valor de catálogo (with action count), Total cobrado (with average per action), Descuento aplicado (amount + %).
  - **Movimientos de stock** — `MetricCard` with total movements and `+entradas/−salidas` units, then per-type progress bars (`Tipo · count · +in/−out`).
- Empty states per section.

### Export (`export/ReportsExport.kt`)
- RESUMEN: `Valor de catálogo`, `Total cobrado`, `Acciones realizadas`, `Descuento aplicado`, `Descuento medio`.
- New sections **INGRESO POR CATEGORÍA** (`Categoría;Cantidad;Ingreso`) and **MOVIMIENTOS DE STOCK** (totals row + per-type `Tipo;Cantidad;Entradas;Salidas;Valor entradas;Valor salidas`).

## Components added / updated

| Component | Status |
|-----------|--------|
| `ProcedureCategorySlice`, `RevenueVsCatalog`, `StockMovementSlice`, `StockMovements` + `ReportsOverview` fields | Added |
| `reportsOverview()` category resolution + catalog-vs-charged + stock movements | Updated |
| `RevenueByCategory`, `RevenueVsCatalog`, `StockMovementsCard` in ReportsContent | Added |
| `renderReportsCsv` new sections/fields | Updated |

## Reused components

`AppCard`, `MetricCard`, `EnterpriseDonutChart`, `DonutSegment`, `methodColors`, progress-bar pattern, `ReportsEmpty` — no new layout/chart primitives.

## Verification

- Runtime probe (temporary, removed): full-range report → 10 categories (Prosthetics Bs 16 950,48 · 24 pagos … Preventive Bs 668,39), category revenues sum exactly to `totalRevenue` (47 650,72 Bs) and counts to `paymentCount` (210); `revenueVsCatalog`: catalog 48 100,00 Bs / charged 47 915,53 Bs → discount 184,47 Bs (0,4%) over 181 actions; stock: 72 movements, 2014 units in (34 «Inventario inicial») and 96 out (38 «Consumo tratamiento») — **ALL REPORT3 CHECKS OK**.
- Smoke-run booted directly into Reports: 75 s, no exceptions.
- ✅ `./gradlew build` — 0 errors.

## Notes

- Seed data uses English category names (Prosthetics, Endodontics, Cosmetic, Periodontics, Surgery, Restorative, Orthodontics, Diagnostic, Pediatric, Preventive) via `treatment_categories`; the legacy `category` fallback covers older rows.
- Seed `inventory_product_movements` rows carry no `unit_cost`, so value-in/out are `0,00` in demo data; the report reads `unit_cost` so real movements with cost will show values.

## Next Task

TASK-004 (provisional): report filters & saved ranges — persist the selected date range, add named range presets.
