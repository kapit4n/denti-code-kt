# Denti-Code KT — System Status Report

**Date:** 2026-08-04
**Version:** v0.19.0
**Stack:** Kotlin Compose Desktop, Material 3, JetBrains Exposed ORM, SQLite

---

## Executive Summary

Denti-Code KT is a dental clinic management desktop application with 10 defined navigation routes. **8 of 10 screens are fully implemented** with real database operations. **2 screens are entirely placeholder stubs** (Reports, Settings; Users placeholder remains until the admin milestone). The Inventory route renders the **modern stock screen** with working stock CRUD (register lines, edit total, ± adjustments, transfers between consultories, CSV export) with real movements recorded, a **movement insights dashboard** (30-day entry/exit/movement KPIs, units-by-category chart, top-10 replenishment suggestions, movement-history notes with running balances), **stock alerts on the Dashboard** (a dedicated card listing low/out-of-stock lines with consultory + category + qty/min, computed from real reads), a **suppliers catalog** (register/edit/delete) and **purchase orders** (line items with qty/cost + total, "Sugerir reposición" prefill from real stock statuses; receiving a pending order credits per-consultory stock and records `RESTOCK` movements). **Milestone 4 (Inventory) is COMPLETE** — TASK-005 closed it with a 26-check data-layer audit (supplier-name uniqueness + pending-order delete guard enforced in the repo, reorder-suggestion logic deduplicated, locale-safe money formatting) and a clean build/smoke-run. The data layer has **164 public repository methods** — all fully implemented with real SQL — and the patient detail window is a complete clinical workspace (clinical history, timeline, documents, notes, prescriptions, follow-ups, treatment plans) with a **real pending balance** computed from treatments minus payments and **working exports** (payment receipts, payments CSV, inventory CSV, HTML patient ficha, text patient summary). **Physical document files now work**: a native picker copies files into `~/.denti-code-kt/documents/<patientId>/`, documents open with the OS viewer and are removed from the store when deleted. **Milestone 3 (Patient Workspace) is complete**; **Milestone 4 (Inventory) is complete**. Remaining gaps: payment edit/delete, authentication, printing to paper, and broader file I/O.

---

## 1. Screen Implementation Status

| # | Route | Screen | Status | Notes |
|---|-------|--------|--------|-------|
| 1 | `dashboard` | DashboardScreen | **DONE** | Live KPIs, charts, alerts, activity feed + stock-alerts card (low/out-of-stock lines) — all from real DB data |
| 2 | `appointments` | AppointmentsScreen | **DONE** | Full CRUD + workflow (confirm/start/complete/cancel/reschedule), notes, reminders, keyboard shortcuts |
| 3 | `patients` | PatientsScreen | **DONE** | Full CRUD: register, edit (detail), archive/restore, hard delete. Export is simulated. |
| 4 | `doctors` | DoctorsScreen | **DONE** | Full CRUD: register, edit, toggle active/vacation |
| 5 | `procedures` | ProceduresScreen | **DONE** | Full CRUD: register, edit, archive/restore/delete, categories, favorites |
| 6 | `inventory` | InventoryStockScreen | **DONE** | Modern stock screen: stock CRUD (register new lines duplicate-guarded, edit total, ± adjustments with reason, transfers between consultories, movement history side panel with type/note/running balance, filters/pagination, CSV export), movement-insights dashboard (30-day entries/exits/movements KPIs, units-by-category bar chart, top-10 replenishment suggestions), **Proveedores** dialog (register/edit/delete suppliers) and **Pedidos** dialog (purchase orders with line items + total, "Sugerir reposición" prefill, "Recibir" credits per-consultory stock with `RESTOCK` movements). |
| 7 | `payments` | PaymentsScreen | **PARTIAL** | Create + Read. Receipt view/save + CSV export work. No edit/delete/void. |
| 8 | `reports` | PlaceholderScreen | **STUB** | "Esta seccion esta en preparacion. Proximamente: Reportes." |
| 9 | `users` | PlaceholderScreen | **STUB** | "Esta seccion esta en preparacion. Proximamente: Usuarios." |
| 10 | `settings` | PlaceholderScreen | **STUB** | "Esta seccion esta en preparacion. Proximamente: Configuracion." |

### Detail Windows (separate OS windows, not routes)

