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
# pinDebDependencies rewrites the ldd-derived Depends with a portable list. Without it the .deb only
# installs on the build host's Ubuntu release (see defaultLinuxPackageDependencies in build.gradle.kts).
./gradlew --console=plain packageDeb pinDebDependencies

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

# Portability: a Depends entry with a "|" alternative is satisfied by whichever package the target
# release ships. A bare t64-suffixed name (Ubuntu 24.04's 64-bit-time_t renames) is installable only on
# 24.04+, so it is a hard failure here rather than a broken installer in the field.
DEB_DEPENDS="$(dpkg-deb -f "${DEB}" Depends)"
UNPORTABLE=()
while IFS= read -r dep; do
    dep="$(printf '%s' "${dep}" | tr -d ' ')"
    [[ -n "${dep}" ]] || continue
    if [[ "${dep}" == *t64 && "${dep}" != *"|"* ]]; then
        UNPORTABLE+=("${dep}")
    fi
done < <(printf '%s' "${DEB_DEPENDS}" | tr ',' '\n')
if [[ ${#UNPORTABLE[@]} -gt 0 ]]; then
    printf 'Depends entries that only exist on Ubuntu 24.04+:\n' >&2
    printf '  %s\n' "${UNPORTABLE[@]}" >&2
    fail "Use a \"pkgA | pkgB\" alternative so the .deb installs on other Ubuntu releases."
fi

# Completeness: every shared library the app links against must be reachable through Depends.
# jpackage only ldd-s the launcher, so the JRE's own libraries (libfontmanager.so ->
# libharfbuzz/libstdc++/libgcc_s) are invisible to it and a missing entry only shows up as an
# UnsatisfiedLinkError at first launch -- long after apt reported a successful install. Derive the
# real closure from the packaged binaries and check it against Depends here instead.
info "Checking that Depends covers every linked library"
PAYLOAD_STAGE="$(mktemp -d)"
trap 'rm -rf "${PAYLOAD_STAGE}"' EXIT
if ! dpkg-deb -x "${DEB}" "${PAYLOAD_STAGE}" 2>/dev/null; then
    fail "Could not unpack ${DEB} to inspect its shared library dependencies."
fi

# Sonames shipped inside the payload (the JRE and app libraries) are satisfied by the package itself.
mapfile -t BUNDLED < <(
    find "${PAYLOAD_STAGE}" -type f -name "*.so*" -printf '%f\n' 2>/dev/null \
        | sed 's/-.*//' | sort -u
)
is_bundled() {
    local name="$1" b
    for b in "${BUNDLED[@]}"; do
        [[ "${name}" == "${b}"* ]] && return 0
    done
    return 1
}

# Only the packages that ship a soname we actually link, mapped to what that soname is called.
# Anything not listed here is treated as a gap, so adding a library without a mapping fails the build.
declare -A SONAME_PKG=(
    [libasound.so.2]="libasound2t64|libasound2"
    [libbrotlidec.so.1]="libbrotli1"
    [libbrotlicommon.so.1]="libbrotli1"
    [libbsd.so.0]="libbsd0"
    [libbz2.so.1.0]="libbz2-1.0"
    [libc.so.6]="libc6"
    # libdl, libpthread and librt are glibc sonames exposed by the JRE's own .so files; libc6 provides all three.
    [libdl.so.2]="libc6"
    [libpthread.so.0]="libc6"
    [librt.so.1]="libc6"
    [libexpat.so.1]="libexpat1"
    [libfontconfig.so.1]="libfontconfig1"
    [libfreetype.so.6]="libfreetype6"
    [libgcc_s.so.1]="libgcc-s1"
    [libgif.so.7]="libgif7"
    [libGL.so.1]="libgl1"
    [libGLdispatch.so.0]="libgl1"
    [libGLX.so.0]="libglx0"
    [libharfbuzz.so.0]="libharfbuzz0b"
    [libjpeg.so.8]="libjpeg-turbo8"
    [liblcms2.so.2]="liblcms2-2"
    [libm.so.6]="libc6"
    [libmd.so.0]="libmd0"
    [libpng16.so.16]="libpng16-16t64|libpng16-16"
    [libstdc++.so.6]="libstdc++6"
    [libX11.so.6]="libx11-6"
    [libXau.so.6]="libxau6"
    [libxcb.so.1]="libxcb1"
    [libXdmcp.so.6]="libxdmcp6"
    [libXext.so.6]="libxext6"
    [libXi.so.6]="libxi6"
    [libXrender.so.1]="libxrender1"
    [libXtst.so.6]="libxtst6"
    [libz.so.1]="zlib1g"
)

LINKED=()
while IFS= read -r soname; do
    [[ -n "${soname}" ]] || continue
    is_bundled "${soname}" && continue
    LINKED+=("${soname}")
done < <(
    find "${PAYLOAD_STAGE}" -type f \( -name "*.so" -o -name "*.so.*" -o -name "Denti-Code" \) -print0 \
        | xargs -0 -r readelf -d 2>/dev/null \
        | grep -oE 'Shared library: \[lib[^]]+\]' \
        | sed -E 's/^.*\[//; s/\]$//' | sort -u
)

MISSING=()
for soname in "${LINKED[@]}"; do
    pkg="${SONAME_PKG[${soname}]:-}"
    if [[ -z "${pkg}" ]]; then
        MISSING+=("${soname} (no SONAME_PKG mapping -- add it to scripts/build-installer.sh)")
        continue
    fi
    if ! printf '%s' "${DEB_DEPENDS}" | tr ' ' '\n' | tr ',' '\n' | grep -qxF "$(printf '%s' "${pkg}" | cut -d'|' -f1 | tr -d ' ')"; then
        MISSING+=("${soname} (provided by '${pkg}', which is not in Depends)")
    fi
done
if [[ ${#MISSING[@]} -gt 0 ]]; then
    printf 'Shared libraries the app links but Depends does not provide:\n' >&2
    printf '  %s\n' "${MISSING[@]}" >&2
    fail "Add them to defaultLinuxPackageDependencies in build.gradle.kts (then re-run pinDebDependencies)."
fi
printf '  %d linked librar%s covered by Depends\n' "${#LINKED[@]}" "$([[ ${#LINKED[@]} -eq 1 ]] && echo y || echo ies)"

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
# An absolute path: apt only accepts a relative "./name.deb" from the directory that holds it, and
# otherwise fails with "E: Unsupported file ./... given on commandline".
echo "  sudo apt install '${RELEASE_DEB}'"
echo "or run ./scripts/install-local.sh, which resolves the path for you."
echo "User data stays in ~/.denti-code-kt/ and is never touched by the installer."
