package com.denticode.kt.ui.doctordetail

import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.DoctorDirectoryRow
import com.denticode.kt.data.DoctorListStatus
import com.denticode.kt.ui.parseAppointmentScheduledAt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AppointmentPreviewFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale("es", "ES"))

data class DoctorAppointmentUi(
    val id: Int,
    val patientName: String,
    val treatmentName: String,
    val scheduledLabel: String,
    val status: AppointmentStatus,
)

data class DoctorDetailUiState(
    val doctor: Doctor,
    val status: DoctorListStatus,
    val todayAppointmentsCount: Int,
    val totalAppointmentsCount: Int,
    val yearsExperience: Int,
    val upcomingAppointments: List<DoctorAppointmentUi>,
)

fun buildDoctorDetailUiState(
    directoryRow: DoctorDirectoryRow,
    appointments: List<AppointmentRow>,
): DoctorDetailUiState {
    val now = java.time.LocalDateTime.now()
    val upcoming =
        appointments
            .filter { appt ->
                appt.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) &&
                    !parseAppointmentScheduledAt(appt.scheduledAt).isBefore(now.minusDays(1))
            }
            .sortedBy { parseAppointmentScheduledAt(it.scheduledAt) }
            .take(12)
            .map { appt ->
                DoctorAppointmentUi(
                    id = appt.id,
                    patientName = appt.patientName,
                    treatmentName = appt.procedureTypeName ?: appt.purpose ?: "Consulta",
                    scheduledLabel = parseAppointmentScheduledAt(appt.scheduledAt).format(AppointmentPreviewFormatter),
                    status = appt.status,
                )
            }
    return DoctorDetailUiState(
        doctor = directoryRow.doctor,
        status = directoryRow.status,
        todayAppointmentsCount = directoryRow.todayAppointmentsCount,
        totalAppointmentsCount = directoryRow.totalAppointmentsCount,
        yearsExperience = directoryRow.yearsExperience,
        upcomingAppointments = upcoming,
    )
}
