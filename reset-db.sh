#!/usr/bin/env bash
# Remove local SQLite so the next app start recreates schema and demo seed data.
set -euo pipefail
db="${HOME:?}/.denti-code-kt/denti-clinic.db"
if [[ -f "$db" ]]; then
  rm -f "$db"
  echo "Removed: $db"
else
  echo "No file at $db (nothing to remove)."
fi
echo "Start the app with ./start.sh or: ./gradlew run --args='--reset-local-db'"
echo "(The --reset-local-db flag deletes the file on startup before connecting.)"
