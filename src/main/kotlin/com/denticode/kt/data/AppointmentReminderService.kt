package com.denticode.kt.data

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.LocalDate
import java.time.LocalDateTime
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/** Generación de recordatorios WhatsApp y acciones de escritorio. */
class AppointmentReminderService(
    private val audit: AppointmentAuditService,
) {
    private val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    private val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    private val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun buildMessage(snapshot: AppointmentDetailSnapshot): String {
        val appt = snapshot.appointment
        val ldt = parseScheduledAtLocal(appt.scheduledAt)
        val firstName = appt.patientName.trim().split(Regex("\\s+")).firstOrNull() ?: appt.patientName
        return buildString {
            appendLine("Hola $firstName,")
            appendLine()
            appendLine("Le recordamos su cita en Denti-Code.")
            appendLine()
            appendLine("Fecha: ${ldt.format(dateFmt)}")
            appendLine("Hora: ${ldt.format(timeFmt)}")
            appendLine("Doctor: ${appt.doctorName}")
            appendLine()
            append("Por favor confirme su asistencia.")
        }
    }

    fun buildWhatsAppUrl(phone: String?, message: String): String? {
        val digits = normalizePhone(phone) ?: return null
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
        return "https://wa.me/$digits?text=$encoded"
    }

    fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }

    fun openUrl(url: String) {
        if (!Desktop.isDesktopSupported()) return
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
        }
    }

    fun logReminderSent(appointmentId: Int) {
        audit.log(appointmentId, "Reminder sent", "Recordatorio WhatsApp preparado")
    }

    private fun normalizePhone(phone: String?): String? {
        val digits = phone?.filter { it.isDigit() }?.takeIf { it.length >= 9 } ?: return null
        return if (digits.startsWith("34") && digits.length >= 11) {
            digits
        } else if (digits.length == 9) {
            "34$digits"
        } else {
            digits
        }
    }

    private fun parseScheduledAtLocal(value: String): LocalDateTime {
        val t = value.trim()
        return try {
            LocalDateTime.parse(t, isoFmt)
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
            } catch (_: DateTimeParseException) {
                LocalDateTime.now()
            }
        }
    }
}
