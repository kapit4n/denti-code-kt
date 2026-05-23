package com.denticode.kt.ui.appointments

import androidx.compose.ui.graphics.Color
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.Patient
import com.denticode.kt.ui.parseAppointmentScheduledAt
import com.denticode.kt.ui.formatEpochMs
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val timeFmt12: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("es", "ES"))
private val dateShortFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))
private val timelineDayHeaderFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", Locale("es", "ES"))

/** Vista de lista central: todas las citas, día, semana o mes. */
enum class AppointmentViewMode {
    ALL,
    DAY,
    WEEK,
    MONTH,
}

fun formatTimelineDayHeader(date: LocalDate): String {
    val raw = date.format(timelineDayHeaderFmt)
    return raw.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(Locale("es", "ES")) else ch.toString()
    }
}

/** Encabezado de día en lista (Todas / Semana / Mes): «Hoy · Viernes 15 may» o fecha completa. */
fun formatTimelineDaySectionLabel(date: LocalDate): String {
    val today = LocalDate.now()
    val header = formatTimelineDayHeader(date)
    return when (date) {
        today -> "Hoy · $header"
        today.minusDays(1) -> "Ayer · $header"
        today.plusDays(1) -> "Mañana · $header"
        else -> header
    }
}

/** Fila de lista: separador de día o cita. */
sealed interface TimelineListEntry {
    data class DayHeader(
        val date: LocalDate,
        val label: String,
        /** Unique within the list (same calendar day can appear twice if mocks are appended out of order). */
        val headerIndex: Int,
    ) : TimelineListEntry

    data class Appointment(val model: AppointmentUiModel) : TimelineListEntry
}

/**
 * Inserta encabezados cuando cambia la fecha local de la cita.
 * Ordena antes de agrupar para evitar cabeceras duplicadas (p. ej. citas reales + mock en «Todas»).
 */
fun buildTimelineEntriesWithDayHeaders(
    appointments: List<AppointmentUiModel>,
    insertDayHeaders: Boolean,
    newestFirst: Boolean = true,
): List<TimelineListEntry> {
    if (!insertDayHeaders || appointments.isEmpty()) {
        return appointments.map { TimelineListEntry.Appointment(it) }
    }
    val sorted =
        if (newestFirst) {
            appointments.sortedByDescending { it.scheduledAt }
        } else {
            appointments.sortedBy { it.scheduledAt }
        }
    val out = ArrayList<TimelineListEntry>(sorted.size + 12)
    var lastDate: LocalDate? = null
    var headerIndex = 0
    for (appt in sorted) {
        val day = appt.scheduledAt.toLocalDate()
        if (day != lastDate) {
            out.add(
                TimelineListEntry.DayHeader(
                    date = day,
                    label = formatTimelineDaySectionLabel(day),
                    headerIndex = headerIndex++,
                ),
            )
            lastDate = day
        }
        out.add(TimelineListEntry.Appointment(appt))
    }
    return out
}

object AppointmentPremiumPalette {
    val primary = Color(0xFF6C63FF)
    val background = Color(0xFFF5F7FB)
    val card = Color(0xFFFFFFFF)
    val success = Color(0xFF34C759)
    val warning = Color(0xFFFFB020)
    val error = Color(0xFFFF5A5F)
    val info = Color(0xFF4DA3FF)
    val textPrimary = Color(0xFF1F2937)
    val textSecondary = Color(0xFF6B7280)
}

data class AppointmentUiModel(
    val id: Int,
    val patientId: Int,
    val primaryDoctorId: Int,
    val patientName: String,
    val patientPhone: String?,
    val doctorName: String,
    val treatmentName: String,
    val scheduledAt: LocalDateTime,
    val durationMinutes: Int,
    val notes: String?,
    val purpose: String?,
    val status: AppointmentStatus,
    val displayStatusLabel: String,
    val statusAccentColor: Color,
    val createdDisplay: String,
) {
    val timeLabel: String get() = scheduledAt.format(timeFmt12)
    val dateLabel: String get() = scheduledAt.format(dateFmt)
    val dateShortLabel: String get() = scheduledAt.format(dateShortFmt)
}

fun AppointmentStatus.toTimelineDisplay(): String =
    when (this) {
        AppointmentStatus.CONFIRMED -> "Confirmada"
        AppointmentStatus.IN_PROGRESS -> "En proceso"
        AppointmentStatus.COMPLETED -> "Completada"
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW,
        -> "Cancelada"
        AppointmentStatus.SCHEDULED,
        AppointmentStatus.RESCHEDULED,
        -> "Pendiente"
    }

