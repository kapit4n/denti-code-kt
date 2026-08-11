# Milestone 6 — Administration

## Goal

Implement the administration module: users, roles, permissions, clinic profile, settings, theme persistence, language, business hours, backup and restore.

## Features (accumulated)

- TASK-001: Users management screen — list users (name, email, role, state, created), register (email, name, role, password, active), edit, toggle active/inactive, delete, with email-uniqueness validation and SHA-256 password hashing (provisional). Real data from `users` / `user_roles` (seeded ADMIN + RECEPTIONIST).

## Deliverable

Complete administration module.

## Tasks

| Task | Description | Status |
|------|-------------|--------|
| TASK-001 | Users management — list/register/edit/toggle-active/delete users with role assignment | DONE |
| TASK-002 | Roles & permissions — role catalog + permission matrix | PENDING |
| TASK-003 | Settings screen — clinic profile (name, city, country, phone, currency) persisted | PENDING |
| TASK-004 | Theme persistence — dark/light theme saved across runs | PENDING |
| TASK-005 | Language preference — `preferred_locale` persisted and applied | PENDING |
| TASK-006 | Business hours — clinic opening hours configuration per day | PENDING |
| TASK-007 | Backup — export SQLite DB copy via native save dialog | PENDING |
| TASK-008 | Restore — import a DB backup with confirmation | PENDING |
| TASK-009 | Administration polish & review | PENDING |
| TASK-010 | Administration audit | PENDING |

## Status

**IN PROGRESS** — TASK-001 done: the Users route is now a real management screen with CRUD, role assignment and email-uniqueness validation.
