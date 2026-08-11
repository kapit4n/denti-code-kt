# Next Task

## Milestone 6 — Administration

**Objective**: Implement the administration module — users, roles, permissions, clinic profile, settings, theme persistence, language, business hours, backup and restore.

## Milestone 6 — Task Breakdown

| Task | Description |
|------|-------------|
| TASK-001 | Users management — list/register/edit/toggle-active/delete users with role assignment | ✅ DONE |
| TASK-002 | Roles & permissions — role catalog + permission matrix | ✅ DONE |
| TASK-003 | Settings screen — clinic profile (name, city, country, phone, currency) persisted |
| TASK-004 | Theme persistence — dark/light theme saved across runs |
| TASK-005 | Language preference — `preferred_locale` persisted and applied |
| TASK-006 | Business hours — clinic opening hours configuration per day |
| TASK-007 | Backup — export SQLite DB copy via native save dialog |
| TASK-008 | Restore — import a DB backup with confirmation |
| TASK-009 | Administration polish & review |
| TASK-010 | Administration audit |

## TASK-003 (next up)

Settings screen — clinic profile: replace the last placeholder stub with a real Settings screen. Persist clinic profile (name, city, country, phone, currency) in a `clinic_settings`-style table; read/write via repository, with KPI-style summary and save feedback. Theme persistence (TASK-004) and language (TASK-005) build on the same screen afterwards.

> Previous: **Milestone 5 — Reports COMPLETE** (`docs/milestones/milestone-05-reports/`). Post-milestone ideas moved to `docs/roadmap/backlog.md`. M6 progress: TASK-001 + TASK-002 done.
