# Milestone 2 — Complete CRUD

## Goal

Complete every missing CRUD operation across all entities. Every entity should be fully manageable (Create, Read, Update, Delete/Soft-delete).

## Current Gap Analysis

| Entity | Create | Read | Update | Delete | Priority |
|--------|--------|------|--------|--------|----------|
| Patients | ✅ DONE | ✅ DONE | ❌ MISSING | ❌ MISSING | HIGH |
| Procedures | ✅ DONE | ✅ DONE | ❌ MISSING | ❌ MISSING | HIGH |
| Payments | ✅ DONE | ✅ DONE | ❌ MISSING | ❌ MISSING | HIGH |
| Inventory Lines | ❌ MISSING | ✅ DONE | ❌ MISSING | ❌ MISSING | HIGH |
| Inventory Movements | ❌ MISSING | ✅ DONE | N/A | N/A | MEDIUM |
| Consultories | Seed only | ✅ DONE | ❌ MISSING | ❌ MISSING | MEDIUM |
| Facilities | Seed only | Indirect | ❌ MISSING | ❌ MISSING | LOW |

## Tasks

| Task | Description | Est. Files |
|------|-------------|-----------|
| TASK-001 | Patient Edit — wire up edit dialog with `updatePatient()` | 3-4 |
| TASK-002 | Patient Delete — add confirm dialog + soft delete | 2-3 |
| TASK-003 | Procedure Type Edit — add update dialog | 2-3 |
| TASK-004 | Procedure Type Delete — add confirm + delete | 2-3 |
| TASK-005 | Payment Edit — wire up edit/void payment dialog | 3-4 |
| TASK-006 | Payment Void — add void confirm dialog | 2-3 |
| TASK-007 | Inventory Create — add new item dialog + `createInventoryLine()` | 3-4 |
| TASK-008 | Inventory Update — wire up edit item dialog | 2-3 |
| TASK-009 | Inventory Adjust Stock — add adjust dialog + movement record | 2-3 |
| TASK-010 | Inventory Movement History — wire up `recordInventoryMovement()` | 2-3 |
| TASK-011 | Consultory CRUD — full management UI | 3-4 |
| TASK-012 | Facility CRUD — full management UI | 3-4 |

## Design Principles

1. **Edit dialogs** should reuse create dialog composables where possible
2. **Delete** should be soft-delete (status flag) where the schema supports it
3. **All operations** must update the UI reactively after completion
4. **Validation** required for all input fields
5. **Snackbar messages** for success/failure feedback

