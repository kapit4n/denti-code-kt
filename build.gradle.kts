import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

group = "com.denticode"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

// Application icons. They are optional: while a file is missing the Compose plugin falls back to its
// own default icon, so packaging never breaks. Drop the production asset in place and it is picked up
// with no further build changes. See src/main/resources/icons/README.md.
val linuxIcon = layout.projectDirectory.file("src/main/resources/icons/denti-code.png").asFile
val windowsIcon = layout.projectDirectory.file("src/main/resources/icons/denti-code.ico").asFile
val macIcon = layout.projectDirectory.file("src/main/resources/icons/denti-code.icns").asFile
// jpackage renders the Debian Maintainer field as "<vendor> <<linux-deb-maintainer>>", so this property
// is the *contact address* only. Override it per machine/CI with -Pdenti.deb.maintainer=... .
// Named differently on purpose: inside `linux { }` the name `debMaintainer` resolves to the DSL property.
val defaultDebMaintainer =
    providers.gradleProperty("denti.deb.maintainer").getOrElse("maintainer@denti-code.local")

// Explicit Linux package dependencies. Left unset, jpackage derives Depends from `ldd` on whatever
// machine builds the .deb, so the artifact only installs on the build host's release. Concretely, the
// CI runner (Ubuntu 24.04) emits `libasound2t64` and `libpng16-16t64` -- the 64-bit-time_t renames --
// and apt reports "unmet dependencies" everywhere else, e.g. on Ubuntu 23.10 where the only
// `libpng16` is `libpng16-16`.
//
// "|" is a Debian dependency alternative, so each renamed library accepts either spelling and a
// single .deb installs on Ubuntu 22.04, 23.10 and 24.04 alike. Keep the list to what the launcher,
// libjvm and the JavaFX natives actually link against (verified via ldd); extra entries here are
// what made the package unportable in the first place.
// Override with -Pdenti.linux.deps="libc6,zlib1g,..." if you need to trim it further.
val defaultLinuxPackageDependencies =
    providers.gradleProperty("denti.linux.deps").getOrElse(
        listOf(
            "libasound2t64 | libasound2",
            "libbrotli1",
            "libbsd0",
            "libbz2-1.0",
            "libc6",
            "libexpat1",
            "libfontconfig1",
            "libfreetype6",
            "libgcc-s1",
            "libgif7",
            "libgl1",
            "libglvnd0",
            "libglx0",
            // These three are linked by the JRE's imaging libraries (libjavajpeg, libjimage, libawt).
            "libjpeg-turbo8",
            "liblcms2-2",
            // jpackage derives Depends by ldd-ing the launcher only, so it never inspects the JRE's
            // own libraries. runtime/lib/libfontmanager.so links these directly, and without them the
            // app dies at startup with UnsatisfiedLinkError: libharfbuzz.so.0: cannot open shared
            // object file -- after a "successful" install.
            "libharfbuzz0b",
            "libmd0",
            "libpng16-16t64 | libpng16-16",
            "libstdc++6",
            "libx11-6",
            "libxau6",
            "libxcb1",
            "libxdmcp6",
            "libxext6",
            "libxi6",
            "libxrender1",
            "libxtst6",
            "xdg-utils",
            "zlib1g",
        ).joinToString(",")
    )

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.denticode.kt.MainKt"
        // Forwards Gradle's -Ddenti.logCitasData=true into the app JVM (Citas query logging).
        jvmArgs("-Ddenti.logCitasData=${System.getProperty("denti.logCitasData", "false")}")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            // jpackage --name: the launcher name and the .deb artifact prefix ("Denti-Code_1.0.0_amd64.deb").
            packageName = "Denti-Code"
            // Single source of truth for the version: project.version above, never hard-coded here.
            packageVersion = project.version.toString()
            description = "Denti-Code dental clinic management desktop application"
            vendor = "Denti-Code"

            linux {
                // Debian Package: field — must be lowercase (Debian policy), so "Denti-Code" != "denti-code".
                packageName = "denti-code"
                appCategory = "Office"
                // jpackage writes --linux-menu-group into the .desktop "Categories" key, and leaves it
                // as the literal "Unknown" when unset, which hides the launcher in some desktops.
                menuGroup = "Office"
                debMaintainer = defaultDebMaintainer
                // Pinned on purpose: some jpackage builds (e.g. Ubuntu's OpenJDK) default app-release to
                // "1" and others leave it unset, which silently renames the artifact from
                // "denti-code_1.0.0_amd64.deb" to "denti-code_1.0.0-1_amd64.deb". Pinning it keeps the
                // Debian version (upstream-revision) identical on every build machine.
                appRelease = "1"
                if (linuxIcon.isFile) iconFile.set(linuxIcon)
            }

            // Kept so Windows/macOS packaging stays available; icons are wired the same way.
            windows {
                if (windowsIcon.isFile) iconFile.set(windowsIcon)
            }

            macOS {
                if (macIcon.isFile) iconFile.set(macIcon)
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

// Release automation reads the version from here so the Gradle project stays the single source of truth.
tasks.register("printVersion") {
    group = "denti-code"
    description = "Prints the Gradle project version used as the installer version (no v prefix)."
    val projectVersion = project.version.toString()
    doLast { println(projectVersion) }
}

// The .deb is the primary release artifact on Linux, so that is the icon we nag about when missing.
tasks.matching { it.name == "packageDeb" || it.name == "createDistributable" }.configureEach {
    val isLinux = System.getProperty("os.name").orEmpty().startsWith("Linux")
    val iconInUse = if (isLinux) linuxIcon else windowsIcon
    doFirst {
        if (iconInUse.isFile) {
            logger.lifecycle("[denti-code] Application icon: ${iconInUse.invariantSeparatorsPath}")
        } else {
            logger.warn(
                "[denti-code] No application icon at ${iconInUse.invariantSeparatorsPath}: the Compose default icon " +
                    "will be packaged. See src/main/resources/icons/README.md.",
            )
        }
    }
}

// The bundled JRE must carry the modules the app actually uses. Compose's default runtime image ships
// only java.base, java.datatransfer, java.xml, java.prefs, java.desktop, java.logging and
// jdk.crypto.ec -- no java.sql -- so the *packaged* app dies on first launch with
// NoClassDefFoundError: java/sql/Connection from Exposed/SQLite, even though it runs fine from Gradle
// (that uses the full toolchain JDK). Exposed and sqlite-jdbc need java.sql, and jdk.unsupported is
// required for sun.misc.Unsafe. `./gradlew suggestRuntimeModules` derives the set from the app.
// The Compose plugin assigns the module list during its own configuration phase, so a plain
// configureEach block runs too early and gets overwritten. afterEvaluate lands after that.
afterEvaluate {
    tasks.withType<AbstractJLinkTask>().configureEach {
        modules.set(
            listOf(
                "java.base",
                "java.datatransfer",
                "java.desktop",
                "java.instrument",
                "java.logging",
                "java.prefs",
                "java.sql",
                "java.xml",
                "jdk.crypto.ec",
                "jdk.unsupported",
            ),
        )
    }
}

// Rewrites Depends in the jpackage output with defaultLinuxPackageDependencies so the .deb installs on
// Ubuntu releases other than the build host. Runs on packageDeb's output via build-installer.sh;
// jpackage itself cannot be told this through the Compose DSL (and its CLI option is unreachable
// because Compose emits the jpackage command with the mode first).
tasks.register("pinDebDependencies") {
    group = "denti-code"
    description = "Replaces the ldd-derived Depends of the built .deb with the portable list."
    val debDir = layout.buildDirectory.dir("compose/binaries/main/deb")
    val deps = defaultLinuxPackageDependencies
    // Rewrites packageDeb's own output directory, so the task must run after it.
    dependsOn("packageDeb")
    outputs.upToDateWhen { false } // always re-pin, so a stale control file can't survive
    doLast {
        val dir = debDir.get().asFile
        val debs = dir.listFiles { f -> f.name.endsWith(".deb") }.orEmpty()
        if (debs.isEmpty()) {
            throw GradleException("No .deb found in ${dir.invariantSeparatorsPath}. Run packageDeb first.")
        }
        for (deb in debs) {
            val staging = createTempDir(prefix = "debpin-")
            try {
                providers.exec {
                    commandLine("dpkg-deb", "-R", deb.absolutePath, staging.absolutePath)
                }.result.get().assertNormalExitValue()
                val control = File(staging, "DEBIAN/control")
                val original = control.readText()
                control.writeText(
                    Regex("(?m)^Depends:.*$")
                        .replaceFirst(original, "Depends: $deps")
                        .let { if (it.contains("Depends: $deps")) it else error("no Depends field") },
                )

                // jpackage's postinst runs `xdg-desktop-menu install` unguarded. That command exits
                // non-zero wherever there is no writable system menu directory (minimal images,
                // servers, containers, and any XDG_DATA_DIRS that lacks /usr/share), and because
                // dpkg runs maintainer scripts with `set -e` the whole install aborts, leaving the
                // package half-configured ("iF") even though every file unpacked fine. Menu
                // registration is cosmetic, so make it best-effort instead of install-breaking.
                val postinst = File(staging, "DEBIAN/postinst")
                if (postinst.isFile) {
                    val before = postinst.readText()
                    val guarded = before.replace(
                        Regex("""^(xdg-desktop-menu .*)$""", RegexOption.MULTILINE),
                        "$1 || true",
                    )
                    if (guarded != before) {
                        postinst.writeText(guarded)
                        postinst.setExecutable(true, false)
                        logger.lifecycle("[denti-code] Hardened postinst (menu registration is now best-effort)")
                    }
                }
                providers.exec {
                    commandLine(
                        "dpkg-deb", "--build", "--root-owner-group", "--uniform-compression",
                        staging.absolutePath, deb.absolutePath,
                    )
                }.result.get().assertNormalExitValue()
                logger.lifecycle("[denti-code] Pinned Depends on ${deb.name}")
            } finally {
                staging.deleteRecursively()
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
}

tasks.register("resetLocalDb") {
    group = "denti-code"
    description = "Deletes ~/.denti-code-kt/denti-clinic.db so the next run recreates schema and seed data."
    doLast {
        val f = file("${System.getProperty("user.home")}/.denti-code-kt/denti-clinic.db")
        when {
            f.exists() && f.delete() -> println("Removed ${f.absolutePath}")
            !f.exists() -> println("No database at ${f.absolutePath}")
            else -> println("Could not delete ${f.absolutePath}")
        }
    }
}


