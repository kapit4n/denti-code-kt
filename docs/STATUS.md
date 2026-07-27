# Denti-Code KT — System Status Report

**Date:** 2026-07-24
**Version:** v0.6.0
**Stack:** Kotlin Compose Desktop, Material 3, JetBrains Exposed ORM, SQLite

---

## Executive Summary

Denti-Code KT is a dental clinic management desktop application with 10 defined navigation routes. **7 of 10 screens are fully implemented** with real database operations. **3 screens are entirely placeholder stubs** (Reports, Users, Settings). The Inventory screen is partially functional (read-only display, no CRUD). The data layer has 37 public repository methods — all fully implemented with real SQL — but several entities lack Update/Delete operations. There is no export functionality, no authentication, no printing, and no file I/O anywhere in the system.

---

## 1. Screen Implementation Status

| # | Route | Screen | Status | Notes |
|---|-------|--------|--------|-------|
| 1 | `dashboard` | DashboardScreen | **DONE** | Live KPIs, charts, alerts, activity feed — all from real DB data |
| 2 | `appointments` | AppointmentsScreen | **DONE** | Full CRUD + workflow (confirm/start/complete/cancel/reschedule), notes, reminders, keyboard shortcuts |
| 3 | `patients` | PatientsScreen | **PARTIAL** | Create + Read. Edit patient is "próximamente". Export is simulated. |
| 4 | `doctors` | DoctorsScreen | **DONE** | Full CRUD: register, edit, toggle active/vacation |
| 5 | `procedures` | ProceduresScreen | **PARTIAL** | Create + Read. No update/delete procedure types. |
| 6 | `inventory` | InventoryStockScreen | **PARTIAL** | Read-only display. All actions (new, edit, adjust, transfer, export) are stubs. |
| 7 | `payments` | PaymentsScreen | **PARTIAL** | Create + Read. No edit/delete/export. |
| 8 | `reports` | PlaceholderScreen | **STUB** | "Esta seccion esta en preparacion. Proximamente: Reportes." |
| 9 | `users` | PlaceholderScreen | **STUB** | "Esta seccion esta en preparacion. Proximamente: Usuarios." |
| 10 | `settings` | PlaceholderScreen | **STUB** | "Esta seccion esta en preparacion. Proximamente: Configuracion." |

### Detail Windows (separate OS windows, not routes)

| # | Window | Status | Notes |
|---|--------|--------|-------|
| 11 | PatientDetailWindow | **PARTIAL** | New visit/payment/treatment dialogs work. Edit patient, clinical history, payment history, export are stubs. |
| 12 | DoctorDetailWindow | **DONE** | Profile display, edit dialog, toggle active — all wired. |

---

## 2. CRUD Completeness by Entity

| Entity | Create | Read | Update | Delete | Notes |
|--------|--------|------|--------|--------|-------|
| **Users** | NONE | NONE | NONE | NONE | Table exists + seeded, zero runtime methods |
| **User Roles** | NONE | NONE | NONE | NONE | Table exists + seeded, zero runtime methods |
| **Doctors** | DONE | DONE | DONE | Soft only | `setDoctorActive(false)` — no hard delete |
| **Patients** | DONE | DONE | **MISSING** | **MISSING** | No `updatePatient()`, no `deletePatient()` |
| **Appointments** | DONE | DONE | DONE | Soft only | Cancel = status change. No hard delete. |
| **Appointment Notes** | DONE | DONE | DONE | DONE | Only entity with full CRUD |
| **Procedure Types** | DONE | DONE | **MISSING** | **MISSING** | No `updateProcedureType()`, no `deleteProcedureType()` |
| **Treatments** | DONE | DONE | Internal only | Internal only | `syncTreatmentForAppointment()` is private |
| **Payments** | DONE | DONE | **MISSING** | **MISSING** | No `updatePayment()`, no `deletePayment()` |
| **Inventory Lines** | **MISSING** | DONE | **MISSING** | **MISSING** | Read-only. No stock add/adjust/create. |
| **Inventory Movements** | **MISSING** | DONE | N/A | N/A | Read-only. No `recordMovement()`. |
| **Consultories** | Seed only | DONE | **MISSING** | **MISSING** | No runtime CRUD beyond seeding |
| **Treatment Facilities** | Seed only | Indirect | **MISSING** | **MISSING** | No repository methods at all |

---

## 3. Functional Action Inventory

### Working (24 operations)

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
| Copy reminder to clipboard | AppointmentsScreen | `reminderService.copyToClipboard()` |
| Open WhatsApp reminder | AppointmentsScreen | `reminderService.openUrl()` |
| Toggle dark theme | CommandPalette | In-memory toggle |
| Command palette navigation | CommandPalette / Ctrl+K | `navigationState.navigateTo()` |

