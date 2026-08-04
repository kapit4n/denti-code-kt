# Milestone 4 — Changelog

All notable changes to the Inventory Management milestone.

## TASK-003 — Stock alerts & threshold notifications

### Dashboard alerts
- **`ui/dashboard/DashboardUiState.kt`** — new `StockAlertUi` + `InventoryLineRow.toStockAlertUi()` (LOW/OUT only)
- **`ui/dashboard/DashboardCards.kt`** — `DashboardStockAlertsCard` (badge, product, consultory · category, qty/min) + `StockAlertBadge`; "Ver inventario" navigates to Inventory
- **`ui/dashboard/DashboardGrid.kt`** — `stockAlerts` param; layout rebalanced (main 0.5 / status+alerts 0.2 / stock alerts 0.3 full-width row)
- **`ui/dashboard/ModernDashboardContent.kt`** — loads `listInventoryLines()`, maps + sorts alerts (OUT first); alert count from real lines
- Threshold editing deferred (min/max are derived defaults, no persisted columns); read-only DoD met

### Build
- ✅ BUILD SUCCESSFUL (0 errors) + smoke-run OK; no `DentiRepository.kt` changes

## TASK-002 — Inventory dashboard & movement insights

### Insights (`ui/inventory/StockUiModels.kt`)
- `StockMovementUiModel` now carries optional `note` + `balance`; `InventoryMovementRow.toUiModel(balance)`
- New derived models: `StockInsights`, `StockReorderSuggestion`, `StockCategoryStat`
- New pure builders: `buildStockInsights()` (30-day window), `buildStockCategoryStats()`, `stockStatusRank()`

### Insights UI (`ui/inventory/StockInsightsPanel.kt`, new)
- `StockInsightsRow` — KPI cards: Entradas (30d), Salidas (30d), Movimientos (30d), Necesitan reposición
- `StockInsightsPanel` — "Unidades por categoría" bar chart (`EnterpriseBarChart`) + top-10 "Reposición sugerida" with order quantities

### Wiring & side panel
- `ModernStockContent.kt` — computes insights, renders KPI row, right-slot logic (details ≥1200 dp else insights ≥1000 dp), running balance per movement
- `StockListComponents.kt` — movement history rows show reason **note** + running **Stock** balance; `categoryColors` exposed for reuse

### Build
- ✅ BUILD SUCCESSFUL (0 errors) + smoke-run OK; no `DentiRepository.kt` changes

## TASK-001 — Inventory CRUD (stock, adjustments, transfers, export)

### Repository (`data/DentiRepository.kt`)
- **`listTreatmentFacilities()`** + new `TreatmentFacilityRow` model
- **`registerInventoryLine()`** — new stock line + `RESTOCK` movement; duplicate guard
- **`adjustInventoryStock()`** — signed delta via `applyQuantityChange`; non-negative guard; `ADJUSTMENT` movement
- **`transferInventoryStock()`** — atomic debit/credit between consultories; `TRANSFER` movements on both sides; creates destination line if missing
- `findInventoryLine()` helper

### Export
- **`export/InventoryExport.kt`** (new) — `renderInventoryCsv()` (`;`-separated, Excel-friendly)

### UI
- **`InventoryStockScreen.kt`** — orchestrates dialogs, IO work, reload + snackbar feedback; CSV export via native save dialog
- **`ui/inventory/StockDialogs.kt`** (new) — `NewStockDialog`, `EditStockDialog`, `AdjustStockDialog`, `TransferStockDialog`
- **`ui/inventory/ModernStockContent.kt`** — real `onEdit`/`onAdjustStock`/`onTransfer` callbacks replace the placeholder funnel
- **`ui/inventory/StockUiModels.kt`** — `StockUiModel.toExportRow()`

### Build
- ✅ BUILD SUCCESSFUL (0 errors) + smoke-run OK
