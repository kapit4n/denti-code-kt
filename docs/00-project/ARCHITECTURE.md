# Architecture

See docs/architecture.md for the full reference.

## High-Level Layers

- **Data Layer** (`data/`) — SQLite via Exposed ORM, `DentiRepository` central CRUD, service classes for business logic
- **UI Layer** (`ui/`) — Compose Desktop screens, reusable components, theming, navigation
- **No ViewModel layer** — state managed via `mutableStateOf` + `LaunchedEffect`

## Data Flow

User Interaction → Composable → LaunchedEffect → withContext(IO) → DentiRepository → Exposed SQL

## Key Architectural Decisions

- Single `DentiRepository` class for all CRUD
- No DI framework (manual injection)
- No external state library
- Pop-up windows for patient/doctor detail
