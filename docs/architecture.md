# Architecture

## Overview

Denti-Code is a Kotlin/Compose Desktop application for dental clinic management. It follows a **Clean Architecture**-inspired layering adapted for a desktop GUI, with a single-window shell and pop-up secondary windows.

## Layer Diagram

```
┌──────────────────────────────────────────────────────────┐
│                    UI / Presentation                      │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │  Screens    │  │  Components  │  │  Layout/Theme   │  │
│  │  (ScreenX)  │  │  (buttons,   │  │  (AppShell,     │  │
│  │             │  │   tables,    │  │   AppScaffold,  │  │
│  │             │  │   dialogs)   │  │   Navigation)   │  │
│  └──────┬──────┘  └──────────────┘  └─────────────────┘  │
│         │                                                  │
│  ┌──────▼────────────────────────────────────────────┐    │
│  │          UI State Models (UiModels.kt)             │    │
│  │     mutableStateOf + LaunchedEffect + Coroutines   │    │
│  └────────────────────────────────────────────────────┘    │
├──────────────────────────────────────────────────────────┤
│                    Services (Business Logic)               │
│  ┌────────────────────────────────────────────────────┐  │
│  │  AppointmentActionsService     WorkflowService     │  │
│  │  AppointmentDetailsService     AuditService        │  │
│  │  AppointmentReminderService                       │  │
│  └──────────────────────┬─────────────────────────────┘  │
├─────────────────────────┼────────────────────────────────┤
│                    Data Layer                             │
│  ┌──────────────────────▼─────────────────────────────┐  │
│  │  DentiRepository (Central CRUD + Queries)          │  │
│  ├────────────────────────────────────────────────────┤  │
│  │  Exposed ORM (Tables.kt → SQL)                     │  │
│  ├────────────────────────────────────────────────────┤  │
│  │  SQLite (file: ~/.denti-code-kt/denti-clinic.db)   │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

## Layers

### Data Layer (`data/`)
- **Database**: SQLite via JDBC, connected and migrated in `Database.kt` (Kotlin `object` singleton)
- **ORM**: Exposed (JetBrains) with 15 table definitions in `Tables.kt`
- **Repository**: `DentiRepository` — single class with all CRUD operations, aggregations, and queries, each wrapped in Exposed `transaction { }` blocks
- **Services** (business logic extracted from the repository):
  - `AppointmentActionsService` — edit, reschedule, cancel, register payment/treatment
  - `AppointmentWorkflowService` — state machine for confirm → start → complete
  - `AppointmentDetailsService` — detail loading, notes, calendar counts
  - `AppointmentAuditService` — append audit log entries
  - `AppointmentReminderService` — WhatsApp message generation
- **Seed**: `Seed.kt` populates demo reference data and clinic data on first launch

### Domain Layer (`domain/`)
- Reserved for future domain models; currently empty. Domain logic lives in the service classes.

### UI / Presentation Layer (`ui/`)
- **Entry point**: `Main.kt` → `AppShell()` in a 1280×800 `Window`
- **Shell**: `AppShell` → `AppScaffold` (sidebar + top bar + animated content area)
- **Navigation**: `NavigationState` (mutable state holder) with sealed `ScreenRoute` class
- **Screens**: Dashboard, Appointments, Patients, Doctors, Procedures, Inventory, Payments (7 complete) + Reports/Users/Settings (placeholders)
- **Pop-up windows**: Patient detail and Doctor detail open as secondary `Window()` composables
- **UI components**: Custom button, card, input, table, dialog, and feedback component library
- **Theming**: Material3 with custom `AppColors` (light/dark), `AppTypography`, `AppSpacing`, `AppShapes`, `AppElevations`
- **State management**: Per-screen `mutableStateOf` + `LaunchedEffect` with `Dispatchers.IO`; no external state library
- **Global state**: `CompositionLocal` for snackbar host, messenger, window command handler

## Navigation

```
AppShell (root Window)
  └── AppScaffold
      ├── AppSidebar (collapsible, sectioned)
      ├── AppTopBar (breadcrumb, search, dark mode toggle, actions)
      └── AnimatedContent (fade transition by ScreenRoute)
           ├── Dashboard
           ├── Appointments
           ├── Patients
           ├── Doctors
           ├── Procedures
           ├── Inventory
           ├── Payments
           ├── Reports (placeholder)
           ├── Users (placeholder)
           └── Settings (placeholder)

Pop-up Windows:
  ├── PatientDetailWindow (from Patients, Appointments, Payments)
  └── DoctorDetailWindow (from Doctors)
```

All routes are defined in `ScreenRoute` sealed class in `NavigationModels.kt`. Keyboard navigation via Ctrl+K command palette.

## Data Flow

```
User Interaction
     │
     ▼
Composable (Screen)
     │  LaunchedEffect / rememberCoroutineScope
     │  withContext(Dispatchers.IO) { repo.method() }
     ▼
DentiRepository
     │  transaction { ... }
     ▼
Exposed Table DSL → SQL → SQLite
     │
     ▼
Domain model (data class) returned
     │
     ▼
UI State updated via mutableStateOf
     │
     ▼
Compose recomposition → new UI
```

All DB calls are suspended via `withContext(Dispatchers.IO)`. The main thread receives results and updates `mutableStateOf` fields, triggering recomposition.

## Database Schema (15 tables)

```
users ──< user_roles
users ──< doctors        (nullable FK)
users ──< patients       (nullable FK)
patients ──< appointments
doctors  ──< appointments
doctors  ──< performed_actions
patients ──< performed_actions
patients ──< payments
procedure_types ──< appointments (nullable)
procedure_types ──< performed_actions
procedure_types ──< payments (nullable)
appointments ──< appointment_notes
appointments ──< appointment_audit_log
appointments ── performed_actions
appointments ──< payments (nullable)
appointments ─> appointments (self-ref follow-up)
performed_actions ──< payments (nullable)
consultories ──< material_inventory_lines
consultories ──< inventory_movements
treatment_facilities ──< material_inventory_lines
treatment_facilities ──< inventory_movements
```

## State Management

- **No external state library** (no LiveData, Flow, RxJava, or state management framework)
- Per-screen state via `var x by remember { mutableStateOf(default) }`
- UI models (`*UiModels.kt`) separate display concerns from domain models
- Global cross-cutting concerns via `CompositionLocal`:
  - `LocalSnackbarHostState` — snackbar notifications
  - `LocalAppMessenger` — success/error messages
  - `LocalWindowCommandHandler` — inter-window commands

## Dependency Injection

- **No DI framework** (no Hilt, Koin, Dagger)
- Manual constructor injection: `DentiRepository` created in `Main.kt`, passed to `AppShell`, forwarded to screens
- Service classes created inside screens via `remember(repo) { ServiceClass(repo) }`
- Database connection is a Kotlin `object` singleton
