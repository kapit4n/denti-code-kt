# TASK-001: Inventory CRUD (stock, adjustments, transfers, export)

## Objective

Turn the Inventory screen from a read-only table into working CRUD: register new stock lines, set/edit the total, apply quantity adjustments, transfer units between consultories and export the stock to CSV — all backed by real repository calls on `MaterialInventoryLinesTable` / `InventoryMovementsTable`.

## Changes

### Repository (`data/DentiRepository.kt`)
- **`listTreatmentFacilities()`** — catalog of active facilities (code/category/displayName) for the register dialog.
- **`findInventoryLine(consultoryId, facilityId)`** — helper used for duplicate guards and transfer targets.
- **`registerInventoryLine(consultoryId, facilityId, quantity, note)`** — inserts a line (rejects duplicates) + records an `RESTOCK` movement.
- **`adjustInventoryStock(consultoryId, facilityId, quantityDelta, note)`** — applies a signed delta (via shared `applyQuantityChange`) with a non-negative guard, records an `ADJUSTMENT` movement. Used both by "Editar" (delta = target − current) and "Ajustar stock" (user-entered delta).
- **`transferInventoryStock(from, to, quantity, note)`** — atomic source debit + target credit (creating the target line if needed) with a sufficient-stock guard, recording `TRANSFER` movements on both sides.
- New model `TreatmentFacilityRow` in `data/Models.kt`.

### Export (`export/InventoryExport.kt`, new)
- `InventoryExportRow` + `renderInventoryCsv()` — Excel-friendly `;` CSV with the clinic's stock columns, mirroring `renderPaymentsCsv`.

### UI
- **`InventoryStockScreen.kt`** — now an action orchestrator: loads lines/movements/consultories/facilities, owns dialog state + `isSaving`/`errorMessage`, runs operations on `Dispatchers.IO`, reloads data and shows success/error snackbars; export via native save dialog (`ExportService.pickSaveFile`).
- **`ui/inventory/StockDialogs.kt`** (new) — `NewStockDialog` (consultory + searchable insumo filtered to those not already present + initial quantity), `EditStockDialog` (set new total), `AdjustStockDialog` (±delta with projected result + optional reason), `TransferStockDialog` (target consultory + quantity limited to available + optional note). Follow the `AppSurfaceDialog` + `AppDropdownField`/`AppNumberField`/`AppTextArea` + `AppButton` conventions.
- **`ui/inventory/ModernStockContent.kt`** — replaced the placeholder `onItemAction(item, "action")` funnel with real callbacks (`onEdit`, `onAdjustStock`, `onTransfer`); the row menu now dispatches the actual dialogs.
- **`ui/inventory/StockUiModels.kt`** — added `StockUiModel.toExportRow()`.

## Files Modified

| File | Change |
|------|--------|
| `data/Models.kt` | `TreatmentFacilityRow` model |
| `data/DentiRepository.kt` | 4 new inventory write methods + `listTreatmentFacilities` + shared `applyQuantityChange` |
| `export/InventoryExport.kt` | New CSV renderer |
| `ui/InventoryStockScreen.kt` | Rewritten as action orchestrator |
| `ui/inventory/StockDialogs.kt` | New: 4 dialogs (new/edit/adjust/transfer) |
| `ui/inventory/ModernStockContent.kt` | Real action callbacks wired |
| `ui/inventory/StockUiModels.kt` | `toExportRow()` mapper |

## Build Status

✅ BUILD SUCCESSFUL (0 errors) + app smoke-run (75 s, no crash).

## Acceptance Criteria

- ✓ "Nuevo insumo" persists a line + initial movement; duplicates rejected with a clear message
- ✓ "Editar" sets the total quantity; "Ajustar stock" applies ±delta with reason; negative stock is prevented
- ✓ "Transferir" moves units between consultories (creating the destination line if missing)
- ✓ "Exportar" writes a real `inventario-YYYY-MM-DD.csv` via the native save dialog
- ✓ Movements history panel reflects the new movements
- ✓ `./gradlew build` passes with 0 errors

## Next Task

Milestone 4 — Inventory Management. TASK-002 (inventory dashboard & movement insights) — see `docs/roadmap/NEXT_TASK.md`.
