# Milestone 4 — Changelog

All notable changes to the Inventory Management milestone.

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
