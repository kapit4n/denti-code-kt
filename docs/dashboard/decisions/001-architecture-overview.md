# Decision 001: Dashboard Architecture Overview

## Context

The dashboard was implemented as a multi-file module under `ui/dashboard/` with a thin wrapper in `ui/DashboardScreen.kt`. The initial implementation includes 7 widget composables, 3 chart composables, and responsive grid layout. Several pieces use hardcoded mock data.

## Alternatives Considered

### 1. Single-file dashboard
- **Pros**: Simple, all code in one place
- **Cons**: Would exceed 1000+ lines, hard to maintain, violates single-responsibility

### 2. ViewModel-based architecture
- **Pros**: Better testability, lifecycle awareness, separation of concerns
- **Cons**: Would require introducing ViewModel dependency, breaking from current project pattern (all screens use `mutableStateOf` + `LaunchedEffect`)

### 3. Current approach: Composable-only with mutableStateOf
- **Pros**: Consistent with all other screens in the project, no new dependencies, simple data flow
- **Cons**: No formal lifecycle management, harder to test

## Final Decision

**Keep the current composable-only architecture** to maintain consistency with the rest of the codebase. The project uses `mutableStateOf` + `LaunchedEffect` + `Dispatchers.IO` across all 7 implemented screens. Introducing ViewModels for the dashboard alone would create inconsistency.

### File Organization

The dashboard module uses this file structure:

| File | Responsibility |
|---|---|
| `DashboardScreen.kt` | Thin routing wrapper (16 lines) |
| `ModernDashboardContent.kt` | Data loading + orchestration (138 lines) |
| `DashboardComponents.kt` | All widget composables (722 lines) |
| `DashboardCharts.kt` | Canvas-based charts (207 lines) |
| `DashboardUiState.kt` | UI data models (74 lines) |

### Pending Refactor

`DashboardComponents.kt` at 722 lines should be split into smaller files for maintainability. This is tracked in the ROADMAP.

## Consequences

- **Positive**: Consistent with project architecture, no new dependencies
- **Positive**: Simple mental model for developers familiar with other screens
- **Negative**: `DashboardComponents.kt` is large and should be split
- **Negative**: Mock data is inline rather than behind an abstraction layer
- **Neutral**: Testing will rely on screenshot/UI testing rather than ViewModel unit tests