| # | Window | Status | Notes |
|---|--------|--------|-------|
| 11 | PatientDetailWindow | **DONE** | Full clinical workspace: tabs (resumen, historial clínico, cronología, citas, pagos, documentos, notas, recetas, seguimientos, plan de tratamiento). New visit/payment/treatment dialogs, medical/dental history CRUD, notes/prescriptions/follow-ups/documents management, treatment plans with phases and estimated cost. Real pending balance (treatments − payments). Receipt view/save, HTML ficha export and text resumen export work. Physical document files work: native picker copies to `~/.denti-code-kt/documents/<patientId>/`, open via OS viewer, physical delete. Refactored: window only orchestrates (state holder in `PatientDetailWindowActions.kt`, submit/export in `PatientDetailSubmitActions.kt`, all dialogs in `PatientDetailDialogsHost.kt`). |
| 12 | DoctorDetailWindow | **DONE** | Profile display, edit dialog, toggle active — all wired. |

---

## 2. CRUD Completeness by Entity

| Entity | Create | Read | Update | Delete | Notes |
|--------|--------|------|--------|--------|-------|
| **Users** | NONE | NONE | NONE | NONE | Table exists + seeded, zero runtime methods |
| **User Roles** | NONE | NONE | NONE | NONE | Table exists + seeded, zero runtime methods |
| **Doctors** | DONE | DONE | DONE | Soft only | `setDoctorActive(false)` — no hard delete |
| **Patients** | DONE | DONE | DONE | DONE | Edit/archive/restore/hard delete via detail window |
| **Appointments** | DONE | DONE | DONE | Soft only | Cancel = status change. No hard delete. |
| **Appointment Notes** | DONE | DONE | DONE | DONE | Only entity with full CRUD |
| **Procedure Types** | DONE | DONE | DONE | DONE | Archive/restore/hard delete with referential checks |
| **Treatments** | DONE | DONE | DONE | DONE | `updateTreatmentStatus/Notes`, `deleteTreatment` |
| **Medical History** | DONE | DONE | DONE | DONE | Patient detail → Historial clínico |
| **Dental History** | DONE | DONE | DONE | DONE | Patient detail → Historial clínico |
| **Patient Notes** | DONE | DONE | Pin toggle | DONE | Patient detail → Notas |
| **Prescriptions** | DONE | DONE | Status only | DONE | Patient detail → Recetas |
| **Follow-ups** | DONE | DONE | Status only | DONE | Patient detail → Seguimientos |
| **Patient Documents** | DONE | DONE | N/A | DONE | Metadata + physical files (copy to `~/.denti-code-kt/documents/<patientId>/`, OS open, physical delete) |
| **Treatment Plans** | DONE | DONE | DONE | DONE | Patient detail → Plan de tratamiento; phases, estimated cost, status workflow |
| **Payments** | DONE | DONE | **MISSING** | **MISSING** | No `updatePayment()`, no `deletePayment()` |
| **Inventory Lines** | DONE | DONE | DONE | **MISSING** | Stock CRUD: `registerInventoryLine()`, `adjustInventoryStock()`; delete by design (qty → 0 = Agotado) |
| **Inventory Movements** | DONE | DONE | N/A | N/A | `registerInventoryLine`/`adjustInventoryStock`/`transferInventoryStock`/`receivePurchaseOrder` record `RESTOCK`/`ADJUSTMENT`/`TRANSFER` |
| **Suppliers** | DONE | DONE | DONE | DONE | `registerSupplier`/`listSuppliers`/`findSupplierByName`/`updateSupplier`/`restoreSupplier`/`hardDeleteSupplier`; name uniqueness + delete blocked by products or pending orders enforced in repo |
| **Purchase Orders** | DONE | DONE | Status only | DONE | `registerPurchaseOrder`/`listPurchaseOrders`/`getPurchaseOrderItems`; `receivePurchaseOrder` (PENDING→RECEIVED, credits stock) ; `deletePurchaseOrder` (PENDING only) |
| **Consultories** | Seed only | DONE | **MISSING** | **MISSING** | No runtime CRUD beyond seeding |
| **Treatment Facilities** | Seed only | Indirect | **MISSING** | **MISSING** | No repository methods at all |

---

## 3. Functional Action Inventory

### Working (41 operations)

