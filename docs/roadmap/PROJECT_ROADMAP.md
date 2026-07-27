# Denti-Code Desktop - Master Project Roadmap

Version: 1.0

Last Updated: 2026-07-24

---

# Project Vision

Denti-Code Desktop is a modern dental clinic management application built with Kotlin Compose Desktop.

The objective is to transform the project from a functional MVP into a production-quality desktop application that could realistically be used in dental clinics.

Every improvement must preserve:

- Kotlin Compose Desktop
- Material 3
- Existing architecture
- Existing navigation
- Existing design language

The application should feel modern, professional, and easy to maintain.

---

# Development Philosophy

Never attempt large refactors in one iteration.

Instead:

Small Tasks

↓

Document

↓

Review

↓

Continue

Every task must leave the project in a buildable state.

---

# General Rules

Always:

✔ Build after every task

✔ Reuse existing components

✔ Avoid duplicated composables

✔ Follow Material 3

✔ Document every change

✔ Update roadmap

✔ Keep architecture clean

Never:

✘ Break unrelated modules

✘ Refactor multiple systems simultaneously

✘ Skip documentation

✘ Leave unfinished implementations

---

# Documentation Structure

Maintain the following structure:

docs/

    roadmap/

        PROJECT_ROADMAP.md

        progress.md

        backlog.md

    milestones/

        milestone-01-ui/

        milestone-02-crud/

        milestone-03-patient/

        milestone-04-inventory/

        milestone-05-reports/

        milestone-06-administration/

        milestone-07-automation/

        milestone-08-production/

        milestone-09-ai/

        milestone-10-commercial/

Every milestone contains:

README.md

CHANGELOG.md

DECISIONS.md

SCREENSHOTS.md

TASK-001.md

TASK-002.md

...

---

# Progress Tracking

Maintain:

docs/roadmap/progress.md

Example

Milestone 1

██████░░░░ 60%

Task 6 / 10

Milestone 2

██░░░░░░░░ 20%

Never lose progress.

Always update after finishing a task.

---

# Master Milestones

---

# Milestone 1

Professional UI Polish

Goal

Improve every implemented screen until it reaches production quality.

Includes

Dashboard

Appointments

Patients

Doctors

Procedures

Inventory

Payments

Tasks include

- spacing
- borders
- typography
- dialogs
- cards
- responsive layouts
- hover states
- empty states
- loading states
- accessibility
- reusable components

Deliverable

A polished desktop application.

Estimated

10 tasks

---

# Milestone 2

Complete CRUD

Goal

Complete every missing CRUD operation.

Patients

- Edit
- Delete

Procedures

- Edit
- Delete

Payments

- Edit
- Void

Inventory

- Create
- Update
- Adjust
- Record movement

Consultories

- CRUD

Facilities

- CRUD

Deliverable

Every entity is manageable.

Estimated

12 tasks

---

# Milestone 3

Patient Workspace

Transform Patient Detail into a complete clinical workspace.

Features

Clinical history

Timeline

Treatments

Payments

Appointments

Files

Observations

Treatment plans

Pending balance

Receipts

Exports

Patient summary

Deliverable

Complete patient management.

Estimated

10 tasks

---

# Milestone 4

Inventory Management

Turn inventory into a real inventory module.

Features

Purchase entries

Adjustments

Transfers

Suppliers

Consumption

Expiration dates

Lots

Movement history

Valuation

Reports

Deliverable

Professional inventory management.

Estimated

10 tasks

---

# Milestone 5

Reports

Implement a dedicated reporting module.

Reports

Appointments

Revenue

Doctors

Patients

Treatments

Inventory

Charts

KPIs

Filters

Exports

Printing

Deliverable

Business intelligence dashboard.

Estimated

12 tasks

---

# Milestone 6

Administration

Implement

Users

Roles

Permissions

Clinic profile

Settings

Theme persistence

Language

Business hours

Backup

Restore

Deliverable

Complete administration module.

Estimated

10 tasks

---

# Milestone 7

Automation

Implement

Appointment reminders

WhatsApp

Email

Notifications

Low stock alerts

Automatic backups

Task scheduler

Deliverable

Automated workflows.

Estimated

8 tasks

---

# Milestone 8

Production Readiness

Implement

Authentication

Encryption

Testing

Dependency Injection

ViewModels

Performance

Logging

Crash handling

Installer

Updates

Deliverable

Production-ready architecture.

Estimated

12 tasks

---

# Milestone 9

AI Features

Implement

Clinical summary

Diagnosis assistant

Treatment recommendations

Appointment assistant

Cost estimation

Inventory prediction

Revenue insights

Voice transcription

Clinical notes generation

Deliverable

AI-powered dental software.

Estimated

10 tasks

---

# Milestone 10

Commercial Release

Implement

Licensing

Multi-clinic

Cloud synchronization

Subscriptions

Audit logs

Online backups

Deliverable

Commercial SaaS/Desktop product.

Estimated

10 tasks

---

# Task Rules

Every milestone is divided into small tasks.

Never complete an entire milestone in one context.

Example

Milestone 3

Task 1

Task 2

Task 3

...

Task 10

Each task should require approximately

30–90 minutes.

---

# Documentation Rules

Every completed task MUST create or update

TASK-XXX.md

Objective

Motivation

Files Modified

Components Added

Components Updated

Technical Notes

UI Notes

Screenshots Required

Next Task

---

# Changelog

Update

CHANGELOG.md

After every task.

Use

Added

Changed

Improved

Fixed

Removed

---

# Screenshots

Maintain

SCREENSHOTS.md

List

Before

After

Required screenshots

Never generate images automatically.

Only maintain documentation.

---

# Architectural Decisions

Maintain

DECISIONS.md

Every important decision should explain

Context

Alternatives

Decision

Consequences

---

# Code Quality Rules

Always

Extract reusable composables.

Avoid duplicate layouts.

Reduce nesting.

Improve readability.

Use descriptive names.

Prefer composition over duplication.

Keep files manageable.

---

# UI Rules

Always use

Material 3

8dp spacing system

Consistent typography

Consistent elevations

OutlineVariant borders

Responsive text

Ellipsis

Accessible touch targets

Hover states

Empty states

Loading states

---

# Completion Report

At the end of every OpenCode context provide

## Completed

Tasks completed

## Modified Files

List

## Documentation Updated

List

## Screenshots Required

List

## Build Status

Success / Failed

## Next Recommended Task

Continue from the roadmap.

---

# Scope Protection

Never start another task until the current task is fully completed.

Never mix milestones.

Never redesign unrelated modules.

Keep every iteration focused.

---

# Resume Rule

When starting a new OpenCode context:

1. Read

docs/roadmap/PROJECT_ROADMAP.md

2. Read

docs/roadmap/progress.md

3. Read the current milestone README.

4. Continue the first unfinished task.

Do not ask the user what to do next unless all tasks for the current milestone are complete.

---

# Current Priority

Priority Order

1. Professional UI Polish

2. Complete CRUD

3. Patient Workspace

4. Inventory Management

5. Reports

6. Administration

7. Automation

8. Production Readiness

9. AI Features

10. Commercial Release

Always finish the current milestone before moving to the next one.

---

# Final Objective

The goal is to build a production-quality dental clinic management system that demonstrates:

- Clean Architecture
- Excellent UI/UX
- Professional code quality
- Complete documentation
- Modular components
- Realistic workflows
- AI-assisted features
- Commercial readiness

Every task should move the project one step closer to this vision while remaining maintainable and fully documented.
