# TASK-005: Inventory Management

## Objective

Complete inventory module for dental supplies: product catalog with stock tracking, categories, suppliers, stock adjustments, and history — reusing the CRUD framework.

## Changes

### Database (`Tables.kt`)
- **Added** `InventoryCategoriesTable` — `name`, `description`, `icon`, `color`, `sortOrder`, `isActive`, `isArchived`, timestamps
- **Added** `SuppliersTable` — `name`, `contactName`, `phone`, `email`, `address`, `notes`, `isActive`, `isArchived`, timestamps
- **Added** `InventoryProductsTable` — `name`, `code` (unique per query), `description`, `categoryId` (FK → InventoryCategoriesTable), `unit`, `purchasePrice`, `sellingPrice`, `currentStock`, `minStock`, `maxStock`, `supplierId` (FK → SuppliersTable), `expirationDate`, `barcode`, `color`, `icon`, `notes`, `isActive`, `isArchived`, timestamps
- **Added** `InventoryProductMovementsTable` — `productId` (FK), `quantityChange`, `type`, `note`, `createdAtEpochMs`
- All tables registered in `Database.kt` for auto-migration

### Models (`Models.kt`)
- **Added** `InventoryProductStatus` enum — ACTIVE, LOW_STOCK, OUT_OF_STOCK, EXPIRED (with `labelEs`)
- **Added** `resolveInventoryProductStatus()` — computes status from stock, minStock, expirationDate
- **Added** `InventoryProduct`, `InventoryProductRegisterRequest`, `InventoryProductUpdateRequest`
- **Added** `InventoryProductCategory`, `InventoryCategoryRegisterRequest`, `InventoryCategoryUpdateRequest`
- **Added** `Supplier`, `SupplierRegisterRequest`, `SupplierUpdateRequest`
- **Added** `InventoryProductMovementRow`, `InventoryProductKpis`

### Repository (`DentiRepository.kt`)

**DRY helpers:**
- `inventoryProductFromRow()` — maps joined product/category/supplier rows
- `inventoryCategoryFromRow()` — maps category rows
- `supplierFromRow()` — maps supplier rows

**Category CRUD — 9 methods:**
- `listInventoryCategories()`, `listAllInventoryCategoriesIncludingArchived()`
- `findInventoryCategoryById()`, `findInventoryCategoryByName()`
- `registerInventoryCategory()`, `updateInventoryCategory()`
- `archiveInventoryCategory()`, `restoreInventoryCategory()`, `hardDeleteInventoryCategory()`

**Supplier CRUD — 9 methods:**
- `listSuppliers()`, `listAllSuppliersIncludingArchived()`
- `findSupplierById()`, `findSupplierByName()`
- `registerSupplier()`, `updateSupplier()`
- `archiveSupplier()`, `restoreSupplier()`, `hardDeleteSupplier()`

**Product CRUD — 13 methods:**
- `listInventoryProducts()`, `listAllInventoryProductsIncludingArchived()`
- `findInventoryProductById()`, `findInventoryProductByName()`, `findInventoryProductByCode()`
- `searchInventoryProducts(query)` — DB-level search by name, code, description, notes
- `registerInventoryProduct()` — with initial stock movement recording
- `updateInventoryProduct()`, `adjustInventoryProductStock()` — updates stock + records movement
- `listInventoryProductMovements()`
- `archiveInventoryProduct()`, `restoreInventoryProduct()`, `hardDeleteInventoryProduct()`
- `loadInventoryProductDirectory()` — returns KPIs + product list

### Seeders
- **Created** `InventoryCategoriesSeeder.kt` — 12 default categories (Material restaurativo, Anestesia, EPP, etc.)
- **Created** `SuppliersSeeder.kt` — 5 sample dental suppliers
- **Created** `InventoryProductsSeeder.kt` — 34 products across categories with realistic prices, stock levels, and movement history
- **Updated** `DemoDataSeeder.kt` — orchestrates new seeders, emptiness check includes InventoryCategoriesTable + SuppliersTable

### UI — InventoryManagementScreen.kt (new)
- **Tabbed interface**: Productos | Categorías | Proveedores
- **Dashboard KPIs**: Total products, units, low stock, out of stock, expiring soon, total value (Bs)
- **Products tab**: search by name/code/category/supplier, category filter dropdown, low-stock-only checkbox, product table with color dot, status chip, price, supplier
- **Product form dialog**: name, code, category dropdown, description, purchase/selling price, stock levels (current/min/max), unit dropdown, supplier dropdown, expiration date, barcode, color palette, notes, isActive
- **Stock adjustment dialog**: type (Entrada/Consumo/Ajuste/Transferencia), quantity, note — auto-negates for consumption
- **Stock history dialog**: chronological movement list with type labels and +/- quantity colors
- **Categories tab**: list with color dot, create/edit with color palette, archive/restore, delete
- **Suppliers tab**: list with contact info, create/edit, archive/restore, delete
- **Delete confirmation dialogs** with warning icon
- **Category manager dialog**: embedded in products tab for quick category CRUD
- All operations use `Validators.required`, `Validators.positiveNumber`, `Validators.length` from CRUD framework
- All operations use `AppSurfaceDialog`, `AppTextField`, `AppDropdownField`, `AppButton`, `AppOutlinedButton`, `AppTextArea` from component library

### Navigation (`AppShell.kt`)
- `ScreenRoute.Inventory` now routes to `InventoryScreen` (replaced `InventoryStockScreen`)

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added 4 new tables |
| `data/Models.kt` | Added 11 data classes/enums/functions |
| `data/DentiRepository.kt` | 3 DRY helpers + 31 new methods |
| `data/Database.kt` | Registered 4 new tables |
| `data/seeders/InventoryCategoriesSeeder.kt` | **New** — 12 categories |
| `data/seeders/SuppliersSeeder.kt` | **New** — 5 suppliers |
| `data/seeders/InventoryProductsSeeder.kt` | **New** — 34 products |
| `data/seeders/DemoDataSeeder.kt` | Updated orchestration |
| `ui/InventoryManagementScreen.kt` | **New** — full inventory UI |
| `ui/layout/AppShell.kt` | Routes Inventory → InventoryScreen |

## Build Status

✅ BUILD SUCCESSFUL (0 errors)

## Acceptance Criteria

- ✓ 12 inventory categories seeded
- ✓ 5 suppliers seeded
- ✓ 34 products seeded with stock and movement history
- ✓ Product CRUD: create, edit, archive, restore, hard delete
- ✓ Duplicate name and code detection on create/edit
- ✓ Category management: create, edit, archive, restore, delete (with referential integrity)
- ✓ Supplier management: create, edit, archive, restore, delete (with referential integrity)
- ✓ Stock adjustment: entry, consumption, adjustment, transfer — with automatic stock recalculation
- ✓ Stock history: chronological movement list per product
- ✓ Search by name, code, description, notes
- ✓ Filter by category, low-stock-only toggle
- ✓ Dashboard KPIs: total products, units, low stock, out of stock, expiring soon, total value
- ✓ Status chips: Activo, Stock bajo, Agotado, Vencido
- ✓ Validation: required name/code, positive prices, numeric fields
- ✓ Snackbar feedback on all operations
- ✓ Loading states