### Placeholder / Stub (13 operations)

| Operation | Location | Message |
|-----------|----------|---------|
| Edit patient | PatientDetailWindow | "Editar paciente (próximamente)." |
| View full patient profile | PatientDetailWindow | "Perfil completo (próximamente)." |
| Clinical history | PatientDetailWindow | "Historial clinico (próximamente)." |
| View payment history | PatientDetailWindow | "Historial de pagos (próximamente)." |
| Send patient reminder | PatientDetailWindow | "Recordatorio preparado (simulacion)." |
| Export patients | PatientsScreen | "Exportacion de pacientes preparada (simulacion)." |
| Export payments | PaymentsScreen | "Exportacion de pagos proximamente." |
| New inventory item | InventoryStockScreen | "Registro de insumos proximamente." |
| Export inventory | InventoryStockScreen | "Exportacion de inventario proximamente." |
| Inventory item actions (edit/adjust/transfer) | InventoryStockScreen | Toast only, no-op lambda |
| Close session / Logout | AppSidebar | "Sesion demo — sin cierre real." |
| View receipt | PaymentsScreen | Just closes dropdown menu |
| Print summary | PatientDetailWindow | Just closes dropdown menu |

---

## 4. Missing Features (Detailed)

### A. Three Entire Screens Not Implemented

1. **Reports Screen** — No analytics, no report generation, no export. Dashboard has basic KPIs but no dedicated reporting with date ranges, filtering, or PDF/CSV output.
2. **Users Screen** — No user management, no role assignments, no authentication/login flow. `UsersTable` and `UserRolesTable` exist in the DB but have zero runtime code.
3. **Settings Screen** — No preferences persistence. Dark theme toggle is in-memory only (lost on restart). No clinic configuration (name, address, phone, logo).

### B. Export / File I/O

- **No export libraries** in `build.gradle.kts` (no Apache POI, iText, OpenPDF, OpenCSV)
- **No file writing** anywhere in the codebase (no `File()`, `BufferedWriter`, `PrintWriter`)
- **No import functionality** for any data type
- **No PDF generation** for receipts, reports, or patient records
- **No CSV/Excel export** for patient lists, payment history, or inventory

### C. Authentication & Security

- **No login screen** — app opens directly to the dashboard
- **No user sessions** — `UsersTable` exists but is unused at runtime
- **No role-based access control** — `UserRolesTable` exists but no permission checks
- **No password hashing** — `password_hash` column exists but no auth code
- **Database is unencrypted** — SQLite file at `~/.denti-code-kt/denti-clinic.db`

### D. Inventory Management

- **No stock creation** — cannot add new inventory items
- **No stock adjustment** — cannot increment/decrement quantities
- **No stock transfer** — cannot move items between consultories
- **No movement recording** — `InventoryMovementsTable` exists but no write methods
- **No min/max quantity storage** — computed from formulas, not stored in DB
- **No low-stock alerts** — count exists but no proactive notification

### E. Patient Management Gaps

- **No patient edit/update** — once registered, patient data cannot be modified
- **No patient deletion** — not even soft-delete
- **No patient export** — simulated only
- **No clinical history view** — placeholder only
- **No full profile view** — placeholder only
- **Fake pending balance** — computed from formula `if (PENDING && id % 2 == 0) 45.0 + (id % 5) * 25.0 else 0.0`, not actual payment data

### F. Procedure Type Management

- **No procedure edit/update** — cannot modify existing procedure types
- **No procedure deletion** — cannot remove catalog entries

### G. Payment Management

- **No payment edit** — once registered, payment cannot be modified
- **No payment deletion** — no way to void or remove payments
- **No receipt generation** — "Ver recibo" just closes the menu
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
| `DentiRepository.kt:307` | Patient pending balance | `if (PENDING && id % 2 == 0) 45.0 + (id % 5) * 25.0 else 0.0` |
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
4. `updateStockQuantity()` + `recordInventoryMovement()` — inventory CRUD
5. `registerInventoryLine()` — add new inventory items

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
| Data layer | 8 | `DentiRepository.kt` (1400 lines), `Models.kt`, `Tables.kt`, `Database.kt`, 5 service classes |
| Seeders | 12 | `DemoDataSeeder` + 11 individual seeders |
| Screens | 10 | 7 implemented + 3 placeholder |
| Screen content | 8 | `AppointmentsPremiumContent.kt` (900 lines), `ModernStockContent.kt`, etc. |
| Dialogs | 4 files | 15 dialog composables |
| Components | ~30 | Buttons, inputs, charts, cards, dialogs, layout |
| Navigation | 6 | Sidebar, top bar, command palette, state, models |
| Theme | 6 | Colors, typography, spacing, shapes, elevations |
| **Total** | **~100** | |
