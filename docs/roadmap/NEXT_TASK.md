# Next Task

## Milestone 4 — Inventory Management

## TASK-003: Stock alerts & threshold notifications

**Objective**: Make the stock thresholds actionable — surface low/out-of-stock lines and alert on the Home/Dashboard without new persistence.

**Scope** (provisional — confirm against `docs/milestones/milestone-04-inventory/README.md`):
- **Dashboard alerts**: a stock-alert card on the Dashboard (`ui/dashboard`) listing `StockStatus.LOW`/`OUT` lines with consultory + category, driving from `listInventoryLines()` + `resolveInventoryStockStatus`.
- **Alert settings**: optional per-line/per-category reorder threshold controls (`minQuantity`/`maxQuantity` edits) if not already covered by TASK-001 "Editar".
- **Status**: refresh `docs/STATUS.md` (inventory row notes) and keep `DentiRepository.kt` untouched unless a rewrite is needed.

**Definition of done**: stock alerts surface on the Dashboard from real repository reads, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous task: **TASK-002 — Inventory dashboard & movement insights** (Milestone 4) is DONE — see `docs/milestones/milestone-04-inventory/TASK-002.md`.