| Operation | Where | Backend |
|-----------|-------|---------|
| Register patient | PatientsScreen | `repo.registerPatient()` |
| Register doctor | DoctorsScreen | `repo.registerDoctor()` |
| Edit doctor | DoctorsScreen / DoctorDetailWindow | `repo.updateDoctor()` |
| Toggle doctor active | DoctorsScreen / DoctorDetailWindow | `repo.setDoctorActive()` |
| Register procedure type | ProceduresScreen | `repo.registerProcedureType()` |
| Create appointment | AppointmentsScreen / PatientDetailWindow | `repo.createAppointment()` |
| Edit appointment | AppointmentsScreen | `actionsService.updateAppointment()` |
| Confirm appointment | AppointmentsScreen | `workflowService.confirmAppointment()` |
| Start appointment | AppointmentsScreen | `workflowService.startAppointment()` |
| Complete appointment | AppointmentsScreen | `workflowService.completeAppointment()` |
| Reschedule appointment | AppointmentsScreen | `actionsService.rescheduleAppointment()` |
| Cancel appointment | AppointmentsScreen | `actionsService.cancelAppointment()` |
| Add appointment note | AppointmentsScreen | `detailsService.addNote()` |
| Edit appointment note | AppointmentsScreen | `detailsService.updateNote()` |
| Delete appointment note | AppointmentsScreen | `detailsService.deleteNote()` |
| Register payment (from Payments screen) | PaymentsScreen | `repo.registerPaymentForPatient()` |
| Register payment (from appointment) | AppointmentsScreen | `actionsService.registerPaymentForAppointment()` |
| Register payment (from patient detail) | PatientDetailWindow | `repo.registerPaymentForPatient()` |
| Register treatment (from patient detail) | PatientDetailWindow | `repo.registerTreatmentForPatient()` |
| Register treatment (from appointment) | AppointmentsScreen | `actionsService.registerTreatmentForAppointment()` |
| Create treatment plan | PatientDetailWindow | `repo.registerTreatmentPlan()` |
| Edit treatment plan | PatientDetailWindow | `repo.updateTreatmentPlan()` |
| Change treatment plan status | PatientDetailWindow | `repo.updateTreatmentPlanStatus()` |
| Delete treatment plan | PatientDetailWindow | `repo.deleteTreatmentPlan()` |
| Add plan phase | PatientDetailWindow | `repo.addTreatmentPlanPhase()` |
| Edit plan phase | PatientDetailWindow | `repo.updateTreatmentPlanPhase()` |
| Change phase status | PatientDetailWindow | `repo.updateTreatmentPlanPhaseStatus()` |
| Delete plan phase | PatientDetailWindow | `repo.deleteTreatmentPlanPhase()` |
| Copy reminder to clipboard | AppointmentsScreen | `reminderService.copyToClipboard()` |
| Open WhatsApp reminder | AppointmentsScreen | `reminderService.openUrl()` |
| Toggle dark theme | CommandPalette | In-memory toggle |
| Command palette navigation | CommandPalette / Ctrl+K | `navigationState.navigateTo()` |
| View payment receipt | PaymentsScreen / PatientDetailWindow | `ExportService` + `PaymentReceiptDialog` (save .txt) |
| Export payments (CSV) | PaymentsScreen | `ExportService` + `renderPaymentsCsv` |
| Export patient ficha (HTML) | PatientDetailWindow | `ExportService` + `renderPatientSummaryHtml` |
| Print/save patient summary (TXT) | PatientDetailWindow | `ExportService` + `renderPatientSummaryText` |
| Upload patient document (file picker + copy to store) | PatientDetailWindow | `ExportService.pickOpenFile` + `DocumentStore.save` |
| Open patient document file | PatientDetailWindow | `ExportService.openFile` (OS default viewer) |
| Delete patient document (metadata + physical file) | PatientDetailWindow | `repo.deletePatientDocument` + `DocumentStore.delete` |
| Register inventory line | InventoryStockScreen | `repo.registerInventoryLine()` (duplicate-guarded) |
| Edit inventory total | InventoryStockScreen | `repo.adjustInventoryStock()` (delta = target − current) |
| Adjust inventory stock | InventoryStockScreen | `repo.adjustInventoryStock()` (± delta + reason) |
| Transfer stock between consultories | InventoryStockScreen | `repo.transferInventoryStock()` |
| Export inventory (CSV) | InventoryStockScreen | `ExportService` + `renderInventoryCsv` |
| Register supplier | InventoryStockScreen (Proveedores) | `repo.registerSupplier()` |
| Edit supplier | InventoryStockScreen (Proveedores) | `repo.updateSupplier()` |
| Delete supplier | InventoryStockScreen (Proveedores) | `repo.hardDeleteSupplier()` |
| Register purchase order | InventoryStockScreen (Pedidos) | `repo.registerPurchaseOrder()` |
| Receive purchase order (credits stock) | InventoryStockScreen (Pedidos) | `repo.receivePurchaseOrder()` |
| Delete purchase order (pending) | InventoryStockScreen (Pedidos) | `repo.deletePurchaseOrder()` |

