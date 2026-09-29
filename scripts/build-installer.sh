#!/usr/bin/env bash
#
# Builds the Ubuntu (.deb) installer for Denti-Code.
#
#   ./scripts/build-installer.sh
#
# Runs the test suite, packages the Debian package, then verifies the artifact
# (name, version, Debian metadata, and that no SQLite database is bundled).
# Never installs anything and never needs sudo.
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

DEB_OUTPUT_DIR="${PROJECT_ROOT}/build/compose/binaries/main/deb"
EXPECTED_DEB_PACKAGE="denti-code"

info() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { printf '\n\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }

info "Checking the project root"
cd "${PROJECT_ROOT}"
[[ -f build.gradle.kts && -f settings.gradle.kts ]] || fail "Run this script from the project root (${PROJECT_ROOT} is not a Gradle project)."
[[ -x ./gradlew ]] || chmod +x ./gradlew || fail "./gradlew is not executable and could not be made executable."
command -v java >/dev/null 2>&1 || fail "java is not on PATH; install a JDK (the project targets Java 17)."
[[ "$(uname -s)" == "Linux" ]] || fail "The .deb package can only be built on Linux."

info "Reading the project version from Gradle (single source of truth)"
VERSION="$(./gradlew --console=plain -q printVersion | tail -n 1 | tr -d '[:space:]')"
[[ -n "${VERSION}" ]] || fail "Could not read the project version. Run ./gradlew printVersion to debug."
echo "Gradle project version: ${VERSION}"

info "Running the test suite"
./gradlew --console=plain test

info "Building the Debian package"
./gradlew --console=plain packageDeb

info "Locating the generated installer"
# Compose/jpackage derives the artifact name from the package name plus the Gradle version, so the
# version is the only reliable selector. Older .deb files from previous versions are reported, not used.
mapfile -t DEBS < <(find "${DEB_OUTPUT_DIR}" -maxdepth 1 -type f -name "*_${VERSION}_*.deb" 2>/dev/null | sort)
mapfile -t STALE < <(find "${DEB_OUTPUT_DIR}" -maxdepth 1 -type f -name '*.deb' 2>/dev/null | grep -v -- "_${VERSION}_" | sort || true)
if [[ ${#STALE[@]} -gt 0 ]]; then
    printf 'Ignoring stale installer(s) from other versions:\n'
    printf '  %s\n' "${STALE[@]}"
fi
[[ ${#DEBS[@]} -eq 1 ]] || {
    printf 'Found %d installer(s) for version %s in %s:\n' "${#DEBS[@]}" "${VERSION}" "${DEB_OUTPUT_DIR}" >&2
    printf '  %s\n' "${DEBS[@]:-<none>}" >&2
    fail "Expected exactly one .deb for version ${VERSION}. Run ./gradlew clean packageDeb and try again."
}
DEB="${DEBS[0]}"

info "Verifying the installer"
[[ -s "${DEB}" ]] || fail "The generated installer is empty: ${DEB}"

command -v dpkg-deb >/dev/null 2>&1 || fail "dpkg-deb is required to verify the package (apt install dpkg)."

# jpackage derives the artifact name from the Compose package name and the Gradle version.
[[ "$(basename "${DEB}")" == *"${VERSION}"* ]] || fail "Artifact name does not carry version ${VERSION}: $(basename "${DEB}")"

DEB_PACKAGE="$(dpkg-deb -f "${DEB}" Package)"
DEB_VERSION="$(dpkg-deb -f "${DEB}" Version)"
DEB_ARCH="$(dpkg-deb -f "${DEB}" Architecture)"
[[ "${DEB_PACKAGE}" == "${EXPECTED_DEB_PACKAGE}" ]] || fail "Unexpected Debian package name '${DEB_PACKAGE}' (expected '${EXPECTED_DEB_PACKAGE}')."
[[ "${DEB_VERSION}" == "${VERSION}" ]] || fail "Deb version '${DEB_VERSION}' does not match the Gradle version '${VERSION}'."

# The application data lives in ~/.denti-code-kt/ and must never be shipped inside the package.
if CONTENTS="$(dpkg-deb -c "${DEB}")"; then
    if printf '%s\n' "${CONTENTS}" | grep -qE '\.(db|sqlite|sqlite3)(-journal|-wal|-shm)?$'; then
        printf '%s\n' "${CONTENTS}" | grep -E '\.(db|sqlite|sqlite3)(-journal|-wal|-shm)?$' >&2
        fail "A SQLite database file is bundled in the installer. Remove it before packaging."
    fi
    printf '%s\n' "${CONTENTS}" | grep -qE '\./opt/' || fail "The installer does not install the application under /opt."
else
    fail "Could not read the contents of ${DEB}."
fi

printf '\n\033[1;32mInstaller verified\033[0m\n'
dpkg-deb -f "${DEB}" Package Version Architecture Section Depends | sed 's/^/  /'

info "Done"
echo "  Version:   ${VERSION}"
echo "  Installer: ${DEB}"
echo
echo "Install it with:"
echo "  sudo apt install ./$(basename "${DEB}")"
echo "User data stays in ~/.denti-code-kt/ and is never touched by the installer."
