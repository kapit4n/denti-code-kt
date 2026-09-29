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
# The artifact *name* is not a reliable selector: jpackage may or may not append a Debian packaging
# revision ("denti-code_1.0.0_amd64.deb" vs "denti-code_1.0.0-1_amd64.deb") depending on the jpackage
# build in use. The Debian control metadata is the source of truth, so select on the upstream version
# (the part before the revision) instead of on the file name.
command -v dpkg-deb >/dev/null 2>&1 || fail "dpkg-deb is required to verify the package (apt install dpkg)."

upstream_version() { printf '%s' "${1%%-*}"; }

MATCHES=()
STALE=()
while IFS= read -r deb; do
    [[ -n "${deb}" ]] || continue
    deb_version="$(dpkg-deb -f "${deb}" Version 2>/dev/null || true)"
    if [[ -n "${deb_version}" && "$(upstream_version "${deb_version}")" == "${VERSION}" ]]; then
        MATCHES+=("${deb}")
    else
        STALE+=("${deb}")
    fi
done < <(find "${DEB_OUTPUT_DIR}" -maxdepth 1 -type f -name '*.deb' 2>/dev/null | sort)

if [[ ${#STALE[@]} -gt 0 ]]; then
    printf 'Ignoring installer(s) for other versions:\n'
    printf '  %s\n' "${STALE[@]}"
fi
[[ ${#MATCHES[@]} -eq 1 ]] || {
    printf 'Found %d installer(s) for version %s in %s:\n' "${#MATCHES[@]}" "${VERSION}" "${DEB_OUTPUT_DIR}" >&2
    printf '  %s\n' "${MATCHES[@]:-<none>}" >&2
    fail "Expected exactly one .deb for version ${VERSION}. Run ./gradlew clean packageDeb and try again."
}
DEB="${MATCHES[0]}"

info "Verifying the installer"
[[ -s "${DEB}" ]] || fail "The generated installer is empty: ${DEB}"

DEB_PACKAGE="$(dpkg-deb -f "${DEB}" Package)"
DEB_VERSION="$(dpkg-deb -f "${DEB}" Version)"
DEB_ARCH="$(dpkg-deb -f "${DEB}" Architecture)"
[[ "${DEB_PACKAGE}" == "${EXPECTED_DEB_PACKAGE}" ]] || fail "Unexpected Debian package name '${DEB_PACKAGE}' (expected '${EXPECTED_DEB_PACKAGE}')."
[[ "$(upstream_version "${DEB_VERSION}")" == "${VERSION}" ]] || fail "Deb version '${DEB_VERSION}' does not match the Gradle version '${VERSION}'."

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

# Publish a copy under a stable, predictable name. Release automation globs build/release/*.deb
# instead of re-deriving jpackage's file naming, so it does not have to know about the revision.
RELEASE_DIR="${PROJECT_ROOT}/build/release"
RELEASE_DEB="${RELEASE_DIR}/${EXPECTED_DEB_PACKAGE}_${VERSION}_amd64.deb"
mkdir -p "${RELEASE_DIR}"
rm -f "${RELEASE_DIR}"/*.deb
cp "${DEB}" "${RELEASE_DEB}"

info "Done"
echo "  Version:     ${VERSION}"
echo "  Debian ver:  ${DEB_VERSION}"
echo "  Installer:   ${RELEASE_DEB}"
echo
echo "Install it with:"
echo "  sudo apt install ./$(basename "${RELEASE_DEB}")"
echo "User data stays in ~/.denti-code-kt/ and is never touched by the installer."
