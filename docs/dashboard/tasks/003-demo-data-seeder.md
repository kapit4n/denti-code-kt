# Task 003: Demo Data Seeder

## Objective

Create a comprehensive, idempotent demo data seeder that populates the SQLite database with realistic Bolivian dental clinic data for development and demonstration purposes. The seeder runs automatically after schema creation on first launch.

## Motivation

- The existing `Seed.kt` was minimal (2 users, 5 doctors, 8 patients)
- Dashboard needed realistic data volumes to demonstrate KPIs, charts, and alerts
- New features (payments, notes, audit) had no demo data at all
- Needed configurable data volumes for different development scenarios

## Files Created

| File | Lines | Description |
|---|---|---|
| `seeders/DemoDataConfig.kt` | 20 | Configuration data class with sensible defaults |
| `seeders/DemoDataSeeder.kt` | 36 | Orchestrator calling seeders in dependency order |
| `seeders/UsersSeeder.kt` | 27 | 2 users (admin + receptionist) |
| `seeders/DoctorsSeeder.kt` | 41 | 5 dentists with specialties |
| `seeders/PatientsSeeder.kt` | 142 | 150 patients (seeded RNG, Bolivian names) |
| `seeders/ProceduresSeeder.kt` | 91 | 28 dental procedures with Bs. pricing |
| `seeders/FacilitiesSeeder.kt` | 152 | 35 materials/supplies across 8 categories |
| `seeders/ConsultoriesSeeder.kt` | 21 | 3 consultories |
| `seeders/AppointmentsSeeder.kt` | 119 | 300 appointments (past 30d, today, next 15d) |
| `seeders/MaterialInventorySeeder.kt` | 81 | Inventory lines + stock movements per consultory |
| `seeders/PerformedActionsSeeder.kt` | 99 | Treatments for completed/in-progress appointments |
| `seeders/PaymentsSeeder.kt` | 120 | 500+ payments (full, partial, anticipos) |
| `seeders/NotesAuditSeeder.kt` | 95 | Appointment notes and audit logs |

## Files Modified

- `src/.../data/Database.kt` — added `DemoDataSeeder.seedIfEmpty()` call after existing seed functions

## Data Summary

| Table | Records | Notes |
|---|---|---|
| users | 2 | admin + recepcionista |
| doctors | 5 | specialties: Odontología General, Endodoncia, Cirugía, etc. |
| patients | 150 | Bolivian names, Cochabamba addresses |
| procedure_types | 28 | Full catalog with Bs. pricing (50–1,500 Bs.) |
| facilities | 35 | 8 categories (Insumos, Equipos, Instrumentos, etc.) |
| consultories | 3 | C-01, C-02, C-03 |
| appointments | 300 | Distributed: 60% past, 10% today, 30% future |
| material_inventory | 210 | 50 lines × 3 consultories |
| performed_actions | 200+ | For completed/in-progress appointments |
| payments | 500+ | Full payments, partial payments, anticipos |
| appointment_notes | ~500 | 1–3 notes per appointment |
| audit_logs | ~600 | CRUD operations across all entities |

## Pricing Catalog

All prices in Bolivianos (Bs.):

| Category | Procedures | Price Range |
|---|---|---|
| Prevención | Limpieza dental, Aplicación fluor, Selladores | 80–250 Bs. |
| Restaurativa | Resinas, Obturaciones, Carillas | 200–800 Bs. |
| Endodoncia | Pulpectomía, Endodoncia completa | 500–1,200 Bs. |
| Cirugía | Extracción simple, Extracción compleja, Implante | 200–1,500 Bs. |
| Ortodoncia | Aparatología, Ajuste, Control | 200–1,000 Bs. |
| Prótesis | Corona, Puente, Prótesis parcial/removible | 400–1,200 Bs. |
| Estética | Blanqueamiento, Carillas estéticas | 300–800 Bs. |
| Odontopediatría | Tratamiento infantil, Seda dental | 150–400 Bs. |

## Inventory Catalog (35 items across 8 categories)

- **Insumos desechables**: Guantes, jeringas, puntas de irrigación, etc.
- **Materiales de restauración**: Resinas, amalgama, cemento, etc.
- **Instrumentos**: Explorador, espejo, sonda, etc.
- **Material de endodoncia**: Guttapercha, hipoclorito, EDTA, etc.
- **Material de cirugía**: Suturas, bisturí, gasas, etc.
- **Material de prótesis**: Acrílico, metal, cerámica, etc.
- **Equipos**: Compresorr, unidad dental, rayos X, etc.
- **Limpieza y esterilización**: Autoclave, desinfectante, ultrasonido, etc.

## Seeder Execution Order

```
1. UsersSeeder          (no dependencies)
2. DoctorsSeeder        (no dependencies)
3. PatientsSeeder       (no dependencies)
4. ProceduresSeeder     (no dependencies)
5. FacilitiesSeeder     (no dependencies)
6. ConsultoriesSeeder   (no dependencies)
7. AppointmentsSeeder   (depends on: patients, doctors, procedures, consultories)
8. MaterialInventorySeeder (depends on: facilities, consultories)
9. PerformedActionsSeeder  (depends on: appointments, procedures, doctors)
10. PaymentsSeeder         (depends on: appointments, procedures)
11. NotesAuditSeeder       (depends on: appointments)
```

## Configuration

`DemoDataConfig` allows adjusting data volumes:

```kotlin
data class DemoDataConfig(
    val patientCount: Int = 150,
    val appointmentCount: Int = 300,
    val paymentCount: Int = 500,
    val materialLineCount: Int = 50,
    val minimumPaymentRatio: Double = 0.7
)
```

## Idempotency

Every seeder checks `count() > 0` before inserting. Safe to call multiple times — will only seed if the table is empty.

## Integration Point

Called in `Database.kt` after existing seed functions:

```kotlin
Seed.seedDentiReferenceDataIfEmpty()
Seed.seedDemoClinicDataIfNeeded()
DemoDataSeeder.seedIfEmpty()  // NEW
```

## Build Verification

- `./gradlew compileKotlin` — passed
- `./gradlew build` — passed (BUILD SUCCESSFUL)

## Related Documentation

- [Database Tables](../db/tables.md) — full schema reference
- [Seeder Architecture Decision](../decisions/002-seeder-architecture.md)