### Placeholder / Stub (8 operations)

| Operation | Location | Message |
|-----------|----------|---------|
| Edit patient | PatientDetailWindow | "Editar paciente (próximamente)." |
| View full patient profile | PatientDetailWindow | "Perfil completo (próximamente)." |
| Send patient reminder | PatientDetailWindow | "Recordatorio preparado (simulacion)." |
| Export patients | PatientsScreen | "Exportacion de pacientes preparada (simulacion)." |
| Close session / Logout | AppSidebar | "Sesion demo — sin cierre real." |
| Reports screen | PlaceholderScreen | "Esta seccion esta en preparacion." |
| Users screen | PlaceholderScreen | "Esta seccion esta en preparacion." |
| Settings screen | PlaceholderScreen | "Esta seccion esta en preparacion." |

---

## 4. Missing Features (Detailed)

### A. Three Entire Screens Not Implemented

1. **Reports Screen** — No analytics, no report generation, no export. Dashboard has basic KPIs but no dedicated reporting with date ranges, filtering, or PDF/CSV output.
2. **Users Screen** — No user management, no role assignments, no authentication/login flow. `UsersTable` and `UserRolesTable` exist in the DB but have zero runtime code.
3. **Settings Screen** — No preferences persistence. Dark theme toggle is in-memory only (lost on restart). No clinic configuration (name, address, phone, logo).

### B. Export / File I/O

- **No export libraries** in `build.gradle.kts` (no Apache POI, iText, OpenPDF, OpenCSV)
- **File writing added** via `export/ExportService.kt` (UTF-8 text files through a native save dialog)
- **Working exports:** payment receipts (`.txt`), payments list (`.csv`), inventory stock (`.csv`), patient ficha (`.html`), patient summary (`.txt`)
- **No import functionality** for any data type
- **No PDF generation** for receipts, reports, or patient records
- **No Excel (.xlsx) export** — CSV is the spreadsheet bridge
- **Document file store** — `DocumentStore` copies uploads to `~/.denti-code-kt/documents/<patientId>/`; there is no cloud/network storage (fully offline by design)

### C. Authentication & Security

- **No login screen** — app opens directly to the dashboard
- **No user sessions** — `UsersTable` exists but is unused at runtime
- **No role-based access control** — `UserRolesTable` exists but no permission checks
- **No password hashing** — `password_hash` column exists but no auth code
- **Database is unencrypted** — SQLite file at `~/.denti-code-kt/denti-clinic.db`

### D. Inventory Management

- **Stock CRUD works** — register (`registerInventoryLine`), edit/adjust (`adjustInventoryStock`), transfer (`transferInventoryStock`), CSV export
- **Movements recorded** — `RESTOCK`/`ADJUSTMENT`/`TRANSFER` rows in `InventoryMovementsTable`; receiving a purchase order also records `RESTOCK` movements
- **Suppliers work** — register/edit/delete via the Proveedores dialog; duplicate names rejected in the repo (case-insensitive) and delete is blocked while the supplier has products or pending orders
- **Purchase orders work** — create with line items + "Sugerir reposición" prefill; receiving credits per-consultory stock; pending orders deletable, received orders not
- **Audit (TASK-005)** — 26-check data-layer probe passed; reorder-suggestion logic deduplicated; money formatting locale-safe
- **No min/max quantity storage** — computed from formulas, not stored in DB
- **No line deletion** — by design, quantity 0 = "Agotado"

### E. Patient Management Gaps

- **No patient export** — simulated only
- **No payment history export** — stub in patient detail

### F. Procedure Type Management

- **No procedure edit/update** — cannot modify existing procedure types
- **No procedure deletion** — cannot remove catalog entries

### G. Payment Management

- **No payment edit** — once registered, payment cannot be modified
- **No payment deletion** — no way to void or remove payments
- **No paper receipt printing** — receipts are viewable and savable as text files; physical printer output is not wired
- **No payment reports** — no way to summarize payments by period/method/patient

### H. Notification / Reminder System

