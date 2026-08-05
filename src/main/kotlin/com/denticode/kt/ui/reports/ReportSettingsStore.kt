package com.denticode.kt.ui.reports

import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.util.Properties

/** Rango con nombre guardado por el usuario para el filtro de reportes. */
data class SavedReportRange(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

/** Configuración del filtro de reportes: último rango usado + presets con nombre. */
data class ReportSettings(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val presets: List<SavedReportRange> = emptyList(),
)

/**
 * Persistencia ligera del filtro de reportes en `~/.denti-code-kt/reports-settings.properties`
 * (100 % local/offline, mismo patrón que [com.denticode.kt.export.DocumentStore]).
 */
object ReportSettingsStore {
    val settingsFile: File = File(System.getProperty("user.home"), ".denti-code-kt/reports-settings.properties")

    /** Carga el último rango y los presets guardados; cae a valores por defecto si el archivo no existe o está dañado. */
    fun load(defaultStart: LocalDate, defaultEnd: LocalDate): ReportSettings {
        if (!settingsFile.isFile) return ReportSettings(defaultStart, defaultEnd)
        return runCatching {
            val p = Properties()
            Files.newInputStream(settingsFile.toPath()).use { p.load(it) }
            val start =
                p.getProperty("startDate")?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: defaultStart
            val end =
                p.getProperty("endDate")?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: defaultEnd
            val count = p.getProperty("presetCount")?.toIntOrNull() ?: 0
            val presets =
                (0 until count).mapNotNull { i ->
                    val name = p.getProperty("preset.$i.name") ?: return@mapNotNull null
                    val s =
                        p.getProperty("preset.$i.start")
                            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                            ?: return@mapNotNull null
                    val e =
                        p.getProperty("preset.$i.end")
                            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                            ?: return@mapNotNull null
                    SavedReportRange(name, s, e)
                }
            ReportSettings(start, end, presets)
        }.getOrDefault(ReportSettings(defaultStart, defaultEnd))
    }

    /** Guarda el rango actual y los presets; los fallos de E/S se ignoran (persistencia best-effort). */
    fun save(settings: ReportSettings) {
        runCatching {
            settingsFile.parentFile.mkdirs()
            val p = Properties()
            p.setProperty("startDate", settings.startDate.toString())
            p.setProperty("endDate", settings.endDate.toString())
            settings.presets.forEachIndexed { i, preset ->
                p.setProperty("preset.$i.name", preset.name)
                p.setProperty("preset.$i.start", preset.startDate.toString())
                p.setProperty("preset.$i.end", preset.endDate.toString())
            }
            p.setProperty("presetCount", settings.presets.size.toString())
            Files.newOutputStream(settingsFile.toPath()).use { p.store(it, "Denti-Code report settings") }
        }
    }
}
