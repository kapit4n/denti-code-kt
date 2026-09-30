#!/usr/bin/env bash
#
#   ./scripts/dev-run.sh [app-args…]
#
# Manual-testing launcher that never blocks on a broken build:
#   1. Tries to build and package the CURRENT code (createDistributable).
#   2. On success it saves the bundle as the "last successful build" and runs it.
#   3. On failure it runs the saved last successful build instead, so testing of
#      the rest of the system can continue while another part is being worked on.
#
# The last successful build is kept at build/last-successful/Denti-Code/ and is
# replaced every time a build succeeds. Launch logs go to build/manual-run.log.
#
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${PROJECT_ROOT}"

SNAPSHOT_ROOT="${PROJECT_ROOT}/build/last-successful"
SNAPSHOT_APP="${SNAPSHOT_ROOT}/Denti-Code"
SNAPSHOT_BIN="${SNAPSHOT_APP}/bin/Denti-Code"
RUN_LOG="${PROJECT_ROOT}/build/manual-run.log"

info() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
note() { printf '\033[0;90m    %s\033[0m\n' "$*"; }
fail() { printf '\n\033[1;31mERROR: %s\033[0m\n' "$*" >&2; }

launch() {
    local bin="$1" label="$2"
    local log
    mkdir -p "${PROJECT_ROOT}/build" "${SNAPSHOT_ROOT}"
    if [[ ! -x "${bin}" ]]; then
        fail "'${bin}' is not executable."
        exit 1
    fi
    log="${RUN_LOG}"
    info "Launching ${label}"
    note "Binary: ${bin}"
    note "Log:    ${log}"
    nohup "${bin}" "$@" >"${log}" 2>&1 &
    echo "    PID:   $!"
    echo
    echo "Running in the background. Close the app window to stop it; tail the log with:"
    echo "  tail -f '${log}'"
    exit 0
}

info "Building the current code (createDistributable)"
if ./gradlew --console=plain createDistributable; then
    DIST="$(find build/compose/binaries/main/app -maxdepth 2 -name 'Denti-Code' -type d 2>/dev/null | head -n1)"
    if [[ -z "${DIST}" || ! -x "${DIST}/bin/Denti-Code" ]]; then
        fail "The build succeeded but no launcher was produced (looked for ${DIST}/bin/Denti-Code)."
        exit 1
    fi
    # Keep the last successful build so a later broken change can still be tested.
    rm -rf "${SNAPSHOT_APP}"
    mkdir -p "${SNAPSHOT_ROOT}"
    cp -a "${DIST}" "${SNAPSHOT_APP}"
    info "Saved the last successful build to ${SNAPSHOT_APP}"
    launch "${SNAPSHOT_BIN}" "the freshly built code"
fi

info "The current code failed to build; falling back to the last successful build."
if [[ -x "${SNAPSHOT_BIN}" ]]; then
    note "Run the build on its own to see the error, e.g.: ./gradlew createDistributable"
    launch "${SNAPSHOT_BIN}" "the last successful build"
fi

fail "No last successful build at ${SNAPSHOT_BIN}."
fail "Fix the compilation error and run this script again."
exit 1