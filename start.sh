#!/usr/bin/env bash
# Denti-Code KT desktop (SQLite under ~/.denti-code-kt/denti-clinic.db).
# Fresh DB on this run: ./gradlew run --args='--reset-local-db'
# Or delete file then run: ./reset-db.sh && ./start.sh
#
# Verbose Citas tab: log Exposed SQL + every appointment row from listAppointments (console):
#   DENTI_LOG_CITAS_DATA=1 ./start.sh
#   ./gradlew run -Ddenti.logCitasData=true
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$root"
exec ./gradlew run
