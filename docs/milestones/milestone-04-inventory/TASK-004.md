# TASK-004: Suppliers & purchase orders

## Objective

Add supplier management and purchase-order support to the inventory module, and wire the modern stock screen (`InventoryStockScreen`) into the Inventory route (replacing the M2-era tabbed `InventoryScreen`). Receiving a purchase order credits the per-consultory stock through the real movement engine.

## Changes

### Schema (`data/Tables.kt`, `data/Database.kt`)
- **`PurchaseOrdersTable`** — `order_id` PK, `supplier_id` FK → `Suppliers` (SET_NULL), `status` (default `PENDING`), `order_date`, `received_at` (nullable), `notes` (nullable), `total_cost`.
- **`PurchaseOrderItemsTable`** — `order_item_id` PK, `order_id` FK → `PurchaseOrders` (CASCADE), `consultory_id` FK, `facility_id` FK, `quantity`, `unit_cost`.
- Both added to the `SchemaUtils.createMissingTablesAndColumns` migration list in `Database.kt`.

### Models (`data/Models.kt`)
- `PurchaseOrderRow` (id, supplierId, supplierName, status, orderDateEpochMs, receivedAtEpochMs, notes, totalCost, itemCount), `PurchaseOrderItemRow`, `PurchaseOrderItemRequest`, `PurchaseOrderRegisterRequest` (supplierId?, notes?, items).

### Repository (`data/DentiRepository.kt`)
- `listPurchaseOrders()` — left join to Suppliers, order date DESC, item counts.
- `getPurchaseOrderItems(orderId)`.
- `registerPurchaseOrder(request)` — requires ≥1 line with qty > 0; `totalCost = Σ qty·unitCost`.
- `receivePurchaseOrder(orderId)` — transactional: requires PENDING; upserts `material_inventory_lines` quantity per item; records `InventoryMovementsTable` `RESTOCK` rows with note `"Recepción de pedido #N"`; marks order RECEIVED + `received_at`.
- `deletePurchaseOrder(orderId)` — only PENDING orders; deletes item rows then the order row.

### UI (`ui/inventory/SuppliersDialog.kt`, `ui/inventory/PurchaseOrderDialogs.kt`, new)
- **`SuppliersDialog`** — list of suppliers (name, contact · phone · email) with register / edit / delete. Edit + create share a `SupplierFormDialog` (name, contact, phone, email, address, notes). Deletion confirms first.
- **`PurchaseOrdersDialog`** — list of orders (#, supplier, date, line count, total, status pill). Pending orders have **Recibir** (credits stock) and delete actions; received orders are read-only.
- **`NewPurchaseOrderDialog`** — supplier dropdown + notes + editable line rows (consultory → insumo → qty → unit cost) with **Sugerir reposición** prefill from the top-10 under-optimal lines (qty = max − current, min 1) and live total.
- **`StockListComponents.kt`** — `StockPageHeader` gains **Proveedores** and **Pedidos** outlined buttons.

### Wiring (`ui/inventory/ModernStockContent.kt`, `ui/InventoryStockScreen.kt`, `ui/layout/AppShell.kt`)
- `ModernStockContent` threads `onSuppliersClick`/`onOrdersClick` through the header.
- `InventoryStockScreen` loads `listSuppliers()` + `listPurchaseOrders()`, computes suggested order items from under-optimal lines, and hosts the three new dialogs (duplicate-name guards reuse `findSupplierByName`).
- `AppShell.kt` routes `ScreenRoute.Inventory → InventoryStockScreen(repo)` — the modern stock screen (with suppliers + purchase orders) is now the Inventory screen. The M2 tabbed `InventoryManagementScreen` remains in the codebase, unused.

## Build Status

✅ BUILD SUCCESSFUL (0 errors) + app smoke-run (75 s, no crash).

## Acceptance Criteria

- ✓ Suppliers persist via `registerSupplier`/`updateSupplier` (duplicate-name guarded) and can be deleted
- ✓ Purchase orders persist (`registerPurchaseOrder`) with line items and computed total
- ✓ Receiving an order (`receivePurchaseOrder`) credits per-consultory stock (`material_inventory_lines`) and records `RESTOCK` movements
- ✓ Pending orders can be deleted; received orders cannot
- ✓ "Sugerir reposición" prefills the new-order dialog from real stock statuses
- ✓ Inventory route shows the modern screen with Proveedores/Pedidos actions
- ✓ `./gradlew build` passes with 0 errors + app smoke-runs

## Next Task

Milestone 4 — Inventory Management. TASK-005 (inventory audit & final review) — see `docs/roadmap/NEXT_TASK.md`.
