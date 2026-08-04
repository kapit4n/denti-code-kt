# Next Task

## Milestone 4 — Inventory Management

## TASK-005: Inventory audit & final review

**Objective**: Close out Milestone 4 with an audit pass over the whole inventory module.

**Scope** (provisional — confirm against `docs/milestones/milestone-04-inventory/README.md` and current schema `docs/db/tables.md`):
- **Cross-cutting review**: verify stock CRUD, movement engine, insights, alerts, suppliers and purchase orders behave correctly together (e.g. receiving an order → stock line + `RESTOCK` movement → KPI/insights/alert updates).
- **Edge cases**: duplicate-supplier/duplicate-line guards, receiving/editing edge states, delete of suppliers referenced by orders (SET_NULL), zero/negative quantities.
- **Polish**: any remaining UI inconsistencies in the modern stock screen and its dialogs; movement-history, insights and alert consistency.
- **Status**: final refresh of `docs/STATUS.md` (version bump, CRUD matrix incl. Suppliers + Purchase Orders) and update `docs/db/tables.md` for the purchase-order tables.
- **Milestone close**: mark Milestone 4 complete in `progress.md`/`MASTER_ROADMAP.md` if the audit passes.

**Definition of done**: audit findings addressed (or documented as deferred), `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous task: **TASK-004 — Suppliers & purchase orders** (Milestone 4) is DONE — see `docs/milestones/milestone-04-inventory/TASK-004.md`.
