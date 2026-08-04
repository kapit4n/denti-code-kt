# Next Task

## Milestone 4 — Inventory Management

## TASK-001: Inventory CRUD (stock, adjustments, movements)

**Objective**: Turn the Inventory screen from read-only display into working CRUD — matching the treatment plan implementation pattern used in Milestone 3.

**Scope**:
- **Repository**: `registerInventoryLine()` (new item), `updateStockQuantity()`/`recordInventoryMovement()` (adjustments), stock transfer between facilities (credit/debit) and closing the loop on `InventoryMovementsTable` writes. Refresh the `DentiRepository.kt` line count in `REFACTORING.md` first and only split if a future task already rewrites a section (no standalone mega-split).
- **UI — InventoryStockScreen / ModernStockContent.kt**: wire the stubbed actions — "Nuevo insumo" (register dialog), item edit, "Ajustar stock" (movement dialog), transfer between consultories, and export. Match the `AppBasicDialog` + `AppButton` conventions and `Bs` currency from the payments/patient-detail screens.
- **Seeders**: keep `InventorySeeder.kt` but make sure new items persist via the repository.
- **Status**: refresh `docs/STATUS.md` (inventory row: PARTIAL → DONE; move "New inventory item" / "Export inventory" / "item actions" from the stub list to the working list).

**Definition of done**: new/edit/adjust/transfer/export all persist through real repository calls, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous task: **TASK-005 — Clinical Workspace Review & Polish** (Milestone 3) is DONE — see `docs/milestones/milestone-03-patient/TASK-005.md`. Milestone 3 is complete.
