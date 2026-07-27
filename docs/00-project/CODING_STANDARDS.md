# Coding Standards

## Language & Style

- Kotlin, JVM target
- Follow Kotlin coding conventions
- No `!!` — use safe calls and Elvis operator
- No raw `Thread` — use coroutines with `Dispatchers.IO`

## Architecture

- No ViewModels — `mutableStateOf` + `LaunchedEffect` for state
- Single `DentiRepository` for all database operations
- Service classes (`*Service.kt`) for business logic extracted from repository
- No DI framework — manual constructor injection

## Naming

- Files: PascalCase matching the primary class/composable
- Composables: PascalCase, prefixed with `App` for shared components
- Repository methods: verb + noun (`registerPatient`, `updateDoctor`)
- State variables: descriptive nouns (`showDialog`, `refreshNonce`)

## Imports

- No wildcard imports
- Group: Kotlin stdlib → Compose → Exposed → app-internal

## Composables

- State declarations at the top of the composable
- Side effects in `LaunchedEffect` or `rememberCoroutineScope`
- No business logic in composables — delegate to repository/services
- `key` parameters for stable recomposition
