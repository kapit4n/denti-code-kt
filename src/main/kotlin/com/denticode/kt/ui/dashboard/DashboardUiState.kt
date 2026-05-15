package com.denticode.kt.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus

@Immutable
data class TodayAppointmentUi(
    val id: Int,
    val timeLabel: String,
    val patientName: String,
    val treatmentLabel: String,
    val doctorName: String,
    val displayStatus: AppointmentDisplayStatus,
)

enum class AppointmentDisplayStatus(val labelEs: String, val color: Color) {
    CONFIRMED("Confirmada", Color(0xFF34C759)),
    IN_PROGRESS("En proceso", Color(0xFF4DA3FF)),
    PENDING("Pendiente", Color(0xFFFFB020)),
    CANCELLED("Cancelada", Color(0xFFFF5A5F)),
}

@Immutable
data class ActivityFeedItem(
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val accent: Color,
)

@Immutable
data class DashboardAlertUi(
    val title: String,
    val subtitle: String,
    val tint: Color,
)

@Immutable
data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color,
)

fun AppointmentRow.toTodayUi(): TodayAppointmentUi {
    val display =
        when (status) {
            AppointmentStatus.CONFIRMED -> AppointmentDisplayStatus.CONFIRMED
            AppointmentStatus.IN_PROGRESS -> AppointmentDisplayStatus.IN_PROGRESS
            AppointmentStatus.CANCELLED,
            AppointmentStatus.NO_SHOW,
            -> AppointmentDisplayStatus.CANCELLED
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.RESCHEDULED,
            -> AppointmentDisplayStatus.PENDING
            AppointmentStatus.COMPLETED -> AppointmentDisplayStatus.CONFIRMED
        }
    val time =
        scheduledAt
            .trim()
            .takeIf { it.length >= 16 }
            ?.substring(11, 16)
            ?: scheduledAt.take(8)
    return TodayAppointmentUi(
        id = id,
        timeLabel = time,
        patientName = patientName,
        treatmentLabel = procedureTypeName ?: purpose ?: "—",
        doctorName = doctorName,
        displayStatus = display,
    )
}
