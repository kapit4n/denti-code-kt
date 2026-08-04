# TASK-002: Inventory dashboard & movement insights

## Objective

Give the Inventory screen a live read-only dashboard without touching `DentiRepository.kt`: 30-day movement KPIs (entries/exits/movements), a category breakdown bar chart, replenishment ("reposición") suggestions and a richer movement-history side panel (type, reason note, running balance) — all computed in-memory from the existing `listInventoryMovements`/`listInventoryLines` reads.

## Changes

### UI models (`ui/inventory/StockUiModels.kt`)
- `StockMovementUiModel` gained optional `note` and `balance`; `InventoryMovementRow.toUiModel(balance)` passes them through.
- New derived data classes:
  - `StockInsights` — `last30dEntries`/`last30dEntriesCount`/`last30dExits`/`last30dExitsCount`/`last30dMovementsCount` + `reorderCount` + `reorderLines` (top-10).
  - `StockReorderSuggestion` — product/category/consultory labels, quantity/min/max, status, `suggestedOrder` (max − current, floor 1).
  - `StockCategoryStat` — `categoryKey`/`label`/`units`/`lineCount` per category.
- New pure functions `stockStatusRank()` (OUT=0, LOW=1, OPTIMAL=2), `buildStockInsights(lines, movements)` (30-day cutoff via `System.currentTimeMillis() - 30 * 86400000`), `buildStockCategoryStats(lines)` (grouped by category, sorted by units desc). No DB writes; no new tables.

### Insights UI (`ui/inventory/StockInsightsPanel.kt`, new)
- `StockInsightsRow` — 4 KPI cards reusing `StockSummaryCard`: Entradas (30d), Salidas (30d), Movimientos (30d), Necesitan reposición.
- `StockInsightsPanel` — side panel with an `EnterpriseBarChart` "Unidades por categoría" (colors reused from the stock table) + the top-10 "Reposición sugerida" list (status-colored "Pedir N" with consultory/stock/min context), plus a summary caption of net 30-day entries/exits.

### Wiring (`ui/inventory/ModernStockContent.kt`)
- Computes `insights` + `categoryStats` from the loaded lines/movements and renders `StockInsightsRow` under the summary row.
- Right slot: `StockDetailsPanel` when a line is selected at ≥1200 dp; otherwise `StockInsightsPanel` at ≥1000 dp.
- `selectedMovements` now walks movements newest→first and derives a running balance per row (`toUiModel(balance)`).

### Side panel (`ui/inventory/StockListComponents.kt`)
- Movement history rows now show the movement **note** (reason) and the running **Stock** balance alongside type/user/date.

## Files Modified

| File | Change |
|------|--------|
| `ui/inventory/StockUiModels.kt` | `note`/`balance` on movement model; `StockInsights`/`StockReorderSuggestion`/`StockCategoryStat` + builders |
| `ui/inventory/StockInsightsPanel.kt` | New: KPI row + insights side panel (bar chart + reorder suggestions) |
| `ui/inventory/ModernStockContent.kt` | Insights computed/render, right-slot logic, running balance |
| `ui/inventory/StockListComponents.kt` | Note + running balance in movement history; `categoryColors` exposed |

## Build Status

✅ BUILD SUCCESSFUL (0 errors) + app smoke-run (75 s, no crash).

## Acceptance Criteria

- ✓ 30-day entry/exit/movement KPIs computed from movement history (no new persistence)
- ✓ Category breakdown bar chart rendered from in-memory aggregation
- ✓ Top-10 replenishment suggestions with a suggested order quantity; count KPI reflects all needing lines
- ✓ Movement history shows type, reason note and running stock balance
- ✓ `./gradlew build` passes with 0 errors; no `DentiRepository.kt` changes

## Next Task

Milestone 4 — Inventory Management. TASK-003 (stock alerts / threshold notifications) — see `docs/roadmap/NEXT_TASK.md`.
