# Milestone 4 — Changelog

All notable changes to the Inventory Management milestone.

## TASK-004 — Suppliers & purchase orders

### Schema & models
- **`data/Tables.kt`** — `PurchaseOrdersTable` (order_id, supplier_id FK→Suppliers SET_NULL, status default PENDING, order_date, received_at, notes, total_cost) + `PurchaseOrderItemsTable` (order_item_id, order_id FK→PurchaseOrders CASCADE, consultory_id, facility_id, quantity, unit_cost); both added to the migration list in `Database.kt`
- **`data/Models.kt`** — `PurchaseOrderRow`, `PurchaseOrderItemRow`, `PurchaseOrderItemRequest`, `PurchaseOrderRegisterRequest`

### Repository (`data/DentiRepository.kt`)
- `listPurchaseOrders()`, `getPurchaseOrderItems()`, `registerPurchaseOrder()` (qty>0 required, total = Σ qty·cost)
- `receivePurchaseOrder()` — PENDING-only, transactional: upserts `material_inventory_lines`, records `RESTOCK` movements (`"Recepción de pedido #N"`), marks order RECEIVED
- `deletePurchaseOrder()` — PENDING-only; deletes items then order

### UI
- **`ui/inventory/SuppliersDialog.kt`** (new) — supplier list + register/edit form (name, contact, phone, email, address, notes) + delete confirm
- **`ui/inventory/PurchaseOrderDialogs.kt`** (new) — `PurchaseOrdersDialog` (list, "Recibir" credits stock, delete pending) + `NewPurchaseOrderDialog` (supplier, notes, editable lines, "Sugerir reposición" prefill, live total)
- **`ui/inventory/StockListComponents.kt`** — `StockPageHeader` adds "Proveedores" + "Pedidos" buttons
- **`ui/InventoryStockScreen.kt`** — loads suppliers/orders, computes suggested order items, hosts new dialogs (duplicate-name guards)
- **`ui/layout/AppShell.kt`** — Inventory route now renders `InventoryStockScreen` (modern screen); tabbed M2 screen stays unused

### Build
- ✅ BUILD SUCCESSFUL (0 errors) + smoke-run OK

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
