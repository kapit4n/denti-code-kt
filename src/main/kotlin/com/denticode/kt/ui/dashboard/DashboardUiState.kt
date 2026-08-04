package com.denticode.kt.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.InventoryLineRow
import com.denticode.kt.data.StockStatus
import com.denticode.kt.data.resolveInventoryStockStatus
import com.denticode.kt.ui.inventory.categoryLabelEs

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

/** Línea de inventario bajo/agotado para la tarjeta de alertas de stock. */
@Immutable
data class StockAlertUi(
    val productName: String,
    val productCode: String,
    val consultoryLabel: String,
    val categoryLabel: String,
    val quantity: Int,
    val minQuantity: Int,
    val status: StockStatus,
)

fun InventoryLineRow.toStockAlertUi(): StockAlertUi? {
    val status = resolveInventoryStockStatus(quantity, minQuantity)
    if (status == StockStatus.OPTIMAL) return null
    val consultory =
        buildString {
            append(consultoryName)
            consultoryShortCode?.let { append(" ($it)") }
        }
    return StockAlertUi(
        productName = facilityDisplayName,
        productCode = facilityCode,
        consultoryLabel = consultory,
        categoryLabel = categoryLabelEs(categoryKey),
        quantity = quantity,
        minQuantity = minQuantity,
        status = status,
    )
}

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
