# Milestone 6 — Changelog

All notable changes to the Administration milestone.

## TASK-001 — Users management

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- `UserRole` enum (ADMIN/RECEPTIONIST/USER) + `AppUser`, `UserDirectoryKpis`, `UserRegistrationRequest`, `UserUpdateRequest`
- Repository: `listUsers` (KPIs + role via `user_roles`, priority ADMIN > RECEPTIONIST > USER), `registerUser`, `updateUser` (role row upsert, optional password change), `setUserActive`, `deleteUser` (cascade), `findUserByEmail(excludeId)`
- Validation: email + display name required, password ≥ 6, email unique (case-insensitive); SHA-256 `hashPassword` (provisional, bcrypt deferred to backlog)

### UI (`ui/users/`, `AppShell.kt`)
- `UsersScreen` + `UsersContent` (header, KPI row usuarios/activos/administradores, search + role/status/sort filters, table with edit/toggle/delete)
- `UsersDialogs` (register/edit with role dropdown + password, toggle-active, delete) and `UsersUiModels` (sorting + filtering state)
- `ScreenRoute.Users` now renders `UsersScreen` instead of the placeholder

### Verification
- Runtime probe (temp `user.home`, fresh DB): 17 checks (seed, register, duplicate email + short password rejected, edit role/active, delete) — **ALL_USERS1_CHECKS_OK**
- Smoke-run booted into Users: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)
