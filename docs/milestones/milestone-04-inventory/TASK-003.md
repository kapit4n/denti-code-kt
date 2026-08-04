# TASK-003: Stock alerts & threshold notifications

## Objective

Make the stock thresholds actionable: surface `StockStatus.LOW`/`OUT` lines on the Home/Dashboard as a dedicated stock-alert card, computed in-memory from real repository reads (`listInventoryLines()` + `resolveInventoryStockStatus`) with no new persistence and no `DentiRepository.kt` changes.

## Changes

### Data (`ui/dashboard/DashboardUiState.kt`)
- New `StockAlertUi` model — product name/code, consultory label, category label, quantity, min, status.
- `InventoryLineRow.toStockAlertUi()` — maps a line to an alert only when `resolveInventoryStockStatus` is `LOW`/`OUT` (reuses `categoryLabelEs` for the category label).

### UI (`ui/dashboard/DashboardCards.kt`)
- **`DashboardStockAlertsCard`** — "Alertas de stock" card with a "Ver inventario" action (navigates to the Inventory route), a summary caption (`X líneas bajas · Y agotadas`) and the top-6 alert rows (status badge, product, consultory · category, `qty/min`). Empty state: "Sin alertas de stock: todos los insumos en óptimo nivel."
- `StockAlertBadge` — compact status pill reusing the inventory palette (LOW amber, OUT red, OPTIMAL green).

### Grid (`ui/dashboard/DashboardGrid.kt`)
- `ResponsiveDashboardGrid` gains a `stockAlerts` parameter; the layout rebalances to three stacked rows: main widgets (0.5), status + alerts (0.2), stock alerts (0.3, full width).

### Wiring (`ui/dashboard/ModernDashboardContent.kt`)
- Loads `listInventoryLines()`, maps to alerts and sorts (OUT first, then by product name); the generic "Stock bajo" alert now derives its count from the real alert list.

### Scope decision
- Per-line/per-category **reorder threshold editing** is deferred: `minQuantity` is derived via `defaultInventoryMinQuantity(category, facilityId)` and `maxQuantity = min * 5` — there are no persisted threshold columns, so editing them would need a schema + repository change beyond this task's read-only DoD. Editing stock totals remains available via TASK-001 "Editar" / "Ajustar stock".

## Files Modified

| File | Change |
|------|--------|
| `ui/dashboard/DashboardUiState.kt` | `StockAlertUi` + `InventoryLineRow.toStockAlertUi()` |
| `ui/dashboard/DashboardCards.kt` | `DashboardStockAlertsCard` + `StockAlertBadge` |
| `ui/dashboard/DashboardGrid.kt` | `stockAlerts` param; rebalanced layout with full-width alerts row |
| `ui/dashboard/ModernDashboardContent.kt` | Loads + sorts stock alerts; alerts count from real lines |

## Build Status

✅ BUILD SUCCESSFUL (0 errors) + app smoke-run (75 s, no crash).

## Acceptance Criteria

- ✓ Dashboard shows a dedicated stock-alerts card listing `LOW`/`OUT` lines with consultory + category + qty/min
- ✓ "Ver inventario" navigates to the Inventory screen
- ✓ Alert list computed from real repository reads (`listInventoryLines()`); no new tables, no `DentiRepository.kt` changes
- ✓ `./gradlew build` passes with 0 errors

## Next Task

Milestone 4 — Inventory Management. TASK-004 (suppliers & purchase orders) — see `docs/roadmap/NEXT_TASK.md`.
