package com.denticode.kt.ui.payments

import com.denticode.kt.data.PaymentDisplayStatus
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.data.PaymentRow
import com.denticode.kt.export.ReceiptData
import com.denticode.kt.ui.parseAppointmentScheduledAt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PaymentUiModel(
    val id: Int,
    val patientId: Int,
    val patientName: String,
    val detailLabel: String,
    val methodLabel: String,
    val amount: Double,
    val amountLabel: String,
    val status: PaymentDisplayStatus,
    val dateLabel: String,
    val timeLabel: String,
    val paidAtLocalDate: LocalDate?,
    val method: PaymentMethod?,
    val row: PaymentRow,
)

data class PaymentsUiState(
    val payments: List<PaymentUiModel>,
    val searchQuery: String,
    val selectedMethod: PaymentMethod?,
    val selectedStatus: PaymentDisplayStatus?,
    val filterDate: LocalDate?,
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalCollected: Double,
    val completedCount: Int,
    val pendingCount: Int,
    val averageAmount: Double,
    val pendingTotal: Double,
)

private val paymentDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

private val paymentTimeFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))

fun derivePaymentStatus(row: PaymentRow): PaymentDisplayStatus {
    val note = row.note?.lowercase().orEmpty()
    return when {
        note.contains("fallid") || note.contains("failed") -> PaymentDisplayStatus.FAILED
        row.method == null || note.contains("pendient") -> PaymentDisplayStatus.PENDING
        else -> PaymentDisplayStatus.PAID
    }
}

fun formatPaymentMethodLabel(row: PaymentRow): String {
    val method = row.method ?: return "Sin método"
    if (method == PaymentMethod.CARD) {
        val mask = row.note?.takeIf { it.contains("****") } ?: "Tarjeta"
        return mask
    }
    return method.displayLabel
}

fun PaymentRow.toUiModel(): PaymentUiModel {
    val dt = runCatching { parseAppointmentScheduledAt(paidAt) }.getOrNull()
    val date = dt?.toLocalDate()
    val dateLabel =
        date?.let {
            val raw = it.format(paymentDateFmt)
            raw.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale("es", "ES")) else ch.toString()
            }
        } ?: paidAt.take(10)
    val timeLabel = dt?.format(paymentTimeFmt) ?: "—"
    val amountLabel = "Bs %.2f".format(amount)
    return PaymentUiModel(
        id = id,
        patientId = patientId,
        patientName = patientName,
        detailLabel = procedureTypeName?.takeIf { it.isNotBlank() } ?: note?.takeIf { it.isNotBlank() } ?: "—",
        methodLabel = formatPaymentMethodLabel(this),
        amount = amount,
        amountLabel = amountLabel,
        status = derivePaymentStatus(this),
        dateLabel = dateLabel,
        timeLabel = timeLabel,
        paidAtLocalDate = date,
        method = method,
        row = this,
    )
}

fun PaymentUiModel.toReceiptData(): ReceiptData =
    ReceiptData(
        receiptNumber = "REC-%06d".format(id),
        patientId = patientId,
        patientName = patientName,
        detailLabel = detailLabel,
        methodLabel = methodLabel,
        amount = amount,
        dateTimeLabel = "$dateLabel · $timeLabel",
        statusLabel = status.labelEs,
        note = row.note,
    )

fun buildPaymentsUiState(
    rows: List<PaymentRow>,
    searchQuery: String,
    selectedMethod: PaymentMethod?,
    selectedStatus: PaymentDisplayStatus?,
    filterDate: LocalDate?,
    currentPage: Int,
    pageSize: Int,
): PaymentsUiState {
    val allUi = rows.map { it.toUiModel() }
    val q = searchQuery.trim().lowercase()
    val filtered =
        allUi.filter { p ->
            val matchesSearch =
                q.isEmpty() ||
                    p.patientName.lowercase().contains(q) ||
                    p.detailLabel.lowercase().contains(q) ||
                    p.methodLabel.lowercase().contains(q) ||
                    p.patientId.toString().contains(q)
            val matchesMethod = selectedMethod == null || p.method == selectedMethod
            val matchesStatus = selectedStatus == null || p.status == selectedStatus
            val matchesDate = filterDate == null || p.paidAtLocalDate == filterDate
            matchesSearch && matchesMethod && matchesStatus && matchesDate
        }
    val paidRows = filtered.filter { it.status == PaymentDisplayStatus.PAID }
    val pendingRows = filtered.filter { it.status == PaymentDisplayStatus.PENDING }
    val totalCollected = paidRows.sumOf { it.amount }
    val completedCount = paidRows.size
    val pendingCount = pendingRows.size
    val pendingTotal = pendingRows.sumOf { it.amount }
    val averageAmount = if (completedCount > 0) totalCollected / completedCount else 0.0
    val maxPage =
        if (filtered.isEmpty()) {
            1
        } else {
            (filtered.size + pageSize - 1) / pageSize
        }
    val page = currentPage.coerceIn(1, maxPage)
    return PaymentsUiState(
        payments = filtered,
        searchQuery = searchQuery,
        selectedMethod = selectedMethod,
        selectedStatus = selectedStatus,
        filterDate = filterDate,
        currentPage = page,
        pageSize = pageSize,
        totalItems = filtered.size,
        totalCollected = totalCollected,
        completedCount = completedCount,
        pendingCount = pendingCount,
        averageAmount = averageAmount,
        pendingTotal = pendingTotal,
    )
}

fun paginatedPayments(state: PaymentsUiState): List<PaymentUiModel> {
    if (state.payments.isEmpty()) return emptyList()
    val start = (state.currentPage - 1) * state.pageSize
    if (start >= state.payments.size) return emptyList()
    val end = (start + state.pageSize).coerceAtMost(state.payments.size)
    return state.payments.subList(start, end)
}

fun totalPaymentPages(state: PaymentsUiState): Int =
    if (state.totalItems == 0) {
        1
    } else {
        (state.totalItems + state.pageSize - 1) / state.pageSize
    }

fun formatPaymentsFilterDateLabel(date: LocalDate?): String {
    if (date == null) return "Todas las fechas"
    val today = LocalDate.now()
    val prefix =
        when (date) {
            today -> "Hoy"
            today.minusDays(1) -> "Ayer"
            else -> null
        }
    val formatted =
        date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))
            .replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale("es", "ES")) else ch.toString()
            }
    return prefix?.let { "$it, $formatted" } ?: formatted
}
