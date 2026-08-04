# Milestone 4 — Inventory Management

## Goal

Turn the read-only stock list into a complete inventory module: working stock CRUD (register, edit, adjust, transfer), movement history, stock-status KPIs, alerts and replenishment support.

## Features

- Stock table by consultory with filters (search, consultory, category, status, low-stock-only) and pagination
- Register new stock lines (consultory + insumo + initial quantity) — duplicates rejected
- Edit total quantity and apply ± adjustments with an optional reason
- Transfers between consultories (creates the destination line if missing)
- Movement history per line (side panel) fed by real `InventoryMovementsTable` rows
- CSV export (native save dialog, `;`-separated)
- Stock status badges (Óptimo / Bajo / Agotado) and KPIs
- Inventory dashboard: 30-day movement KPIs (entradas/salidas/movimientos), category breakdown chart and top-10 replenishment suggestions
- Movement history side panel with type, reason note and running stock balance
- Stock alerts on the Dashboard: dedicated card listing low/out-of-stock lines (consultory + category + qty/min) with navigation to the inventory screen
- Suppliers catalog (register/edit/delete) with contact data
- Purchase orders (supplier, line items, qty/cost, total) — receiving a pending order credits per-consultory stock and records `RESTOCK` movements; "Sugerir reposición" prefills lines from real stock statuses

## Deliverable

Working inventory management.

## Tasks

| Task | Description | Status |
|------|-------------|--------|
| TASK-001 | Inventory CRUD (register/edit/adjust/transfer + CSV export) | DONE |
| TASK-002 | Inventory dashboard & movement insights | DONE |
| TASK-003 | Stock alerts & replenishment suggestions | DONE |
| TASK-004 | Suppliers & purchase orders | DONE |
| TASK-005 | Inventory audit & final review | DONE |

## Status

**✅ COMPLETE** — Inventory is a working module: stock CRUD, movement engine, insights, alerts, suppliers and purchase orders, verified by a 26-check data-layer probe.
