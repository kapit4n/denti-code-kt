# Next Task

## Milestone 4 — Inventory Management

## TASK-004: Suppliers & purchase orders

**Objective**: Add supplier management and purchase-order support to the inventory module.

**Scope** (provisional — confirm against `docs/milestones/milestone-04-inventory/README.md` and current schema `docs/db/tables.md`):
- **Suppliers catalog**: CRUD screen or dialog for suppliers (name, contact, phone, RUC/NIT if desired), backed by a new `suppliers` table + repository methods (`registerSupplier`, `listSuppliers`, `updateSupplier`, `deleteSupplier` if the milestone requires it).
- **Purchase orders**: register purchase orders (supplier, line items, quantities, cost, status pending/received) that, when received, apply a `RESTOCK`-style movement through the existing stock engine — reuse `adjustInventoryStock`/`applyQuantityChange` where possible.
- **Integration**: wire replenishment suggestions from TASK-002/TASK-003 into "new purchase order" prefills; link orders to inventory movements.
- **Status**: refresh `docs/STATUS.md` (inventory row notes + CRUD matrix) and update `docs/db/tables.md` for any new tables.

**Definition of done**: suppliers + purchase orders persist through real repository calls, receiving an order updates stock with recorded movements, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous task: **TASK-003 — Stock alerts & threshold notifications** (Milestone 4) is DONE — see `docs/milestones/milestone-04-inventory/TASK-003.md`.
