# TASK-004: Treatment Management

## Objective

Elevate the treatment catalog from basic CRUD to a production-ready management module with categories, advanced filtering, sorting, color/icon support, and favorites — providing a rich, organized browsing experience for procedure types.

## Changes

### Database (`Tables.kt`)
- **Added** `TreatmentCategoriesTable` — `name` (unique, not null), `icon`, `color`, `sortOrder` (default 0), `isActive` (default true), `isArchived` (default false), `createdAtEpochMs`, `updatedAtEpochMs`
- **Extended** `ProcedureTypesTable` — 7 new columns: `categoryId` (FK → `TreatmentCategoriesTable.id`, SET NULL), `currency` (default "BOB"), `color`, `icon`, `isFavorite` (default false), `notes`, `createdAtEpochMs`, `updatedAtEpochMs`
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- **Added** `TreatmentCategory` data class — `id`, `name`, `icon`, `color`, `sortOrder`, `isActive`, `isArchived`, `createdAtEpochMs`, `updatedAtEpochMs`
- **Added** `CategoryRegisterRequest` — `name`, `icon`, `color`, `sortOrder`, `isActive`
- **Added** `CategoryUpdateRequest` — same fields as register
- **Extended** `ProcedureTypeRow` — 7 new fields matching table columns
- **Extended** `ProcedureTypeRegisterRequest` — `categoryId`, `currency`, `color`, `icon`, `isFavorite`, `notes`
- **Extended** `ProcedureTypeUpdateRequest` — same 7 new fields

### Repository (`DentiRepository.kt`)

**DRY refactoring:**
- **Extracted** `procedureTypeFromRow()` helper — eliminates 7 duplicated row-mapping methods (list, listAll, find, search)
- **Extracted** `categoryFromRow()` helper — maps `TreatmentCategoriesTable` → `TreatmentCategory`

**Category CRUD — 10 new methods:**
- `listCategories()` — active, non-archived categories sorted by sortOrder/name
- `listAllCategoriesIncludingArchived()` — for category manager UI
- `findCategoryById(id)` — lookup by ID
- `findCategoryByName(name, excludeId?)` — duplicate name detection
- `registerCategory(request)` — create with duplicate check, returns category ID
- `updateCategory(id, request)` — update fields
- `archiveCategory(id)` — soft delete
- `restoreCategory(id)` — restore from archive
- `hardDeleteCategory(id)` — permanent delete, rejects if procedure types reference the category
- `toggleFavoriteProcedureType(id)` — flips `isFavorite` boolean

**Updated procedure methods:**
- All procedure type list/find/search methods now map `categoryId`, `currency`, `color`, `icon`, `isFavorite`, `notes`, timestamps
- Registration and update methods write all new columns

### Seeders
- **Created** `TreatmentCategoriesSeeder.kt` — 11 default categories with Material icons and preset colors (e.g., "Preventiva" / shield / #4CAF50, "Ortodoncia" / straighten / #2196F3)
- **Updated** `ProceduresSeeder.kt` — links each procedure to a category by name, sets `currency = "BOB"`, `createdAtEpochMs`
- **Updated** `DemoDataSeeder.kt` — seeds categories before procedures; emptiness check includes `TreatmentCategoriesTable`

### UI — ProceduresScreen (`ProceduresScreen.kt`) — Full rewrite
- **SearchAndFiltersBar** — real-time text search (name, description, category), category filter chips, status filter chips (active/inactive), favorites-only toggle, sort dropdown (name/price/duration/category/status) with ASC/DESC toggle
- **ProcedureTypeCard** — color dot from hex, favorite star toggle, category chip, action dropdown
- **ProcedureTypeFormDialog** — name with validation, description, duration, price, currency select, category dropdown, color preset palette (12 colors), icon picker, requiresTooth checkbox, isActive toggle, notes
- **Category management** — `CategoryManagerDialog` (list + archive/restore/delete), `CategoryFormDialog` (create/edit with color palette), `CategoryDeleteDialog` (referential integrity warning)
- **Treatment status/delete dialogs** — unchanged from TASK-003

### Utility (`Format.kt`)
- **Added** `parseHexColor(hex)` — Compose-safe hex color parser (replaces `android.graphics.Color.parseColor` which is unavailable on Desktop)

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added `TreatmentCategoriesTable`; extended `ProcedureTypesTable` with 7 columns |
| `data/Models.kt` | Added `TreatmentCategory`, `CategoryRegisterRequest`, `CategoryUpdateRequest`; extended 3 existing types |
| `data/DentiRepository.kt` | 2 DRY helpers + 10 category methods + `toggleFavoriteProcedureType` + updated procedure methods |
| `data/seeders/TreatmentCategoriesSeeder.kt` | **New file** — 11 default categories |
| `data/seeders/ProceduresSeeder.kt` | Links categories, sets new columns |
| `data/seeders/DemoDataSeeder.kt` | Category seeding orchestration |
| `ui/ProceduresScreen.kt` | Full rewrite: filters, sorting, favorites, category management, color/icon support |
| `ui/Format.kt` | Added `parseHexColor()` utility |

## Build Status

✅ BUILD SUCCESSFUL (0 errors)

## Acceptance Criteria

- ✓ 11 treatment categories seeded with icons and colors
- ✓ Procedure types linked to categories via FK
- ✓ Category manager: create, edit, archive, restore, hard delete categories
- ✓ Category delete rejected if procedure types reference the category
- ✓ Category form with color palette picker
- ✓ Search by name, description, or category name
- ✓ Filter by category (chips), status (active/inactive), favorites
- ✓ Sort by name, price, duration, category, status — ASC/DESC toggle
- ✓ Favorite toggle on each procedure type card (star icon)
- ✓ Color dot displayed on cards, color picker in form
- ✓ Currency field (defaults to BOB)
- ✓ Notes field on procedure types
- ✓ Validation: required name, positive price, numeric duration, duplicate name check
- ✓ Snackbar feedback on all operations
- ✓ Loading/busy states on all async operations
