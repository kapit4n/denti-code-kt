# TASK-005: Inventory audit & final review

## Objective

Close out Milestone 4 (Inventory Management) with an audit pass over the whole module: cross-cutting behavior, edge cases, data-layer invariants and UI consistency. Every fix verified with a temporary in-memory/temp-file SQLite probe that exercised the real repository methods (26 checks), plus a full build and app smoke-run.

## Audit findings & fixes

### 1. Supplier name uniqueness not enforced at the data layer (`data/DentiRepository.kt`)
- The duplicate-name guard only existed in the UI (`InventoryStockScreen`); `registerSupplier`/`updateSupplier` accepted duplicates from any other caller.
- **Fix**: `registerSupplier` now requires no other supplier with the same name (case-insensitive, via `findSupplierByName`); `updateSupplier` enforces the same excluding self. UI pre-check retained.

### 2. Supplier deletion vs pending purchase orders (`data/DentiRepository.kt`)
- `hardDeleteSupplier` blocked deletion when products referenced the supplier, but not when **pending purchase orders** did (FK is SET_NULL), which silently orphaned open orders.
- **Fix**: deletion is now also blocked when the supplier has `PENDING` purchase orders. Received orders (SET_NULL) still allow deletion.

### 3. Duplicated reorder-suggestion logic (`ui/inventory/StockUiModels.kt`, `ui/InventoryStockScreen.kt`)
- The "Sugerir reposición" prefill in `InventoryStockScreen` re-implemented the filter/sort/take-10 logic already in `buildStockInsights`, risking divergence.
- **Fix**: extracted a shared `underOptimalUiModels()` builder (OUT first, then alphabetical) used by both `buildStockInsights` and the new `buildReorderOrderItems()` (→ `List<PurchaseOrderItemRequest>`); the screen now calls the helper.

### 4. Locale-sensitive money formatting (`ui/inventory/PurchaseOrderDialogs.kt`)
- `"%,.2f".format(value).replace(',', ' ')` produced wrong output under es-ES locale (thousands separator became the decimal group).
- **Fix**: explicit `String.format(Locale.US, "%,.2f", value)` → stable `Bs 1 234.56` format. Also surfaced `errorMessage` in the suppliers list view (was only shown in the form).

## Runtime probe (temporary, removed after use)

A `DentiRepository` integration probe against an isolated SQLite file verified 26 behaviors, all passing:

- **Suppliers**: register, list, case-insensitive find, update, duplicate-name rejection (after fix), delete blocked while a pending order references the supplier, delete allowed once orders are resolved.
- **Purchase orders**: register with lines + total (`Σ qty·cost`), list fields (supplier name, item count, total, status), zero-quantity-only order rejected, receive credits existing lines and creates missing lines, two `RESTOCK` movements (`"Recepción de pedido #N"`), double-receive rejected, received orders cannot be deleted, pending orders can.
- **Stock engine**: register line (initial movement), transfer debits/credits atomically, self-transfer rejected, over-transfer rejected, adjust applies delta, negative stock rejected, duplicate line rejected, directory KPIs computed.

## Build Status

✅ BUILD SUCCESSFUL (0 errors) + app smoke-run (75 s, no crash). Probe: `ALL CHECKS PASSED`.

## Acceptance Criteria

- ✓ Cross-cutting flows verified end-to-end at the data layer (register → order → receive → movement → KPI)
- ✓ Edge cases guarded: duplicate suppliers/lines, negative stock, self/over-transfer, zero-quantity orders, double-receive, delete of received orders, supplier delete with pending orders
- ✓ Reorder-suggestion logic deduplicated; money formatting locale-safe
- ✓ `./gradlew build` passes with 0 errors + app smoke-runs
- ✓ Docs refreshed; Milestone 4 marked complete

## Milestone Close

Milestone 4 — Inventory Management is **COMPLETE**: TASK-001..005 done. Next milestone: **Milestone 5 — Reports** (`docs/roadmap/NEXT_TASK.md`).
