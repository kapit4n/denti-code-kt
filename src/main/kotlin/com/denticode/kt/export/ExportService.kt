package com.denticode.kt.export

import com.denticode.kt.data.seeders.DemoDataConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * File export utilities: clinic identity, native save dialog and file writing.
 * The app has no third-party export libraries, so exports are plain UTF-8 text/HTML/CSV
 * produced with the JDK stdlib.
 */
object ExportService {

    /** Identidad de la clínica para encabezados de recibos y fichas. */
    val clinicName: String = DemoDataConfig.DEFAULT.clinicName
    val clinicCity: String = DemoDataConfig.DEFAULT.clinicCity
    val clinicCountry: String = DemoDataConfig.DEFAULT.clinicCountry
    val currencySymbol: String = DemoDataConfig.DEFAULT.currencySymbol

    /**
     * Abre el diálogo nativo de guardar (en el EDT) y devuelve el archivo elegido,
     * o null si el usuario cancela.
     */
    suspend fun pickSaveFile(defaultName: String): File? =
        withContext(Dispatchers.Swing) {
            val dialog = FileDialog(null as Frame?, "Guardar archivo", FileDialog.SAVE)
            dialog.file = defaultName
            dialog.isVisible = true
            val dir = dialog.directory
            val name = dialog.file
            if (dir.isNullOrBlank() || name.isNullOrBlank()) null else File(dir, name)
        }

    /** Escribe contenido UTF-8 en el archivo (creando el directorio padre si falta). */
    fun writeTextFile(file: File, content: String): File {
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    /** Abre el archivo con el visor del sistema (si hay uno asociado). */
    fun openFile(file: File) {
        runCatching { Desktop.getDesktop().open(file) }
    }

    fun money(value: Double): String = "Bs. %.2f".format(value)
}
