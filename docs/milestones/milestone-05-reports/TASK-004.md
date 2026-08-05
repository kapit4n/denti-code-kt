# TASK-004: Reports — filters & saved ranges

## Objective

Improve the Reports filter bar: **persist the selected date range across runs**, allow **named range presets** (save the current Desde/Hasta under a custom name, apply/delete them from the same chip row) and add **keyboard/mouse improvements** to the filter bar.

## What was added

### Persistence (`ui/reports/ReportSettingsStore.kt`)
- New `ReportSettingsStore` object — best-effort persistence of the report filter to `~/.denti-code-kt/reports-settings.properties` (same local/offline pattern as `DocumentStore`).
- `ReportSettings(startDate, endDate, presets)` and `SavedReportRange(name, startDate, endDate)` models; `load(defaultStart, defaultEnd)` falls back to defaults when the file is missing or corrupted; `save(...)` writes ISO dates + preset list.
- Tolerates legacy/corrupt entries (each preset row validated individually).

### Screen (`ui/reports/ReportsScreen.kt`)
- On init, loads the saved range/presets from disk (IO); the KPI/chart overview then recomputes for the restored range.
- Any range change (chip, date picker or preset) or preset add/remove is persisted automatically (`LaunchedEffect` on `startDate`/`endDate`/`presets`, guarded until the initial load finishes).
- `savePreset(name)` (deduplicates by name, cap 12) and `removePreset(name)` with messenger feedback.

### UI (`ui/reports/ReportsContent.kt`)
- **ReportsRangeBar** now renders: the 5 quick chips → saved preset chips (each with a trailing ✕ delete `IconButton`) → a **«Guardar rango»** chip (`+` icon) that opens a save dialog.
- **SaveRangeDialog**: `AppBasicDialog` + `AppTextField` for the preset name (blank names are rejected silently by keeping the dialog open).
- **Keyboard shortcuts** on the chip row: `Alt+1…5` applies a quick range, `Alt+Mayús+1…9` applies a saved preset; a caption below the chips documents both (`Atajos: Alt+1…5 = rango rápido · Alt+Mayús+1…9 = rango guardado`).
- Mouse: hover-driven delete icon on preset chips; chips keep the existing Material `FilterChip` hover/selected states.

## Components added / updated

| Component | Status |
|-----------|--------|
| `ReportSettingsStore`, `ReportSettings`, `SavedReportRange` | Added |
| `ReportsScreen` init-load + auto-persist + preset add/remove | Updated |
| `ReportsRangeBar` presets + «Guardar rango» chip + keyboard shortcuts + hint | Updated |
| `SaveRangeDialog` | Added |

## Verification

- Runtime probe (temporary, removed, `user.home` redirected to a temp dir): defaults when no file, full save→load round-trip of range + 2 presets, preset-list clear, file written, and `reportsOverview` still returns revenue — **ALL REPORT4 CHECKS OK**.
- Smoke-run booted directly into Reports: 75 s, no exceptions (first launch wrote the settings file with the default 30-day range).
- ✅ `./gradlew build` — 0 errors.

## Next Task

TASK-005 (provisional): PDF/HTML report printing — render the full report (KPIs + charts + tables) to a printable document and print/save from the Reports screen.
