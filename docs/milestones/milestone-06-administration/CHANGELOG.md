# Milestone 6 — Changelog

All notable changes to the Administration milestone.

## TASK-002 — Roles & permissions

### Data layer (`data/Models.kt`)
- `Permission` enum — 16 permissions with Spanish `labelEs` + `moduleEs` grouping (10 functional modules: dashboard, citas, pacientes, doctores, catálogo, stock, pagos, reportes, usuarios, configuración)
- `UserRole` extended with `description` + `permissions` matrix: ADMIN (all 16), RECEPTIONIST (8 day-to-day ops, no usuarios/config/stock/catálogo), USER (8 read-only, no `_MANAGE`)
- `RoleCatalog` object — `roles()`, `permissionsFor(role)`, `can(role, permission)` (static source of truth; enforcement deferred to login/RBAC in backlog)

### UI (`ui/users/`)
- `RolesDialog` — role cards (name + description) + permission matrix (rows by module, checkmark per role)
- `UsersContent` header: «Roles y permisos» outlined button; `UsersScreen` `showRoles` state

### Verification
- Runtime probe (pure model, no DB): 27 checks (16 perms, 3 roles, ADMIN full, RECEPTIONIST/USER exact sets, `RoleCatalog.can`, 10 modules) — **ALL_ROLES2_CHECKS_OK**
- Smoke-run booted into Users: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)

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