- **Manual only** — WhatsApp reminders require clicking a button
- **No automatic reminders** — no background timers, no scheduled checks
- **No OS notifications** — no tray icon, no system notification API
- **No SMS or email** — only WhatsApp via browser
- **Notification bell icon** exists in top bar but is a no-op

### I. Printing

- **No print functionality** — no `javax.print`, no `java.awt.print` usage
- **No receipt printing**
- **No appointment card printing**
- **No report printing**

### J. Data Integrity

- **No backup/restore** — no mechanism to backup the SQLite database
- **No data validation beyond basics** — no email format check, no phone format check
- **No optimistic locking** — concurrent edits could overwrite each other
- **Schema migration** uses `SchemaUtils.createMissingTablesAndColumns()` — no versioned migrations

---

## 5. Fake / Computed Data (Not From Database)

| Location | What | Formula |
|----------|------|---------|
| `DentiRepository.kt:558` | Doctor experience years | `(id * 7 + firstName.length * 3 + lastName.length) % 13 + 4` |
| `DentiRepository.kt:645` | Inventory last updated timestamp | `System.currentTimeMillis() - (lineId * 86_400_000 % 2_592_000_000)` |
| `DentiRepository.kt:714` | Inventory min quantity | Formula based on category key + facilityId |

---

## 6. Architecture Notes

- **No dependency injection** — `DentiRepository` is passed manually through composable parameters
- **No ViewModel pattern** — state is managed via `remember { mutableStateOf() }` blocks inside composables
- **No Jetpack Navigation** — custom `NavigationState` with `when` dispatch in `AppShell.kt`
- **No back-stack** — single route state, no nested navigation
- **No deep linking** — desktop app, not applicable
- **100% offline** — all data in local SQLite, no networking
- **Single-window primary** — patient and doctor detail open as separate OS windows
- **No unit tests** — `test` source set has no files (`compileTestKotlin NO-SOURCE`)

---

## 7. Prioritized Roadmap (Suggested)

### Phase 1 — Core CRUD Gaps (High Priority)
1. `updatePatient()` + edit patient dialog
2. `updateProcedureType()` + edit procedure dialog
3. `updatePayment()` + void payment capability
4. ~~`updateStockQuantity()` + `recordInventoryMovement()`~~ — inventory CRUD (DONE, M4 TASK-001)
5. ~~`registerInventoryLine()`~~ — add new inventory items (DONE, M4 TASK-001)

### Phase 2 — Missing Screens (High Priority)
6. Reports screen — appointment revenue, patient demographics, treatment analytics
7. Users screen — user CRUD, role management
8. Settings screen — clinic info, theme persistence, defaults

### Phase 3 — Export & Documents (Medium Priority)
9. PDF receipt generation
10. CSV/Excel export for patients, payments, inventory
11. Patient record PDF export
12. Appointment card/summary printing

### Phase 4 — Authentication & Security (Medium Priority)
13. Login screen with password authentication
14. Role-based access control (admin, doctor, secretary)
15. Session management
16. Database encryption

### Phase 5 — Notifications & Automation (Low Priority)
17. Scheduled WhatsApp reminders (background timer)
18. OS tray notifications for upcoming appointments
19. Low-stock alerts
20. Appointment reminders via email/SMS

### Phase 6 — Quality & Infrastructure (Low Priority)
21. Unit and integration tests
22. Database backup/restore
23. Versioned schema migrations
24. State management refactor (ViewModel pattern)
25. Dependency injection

---

## 8. File Inventory Summary

| Category | Files | Key Files |
|----------|-------|-----------|
| Entry point | 1 | `Main.kt` |
| Data layer | 8 | `DentiRepository.kt` (3000+ lines), `Models.kt`, `Tables.kt`, `Database.kt`, 5 service classes |
| Seeders | 18 | `DemoDataSeeder` + 17 individual seeders |
| Screens | 10 | 7 implemented + 3 placeholder |
| Screen content | 8 | `AppointmentsPremiumContent.kt` (900 lines), `ModernStockContent.kt`, etc. |
| Dialogs | 11 files | 24 dialog composables (incl. `PatientDetailDialogs.kt`, `PatientDetailDialogsHost.kt`, `Clinical*Dialogs.kt`) |
| Components | ~30 | Buttons, inputs, charts, cards, dialogs, layout |
| Navigation | 6 | Sidebar, top bar, command palette, state, models |
| Theme | 6 | Colors, typography, spacing, shapes, elevations |
| **Total** | **~100** | |
