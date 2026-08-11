# TASK-002: Administration — Roles & permissions

## Objective

Add the **role catalog and permission matrix** to Milestone 6: the three roles (Administrador / Recepcionista / Usuario) get a Spanish description and an explicit set of permissions, exposed in a matrix UI (rows = permissions grouped by functional module, columns = roles) reachable from the Users screen. Pure model + UI task: the catalog is static code (`RoleCatalog`), so no DB migration and no UI screen changes to the other tabs were needed. RBAC **enforcement** (hiding screens/actions per role) intentionally stays deferred to the post-milestone backlog, which is where real login + sessions will land.

## What was added

### Data layer (`data/Models.kt`)
- `Permission` enum — 16 permissions with Spanish `labelEs`, grouped by functional module via `moduleEs` (Dashboard, Citas, Pacientes, Doctores, Catálogo clínico, Stock insumos, Pagos, Reportes, Usuarios, Configuración); each module has a `_VIEW` and (for most) a `_MANAGE` level.
- `UserRole` extended with `description` (Spanish) and `permissions` — the **permission matrix**:
  - **ADMIN** → all 16 permissions.
  - **RECEPTIONIST** → 8: dashboard, citas ver/gestionar, pacientes ver/gestionar, pagos ver/gestionar, reportes ver. No acceso a doctores, catálogo, stock, usuarios ni configuración.
  - **USER** → 8 read-only: dashboard, citas ver, pacientes ver, doctores ver, catálogo ver, stock ver, pagos ver, reportes ver. Sin ningún permiso `_MANAGE`.
- `RoleCatalog` object — the static source of truth: `roles()`, `permissionsFor(role)`, `can(role, permission)`.

### UI (`ui/users/RolesDialog.kt`)
- `RolesDialog` — dialog with the three role cards (name + description), then the **permission matrix**: header row with the 3 roles, module header bands, and per-permission rows with a checkmark (allowed) or muted icon (not allowed) per role.
- `UsersContent` header: new «Roles y permisos» outlined button (`AppOutlinedButton`, `Icons.Default.Groups`) next to «Nuevo usuario».
- `UsersScreen`: `showRoles` state opens/closes the dialog.

## Components added / updated

| Component | Status |
|-----------|--------|
| `Permission` (16 permissions), `RoleCatalog` | Added |
| `UserRole` (`description`, `permissions` matrix) | Extended |
| `ui/users/RolesDialog.kt` | Added |
| `UsersContent` (header «Roles y permisos»), `UsersScreen` (`showRoles`) | Updated |

## Verification

- Runtime probe (temporary, removed; pure model — no DB needed): 27 checks — 16 permissions + 3 roles defined, all labels/descriptions non-blank, ADMIN holds all permissions, RECEPTIONIST has exactly its 8 (lacks USERS_MANAGE / SETTINGS_MANAGE / INVENTORY_VIEW / PROCEDURES_VIEW), USER has exactly its 8 read-only and no `_MANAGE`, `RoleCatalog.can` correct, 10 functional modules, `moduleEs` mapping — **ALL_ROLES2_CHECKS_OK**.
- Smoke-run booted directly into Users: 75 s, no exceptions.
- ✅ `./gradlew build` — 0 errors.

## Next Task

TASK-003: Settings — configuration screen (clinic profile / general settings) replacing the last placeholder stub (see `docs/roadmap/NEXT_TASK.md`).
