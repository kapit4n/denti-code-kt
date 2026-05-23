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
}

compose.desktop {
    application {
        mainClass = "com.denticode.kt.MainKt"
        // Forwards Gradle's -Ddenti.logCitasData=true into the app JVM (Citas query logging).
        jvmArgs("-Ddenti.logCitasData=${System.getProperty("denti.logCitasData", "false")}")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Denti-Code KT"
            description = "Denti-Code clinic desktop UI (Kotlin + Compose), models aligned with denti-code-desktop"
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
