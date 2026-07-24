# Decision 002: Demo Data Seeder Architecture

## Context

The project needed a comprehensive demo data seeder to populate the SQLite database with realistic data for development and demonstration. The existing `Seed.kt` was minimal (2 users, 5 doctors, 8 patients) and didn't cover payments, notes, audit, or inventory tables.

## Requirements

1. **Idempotent** — safe to call multiple times; only seeds if tables are empty
2. **Configurable** — data volumes adjustable via a config object
3. **Independent seeders** — each table has its own seeder object
4. **Dependency-aware** — seeders execute in topological order (e.g., appointments before payments)
5. **Coherent data** — foreign keys satisfied; data realistic for a Cochabamba dental clinic
6. **Seeded RNG** — deterministic output for reproducible demo data

## Alternatives Considered

### 1. Extend existing `Seed.kt`
- **Pros**: Minimal change, existing patterns
- **Cons**: Would make an already large file even larger; mixes reference data with demo data; no configurability

### 2. SQL seed scripts
- **Pros**: Database-native, portable
- **Cons**: Kotlin type safety lost; harder to generate realistic random data; Exposed ORM expectations bypassed

### 3. Current approach: Per-table Kotlin seeders with orchestrator
- **Pros**: Type-safe, testable, configurable, idempotent by design
- **Cons**: More files, slightly more complex setup

## Final Decision

**Per-table Kotlin seeders with a central orchestrator.** Each entity gets its own `object` seeder under `data/seeders/`. A `DemoDataSeeder` orchestrator calls them in dependency order. Configuration is a simple data class passed to each seeder.

### Key Design Choices

1. **`object` seeders** — stateless singletons; each has a `seed(config, ...)` method
2. **Dependency injection via parameters** — seeders receive lists of parent entities (e.g., `PatientsSeeder.SeedPatient`) rather than querying the database
3. **Internal `Seed*` data classes** — each seeder defines its own lightweight data class for returning seeded records to downstream seeders
4. **Seeded `Random(42)`** — deterministic RNG for reproducible data across app launches
5. **Coexistence with existing `Seed.kt`** — new seeder runs after existing seed functions; no modifications to existing code

### Execution Order

```
Users → Doctors → Patients → Procedures → Facilities → Consultories
    → Appointments → MaterialInventory → PerformedActions → Payments → NotesAudit
```

### Data Coherence

- All appointments reference valid patient/doctor/consultory IDs
- Payments reference valid appointment/procedure IDs
- Performed actions reference valid appointment/procedure/doctor IDs
- Material inventory references valid facility/consultory IDs
- Audit logs reference valid patient IDs with operation type = `CREATE`

## Consequences

- **Positive**: Type-safe, configurable, idempotent demo data
- **Positive**: Realistic data volumes for dashboard demo (300 appointments, 500+ payments)
- **Positive**: Easy to extend with new seeders
- **Negative**: 13 new files under `data/seeders/` (mitigated by clear naming)
- **Negative**: Existing `Seed.kt` still active (no removal, no conflict)
