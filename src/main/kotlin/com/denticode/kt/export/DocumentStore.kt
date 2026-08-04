package com.denticode.kt.export

import java.io.File
import java.net.URLConnection
import java.nio.file.Files

/**
 * Almacenamiento físico de documentos de pacientes.
 *
 * Los archivos se copian a `~/.denti-code-kt/documents/<patientId>/` con un prefijo
 * de timestamp para evitar colisiones y para que la app siga siendo 100 % local/offline.
 * Solo se borran archivos que estén dentro de este directorio (nunca rutas arbitrarias).
 */
object DocumentStore {

    val baseDir: File = File(System.getProperty("user.home"), ".denti-code-kt/documents")

    /** Directorio por paciente. */
    fun patientDir(patientId: Int): File = File(baseDir, patientId.toString())

    /**
     * Copia [source] dentro del directorio del paciente con un nombre único
     * (`<epochMs>_<nombre original>`) y devuelve el archivo destino ya escrito.
     */
    fun save(patientId: Int, source: File): File {
        require(source.isFile) { "El archivo seleccionado no existe." }
        val dir = patientDir(patientId)
        dir.mkdirs()
        val target = File(dir, "${System.currentTimeMillis()}_${sanitizeName(source.name)}")
        source.copyTo(target, overwrite = false)
        return target
    }

    /**
     * Elimina el archivo físico si existe y está dentro de [baseDir].
     * Devuelve true si se eliminó o ya no existía, false si la ruta queda fuera del almacén.
     */
    fun delete(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return true
        val file = File(filePath)
        val inside = file.canonicalPath.startsWith(baseDir.canonicalPath)
        if (!inside) return false
        return runCatching { file.delete() }.getOrDefault(false) || !file.exists()
    }

    /** Intenta resolver el tipo MIME desde el archivo; fallback por extensión. */
    fun guessMimeType(file: File): String? {
        val probed = runCatching { Files.probeContentType(file.toPath()) }.getOrNull()
        if (!probed.isNullOrBlank()) return probed
        return guessMimeTypeByName(file.name)
    }

    /** Tipo MIME por extensión (fallback ligero cuando probeContentType falla). */
    fun guessMimeTypeByName(fileName: String): String? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "html", "htm" -> "text/html"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "zip" -> "application/zip"
            "mp4" -> "video/mp4"
            "dcm" -> "application/dicom"
            else -> URLConnection.guessContentTypeFromName(fileName)
        }
    }

    /** Extensión legible para la tarjeta (PDF, JPG, PNG…). */
    fun extensionLabel(fileName: String?): String =
        fileName
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
            ?.uppercase() ?: "—"

    private fun sanitizeName(name: String): String =
        name.replace(Regex("[^\\p{L}\\p{N}._-]"), "_").takeIf { it.isNotBlank() } ?: "documento"
}