fun AppointmentStatus.toAccentColor(): Color =
    when (this) {
        AppointmentStatus.CONFIRMED -> AppointmentPremiumPalette.success
        AppointmentStatus.IN_PROGRESS -> AppointmentPremiumPalette.info
        AppointmentStatus.COMPLETED -> Color(0xFF6366F1)
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW,
        -> AppointmentPremiumPalette.error
        AppointmentStatus.SCHEDULED,
        AppointmentStatus.RESCHEDULED,
        -> AppointmentPremiumPalette.warning
    }

/**
 * Filas ficticias (ids negativos) para previsualizar la lista en vista «Todas».
 * Se concatenan después de las citas reales.
 */
fun citasListMockPreviewModels(): List<AppointmentUiModel> {
    val now = LocalDateTime.now().withSecond(0).withNano(0)
    fun mock(
        id: Int,
        patient: String,
        doctor: String,
        treatment: String,
        at: LocalDateTime,
        status: AppointmentStatus,
    ): AppointmentUiModel =
        AppointmentUiModel(
            id = id,
            patientId = -id,
            primaryDoctorId = 1,
            patientName = patient,
            patientPhone = "+34 600 000 000",
            doctorName = doctor,
            treatmentName = treatment,
            scheduledAt = at,
            durationMinutes = 45,
            notes = "Ejemplo (mock)",
            purpose = "Vista previa UI",
            status = status,
            displayStatusLabel = status.toTimelineDisplay(),
            statusAccentColor = status.toAccentColor(),
            createdDisplay = "—",
        )
    return listOf(
        mock(-901, "Laura Martín", "Dr. Elena Feria", "Limpieza y profilaxis", now.plusDays(1).withHour(9).withMinute(0), AppointmentStatus.CONFIRMED),
        mock(-902, "Javier Ortega", "Dr. Carlos Mena", "Revisión ortodoncia", now.plusDays(2).withHour(11).withMinute(30), AppointmentStatus.SCHEDULED),
        mock(-903, "Marta Sánchez", "Dra. Ana Ruiz", "Endodoncia molar", now.plusDays(3).withHour(16).withMinute(0), AppointmentStatus.IN_PROGRESS),
        mock(-904, "Pablo Gil", "Dr. Luis Vidal", "Extracción simple", now.plusDays(5).withHour(10).withMinute(15), AppointmentStatus.COMPLETED),
        mock(-905, "Elena Ríos", "Dra. Carmen Ibarra", "Primera visita implante", now.plusDays(7).withHour(12).withMinute(45), AppointmentStatus.RESCHEDULED),
    )
}

fun AppointmentRow.toUiModel(patientPhone: String?, patientCreatedEpoch: Long?): AppointmentUiModel {
    val ldt = parseAppointmentScheduledAt(scheduledAt)
    val dur = estimatedDurationMinutes?.coerceAtLeast(5) ?: 30
    val treatment = procedureTypeName?.takeIf { it.isNotBlank() } ?: purpose?.takeIf { it.isNotBlank() } ?: "Consulta"
    val registroPaciente =
        patientCreatedEpoch?.let { formatEpochMs(it) } ?: "—"
    return AppointmentUiModel(
        id = id,
        patientId = patientId,
        primaryDoctorId = primaryDoctorId,
        patientName = patientName,
        patientPhone = patientPhone?.takeIf { it.isNotBlank() },
        doctorName = doctorName,
        treatmentName = treatment,
        scheduledAt = ldt,
        durationMinutes = dur,
        notes = notes,
        purpose = purpose,
        status = status,
        displayStatusLabel = status.toTimelineDisplay(),
        statusAccentColor = status.toAccentColor(),
        createdDisplay = registroPaciente,
    )
}

fun weekRangeFor(date: LocalDate): Pair<LocalDate, LocalDate> {
    val mon = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sun = mon.plusDays(6)
    return mon to sun
}

fun formatMonthTitle(ym: YearMonth): String {
    val m = ym.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    return "${m.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }} ${ym.year}"
}

fun formatRangeLabel(start: LocalDate, end: LocalDate): String {
    val a = start.format(DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES")))
    val b = end.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))
    return "$a – $b"
}

fun patientsById(patients: List<Patient>): Map<Int, Patient> = patients.associateBy { it.id }

data class AppointmentsUiState(
    val appointments: List<AppointmentUiModel>,
    val selectedAppointment: AppointmentUiModel?,
    val selectedDate: LocalDate,
    val selectedDoctor: String?,
    val selectedStatus: AppointmentStatus?,
    val searchQuery: String,
)
