# TASK-006: Inventory Operations & Stock Movement Engine

## Objective

Transform the inventory module into a complete inventory management system with real stock operations, movement types, history, treatment consumption architecture preparation, and enhanced dashboard with recent activity tracking.

## Changes

### Database (`Tables.kt`)
- **Enhanced** `InventoryProductMovementsTable` — added `previousStock`, `currentStock`, `unitCost`, `reason`, `referenceNumber`, `status`, `updatedAtEpochMs` columns
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- **Added** `InventoryMovementType` enum — PURCHASE, TREATMENT_CONSUMPTION, MANUAL_ADJUSTMENT, EXPIRED_DAMAGED_LOST, INVENTORY_CORRECTION, RETURN_TO_SUPPLIER, INITIAL_INVENTORY, STOCK_TRANSFER (with `labelEs` and `icon`)
- **Enhanced** `InventoryProductMovementRow` — added `previousStock`, `currentStock`, `unitCost`, `reason`, `referenceNumber`, `status`, `updatedAtEpochMs`
- **Added** `InventoryMovementStats` — todayEntries, todayConsumptions, todayAdjustments, todayValue, recentMovementsCount, lowStockAlerts
- **Added** `InventoryProductSummary` — quick product info for movement forms
- **Added** `TreatmentMaterialRequirement` — interface for treatment consumption architecture
- **Added** `InventoryConsumptionPlan` — interface for treatment consumption architecture

### Repository (`DentiRepository.kt`)
- **Enhanced** `adjustInventoryProductStock()` — validates quantities, records previous/current stock, requires reason, supports all movement types
- **Enhanced** `listInventoryProductMovements()` — supports pagination, type filtering, date range filtering
- **Added** `getInventoryMovementStats()` — dashboard statistics (today's entries/consumptions/adjustments)
- **Added** `getRecentMovementsWithProduct()` — movements with product names for timeline
- **Added** `searchInventoryProductsPaginated()` — paginated product search with filters
- **Added** `getLowStockProducts()` — products below minimum stock threshold
- **Added** `getExpiringProducts()` — products expiring within specified days
- **Added** `bulkAdjustStock()` — batch stock adjustments (transactional)

### UI — InventoryManagementScreen.kt (enhanced)
- **Enhanced** `StockAdjustmentDialog` — all movement types with proper labels, reason field (required), unit cost, reference number, validation
- **Enhanced** `StockHistoryDialog` — movement timeline with product names, type icons, +/- quantity colors, timestamps
- **Enhanced** `ProductDashboard` — recent movements panel, today's stats (entries/consumptions/value), stock indicators
- **Enhanced** `ProductsTab` — improved search with category/supplier/status filters, pagination
- **Added** `MovementTimelineItem` — visual timeline component with type icon, product name, quantity, reason
- **Added** `StockIndicator` — visual stock level indicator (bar with color coding)
- **Added** `LowStockAlertCard` — highlighted card for products requiring attention
- **Added** `TodayStatsCard` — dashboard card showing today's inventory activity

### Treatment Consumption Architecture (interfaces only)
- **Added** `TreatmentMaterialRequirement` interface — defines required materials for a treatment
- **Added** `InventoryConsumptionPlan` interface — defines planned consumption with quantities
- **Note**: Implementation deferred to future task; only architecture preparation in this task

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Enhanced `InventoryProductMovementsTable` with 7 new columns |
| `data/Models.kt` | Added `InventoryMovementType` enum, enhanced movement model, added stats/summary interfaces |
| `data/DentiRepository.kt` | Enhanced movement methods, added pagination/statistics/bulk operations |
| `ui/InventoryManagementScreen.kt` | Enhanced dialogs, dashboard, product list, added timeline/indicators |

## Build Status

✅ BUILD SUCCESSFUL (0 errors)

## Acceptance Criteria

- ✓ 8 movement types with Spanish labels and icons
- ✓ Enhanced movement table with previous/current stock tracking
- ✓ Reason required for all stock adjustments
- ✓ Unit cost and reference number optional fields
- ✓ Movement statistics for dashboard (today's activity)
- ✓ Paginated movement history with filters
- ✓ Low stock alerts and expiring products indicators
- ✓ Batch stock adjustments (transactional)
- ✓ Treatment consumption architecture interfaces (no implementation)
- ✓ Enhanced product search with pagination
- ✓ Visual stock indicators (color-coded bars)
- ✓ Movement timeline with product names and type icons
- ✓ Dashboard shows today's entries/consumptions/value
- ✓ All operations use existing CRUD framework components
- ✓ Validation: positive quantities, required reason, valid movement types
- ✓ Snackbar feedback on all operations
- ✓ Loading states
