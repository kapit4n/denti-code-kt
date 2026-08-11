# TASK-001: Administration — Users management

## Objective

Replace the Users placeholder stub with a real **users management screen**: list users with their roles, register new users, edit users, toggle active/inactive and delete them, with email-uniqueness validation. Data lives in the existing `users` / `user_roles` tables (seeded with `admin@sonrisa-clinica.bo` ADMIN and `recepcion@sonrisa-clinica.bo` RECEPTIONIST).

## What was added

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- `UserRole` enum (ADMIN / RECEPTIONIST / USER) with Spanish `displayLabel` and `fromDb` fallback (seeded strings are kept as-is in `user_roles.role`).
- `AppUser`, `UserDirectoryKpis(totalUsers, activeUsers, adminCount)`, `UserRegistrationRequest`, `UserUpdateRequest`.
- Repository methods: `listUsers()` (KPIs + rows, role via `user_roles`, primary role priority ADMIN > RECEPTIONIST > USER), `registerUser`, `updateUser` (email/name/role/active + optional new password, role row upsert), `setUserActive`, `deleteUser` (cascades `user_roles`), `findUserByEmail(email, excludeId)`.
- Validation: email required + contains `@`, display name required, password ≥ 6 chars, email uniqueness (case-insensitive, `excludeId` on edit).
- Password hashed with **SHA-256** (`hashPassword`, JDK only — no bcrypt lib in the project; documented as provisional, real bcrypt/Argon2 + login deferred to the post-milestone backlog).

### UI (`ui/users/`)
- `UsersUiModels.kt` — `UserSortOrder` (Nombre A-Z/Z-A, más/menos recientes), `UserUiModel`, `buildUsersUiState` (search by name/email/role, role + status filters, sorting).
- `UsersDialogs.kt` — `UserRegistrationDialog` / `UserEditDialog` (email, name, role dropdown, password — required on create, optional change on edit), `UserToggleActiveDialog`, `UserDeleteDialog`.
- `UsersContent.kt` — header + «Nuevo usuario», KPI row (usuarios / activos / administradores), search + role/status/sort dropdowns, users table with per-row edit / toggle / delete actions, empty states.
- `UsersScreen.kt` — orchestration (load, register, edit, toggle, delete with confirm dialogs + messenger feedback).
- `AppShell` now routes `ScreenRoute.Users` → `UsersScreen(repo)` (was the shared placeholder).

## Components added / updated

| Component | Status |
|-----------|--------|
| `UserRole`, `AppUser`, `UserDirectoryKpis`, `UserRegistrationRequest`, `UserUpdateRequest` | Added |
| `listUsers` / `registerUser` / `updateUser` / `setUserActive` / `deleteUser` / `findUserByEmail` (+ `hashPassword`, `primaryRole`) | Added |
| `ui/users/` (UsersScreen, UsersContent, UsersDialogs, UsersUiModels) | Added |
| `AppShell` Users route wiring | Updated |

## Verification

- Runtime probe (temporary, removed; `user.home` redirected to a temp dir with a fresh DB): 17 checks — seed has 2 users (1 admin + 1 receptionist), register adds the user (id, normalized email lookup, KPIs), duplicate email and short password rejected, edit changes role/active + rejects duplicate email, `setUserActive` works, delete removes the user — **ALL_USERS1_CHECKS_OK**.
- Smoke-run booted directly into Users: 75 s, no exceptions.
- ✅ `./gradlew build` — 0 errors.

## Next Task

TASK-002: Roles & permissions — role catalog + permission matrix (see `docs/roadmap/NEXT_TASK.md`).
