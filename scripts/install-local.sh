#!/usr/bin/env bash
#
# Installs the locally built Denti-Code .deb package.
#
#   ./scripts/build-installer.sh     # produce the installer first
#   ./scripts/install-local.sh       # install it with sudo apt
#
# The installer only replaces application files under /opt/denti-code.
# The clinic database in ~/.denti-code-kt/ is never read, moved or deleted here.
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

DEB_OUTPUT_DIR="${PROJECT_ROOT}/build/compose/binaries/main/deb"
RELEASE_DIR="${PROJECT_ROOT}/build/release"
USER_DATA_DIR="${HOME}/.denti-code-kt"

info() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { printf '\n\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }

info "Looking for a Denti-Code installer in ${RELEASE_DIR}"
# build-installer.sh publishes a copy under build/release with a stable name; fall back to the raw
# jpackage output directory (which may carry a Debian packaging revision in the file name).
CANDIDATES=()
if compgen -G "${RELEASE_DIR}/*.deb" >/dev/null 2>&1; then
    mapfile -t CANDIDATES < <(find "${RELEASE_DIR}" -maxdepth 1 -type f -name '*.deb' | sort)
fi
if [[ ${#CANDIDATES[@]} -eq 0 ]]; then
    if [[ -d "${DEB_OUTPUT_DIR}" ]]; then
        mapfile -t CANDIDATES < <(find "${DEB_OUTPUT_DIR}" -maxdepth 1 -type f -name '*.deb' | sort)
    else
        fail "No installer directory. Run ./scripts/build-installer.sh first."
    fi
fi

# Prefer the package built from the current Gradle version. The Debian upstream version is the part
# before the packaging revision ("1.0.0-1" -> "1.0.0"), so the file name is never parsed.
if command -v dpkg-deb >/dev/null 2>&1; then
    VERSION="$(cd "${PROJECT_ROOT}" && ./gradlew --console=plain -q printVersion 2>/dev/null | tail -n 1 | tr -d '[:space:]' || true)"
    if [[ -n "${VERSION}" ]]; then
        MATCHES=()
        for candidate in "${CANDIDATES[@]}"; do
            deb_version="$(dpkg-deb -f "${candidate}" Version 2>/dev/null || true)"
            [[ -n "${deb_version}" && "${deb_version%%-*}" == "${VERSION}" ]] && MATCHES+=("${candidate}")
        done
        if [[ ${#MATCHES[@]} -eq 1 ]]; then
            CANDIDATES=("${MATCHES[0]}")
        fi
    fi
fi

[[ ${#CANDIDATES[@]} -ge 1 ]] || fail "No .deb found. Run ./scripts/build-installer.sh first."
[[ ${#CANDIDATES[@]} -eq 1 ]] || {
    printf 'Found %d installers:\n' "${#CANDIDATES[@]}" >&2
    printf '  %s\n' "${CANDIDATES[@]}" >&2
    fail "Refusing to guess. Delete the stale ones or rebuild with ./gradlew clean packageDeb."
}

DEB="${CANDIDATES[0]}"
[[ -s "${DEB}" ]] || fail "The installer is empty: ${DEB}"

if command -v dpkg-deb >/dev/null 2>&1; then
    echo "  Package:  $(dpkg-deb -f "${DEB}" Package)"
    echo "  Version:  $(dpkg-deb -f "${DEB}" Version)"
    echo "  Arch:     $(dpkg-deb -f "${DEB}" Architecture)"
fi
echo "Installing: ${DEB}"

info "Installing with sudo apt install (sudo is used for this step only)"
if command -v apt >/dev/null 2>&1; then
    sudo apt install -y "./${DEB}"
else
    sudo dpkg -i "${DEB}"
    sudo apt-get install -f -y
fi

info "Done"
echo "  Launch Denti-Code from the Applications menu, or run: /opt/denti-code/bin/Denti-Code"
if [[ -d "${USER_DATA_DIR}" ]]; then
    echo "  Existing clinic data left untouched: ${USER_DATA_DIR}"
else
    echo "  No data directory yet: it will be created at ${USER_DATA_DIR} on first launch."
fi
echo "  Uninstall (keeps data):   sudo apt remove denti-code"
echo "  Uninstall + erase data:   sudo apt remove --purge denti-code && rm -rf ${USER_DATA_DIR}"
