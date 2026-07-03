# Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Kotlin (JVM) | 2.0.21 |
| UI Framework | JetBrains Compose Desktop (Multiplatform) | 1.7.3 |
| UI Paradigm | Material Design 3 | bundled |
| ORM | Exposed (JetBrains) | 0.55.0 |
| Database | SQLite (via xerial JDBC) | 3.47.1.0 |
| Async | Kotlin Coroutines (core + swing) | 1.9.0 |
| Build System | Gradle (Kotlin DSL) | 8.10.2 |
| JDK | Java (jvmToolchain) | 17+ |
| Packaging | Compose Desktop plugin (native dists) | — |

## Dependencies (`build.gradle.kts`)

```kotlin
// Kotlin & Compose
kotlin("jvm") version "2.0.21"
org.jetbrains.compose version "1.7.3"
org.jetbrains.kotlin.plugin.compose version "2.0.21"

// Compose UI
compose.desktop.currentOs
compose.material3
compose.runtime
compose.foundation
compose.ui
compose.materialIconsExtended

// Database
org.jetbrains.exposed:exposed-core:0.55.0
org.jetbrains.exposed:exposed-jdbc:0.55.0
org.xerial:sqlite-jdbc:3.47.1.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0
org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0
```

## Key Technology Decisions

### Why Compose Desktop?
- Shared Kotlin codebase with a declarative, reactive UI model
- Material3 theming out of the box
- Native desktop packaging (DMG, MSI, DEB) via the Compose plugin
- No web server or browser dependency — runs as a standalone native window

### Why SQLite + Exposed?
- Embedded database — no server setup, no network dependency
- Single-file storage at `~/.denti-code-kt/denti-clinic.db`
- Exposed provides type-safe SQL DSL with Kotlin idioms
- Schema migration via `SchemaUtils.createMissingTablesAndColumns()` (no Flyway/Liquibase)

### Why Manual DI?
- Application scope is small enough that a DI framework adds complexity without benefit
- Single repository instance created at startup and passed explicitly

### Why mutableStateOf + LaunchedEffect?
- Compose Desktop does not bundle ViewModel or lifecycle libraries
- `mutableStateOf` is the primitive Compose reactive state mechanism
- `LaunchedEffect` scope is tied to the composable lifecycle, automatically cancelled on disposal

## Project Layout

```
src/main/kotlin/com/denticode/kt/
├── Main.kt                    # Entry point, window creation
├── data/                      # Database, repository, services
│   ├── Database.kt            # SQLite connection & migration
│   ├── Tables.kt              # 15 Exposed table definitions
│   ├── Models.kt              # Domain data classes & enums
│   ├── DentiRepository.kt     # Central CRUD repository
│   ├── Seed.kt                # Demo data seeding
│   ├── AppointmentActionsService.kt
│   ├── AppointmentDetailsService.kt
│   ├── AppointmentWorkflowService.kt
│   ├── AppointmentAuditService.kt
│   └── AppointmentReminderService.kt
├── ui/                        # Compose Desktop screens & components
│   ├── theme/                 # Material3 theming (colors, typography, shapes)
│   ├── navigation/            # Sidebar, top bar, command palette
│   ├── layout/                # App scaffold, responsive layout, chrome
│   ├── components/            # Reusable UI components (buttons, cards, inputs, table, etc.)
│   ├── dashboard/             # Dashboard screen with KPIs & charts
│   ├── appointments/          # Appointment list & detail
│   ├── patients/              # Patient directory
│   ├── patientdetail/         # Patient detail pop-up window
│   ├── doctors/               # Doctor directory
│   ├── doctordetail/          # Doctor detail pop-up window
│   ├── payments/              # Payment registry
│   ├── inventory/             # Inventory stock view
│   ├── treatments/            # Treatment form fields
│   ├── charts/                # Canvas-based chart components
│   ├── Format.kt              # Date/money formatting
│   └── *Screen.kt             # Top-level screen composables
└── domain/                    # (reserved)
```

## Runs On

| Platform | Support |
|----------|---------|
| Linux | DEB native distribution |
| macOS | DMG native distribution |
| Windows | MSI native distribution |
| JDK | 17 or later |
