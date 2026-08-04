# Next Task

## Milestone 4 — Inventory Management

## TASK-002: Inventory dashboard & movement insights

**Objective**: Surface the inventory data already stored/collected in TASK-001 — movement-driven insights over the read-only stock table, without new storage.

**Scope**:
- **KPIs / summary row**: keep total insumos / stock total / stock bajo / agotados (already computed), and add a movement-derived block (entradas/salidas de los últimos 30 días, valor estimado en `Bs`), reusing `listInventoryMovements(limit = 1000)`.
- **Movement history panel**: the side panel already lists recent movements per line; extend it with the movement type label (already mapped in `StockUiModels.movementTypeLabelEs`), the note text, and a running balance per line.
- **Category breakdown**: small chart/bar of units by category using the existing `categoryLabelEs` mapping (the app already ships a charts package — reuse the same patterns as the dashboard).
- **Replenishment suggestions**: for `StockStatus.LOW`/`OUT` lines, show "Pedido sugerido" = `maxQuantity − quantity` in a dedicated section (computed only, no new table).
- **Status**: refresh `docs/STATUS.md` (inventory row notes) and keep `DentiRepository.kt` untouched unless a rewrite is needed (no standalone split).

**Definition of done**: the Inventory route shows movement-derived insights computed from real repository reads, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous task: **TASK-001 — Inventory CRUD** (Milestone 4) is DONE — see `docs/milestones/milestone-04-inventory/TASK-001.md`.
