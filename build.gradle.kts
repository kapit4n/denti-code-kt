import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
