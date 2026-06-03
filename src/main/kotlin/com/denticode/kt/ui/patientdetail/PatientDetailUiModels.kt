package com.denticode.kt.ui.patientdetail

import androidx.compose.ui.graphics.Color
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.appointments.AppointmentPremiumPalette
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.parseAppointmentScheduledAt
import com.denticode.kt.ui.patients.formatBirthDateLabel
import com.denticode.kt.ui.patients.patientAgeFromDob
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PatientDetailUiModel(
    val patient: Patient,
    val fullName: String,
    val birthDateLabel: String,
    val ageYears: Int?,
    val phone: String,
    val email: String?,
    val isActive: Boolean,
    val medicalSummary: String?,
)

data class PatientDetailKpis(
    val totalAppointments: Int,
    val completedAppointments: Int,
    val nextAppointmentLabel: String,
    val pendingBalance: Double,
)

enum class PatientPaymentDisplayStatus {
    PAID,
    PENDING,
    OVERDUE,
    ;

    val labelEs: String
        get() =
            when (this) {
                PAID -> "Pagado"
                PENDING -> "Pendiente"
                OVERDUE -> "Vencido"
            }
}

data class PatientDetailAppointmentUi(
    val id: Int,
    val dateLabel: String,
    val timeLabel: String,
    val durationLabel: String,
    val doctorName: String,
    val treatmentName: String,
    val notes: String?,
    val status: AppointmentStatus,
    val statusLabel: String,
    val statusColor: Color,
    val scheduledAt: LocalDateTime,
)

data class PatientDetailPaymentUi(
    val id: Int,
    val amount: Double,
    val amountLabel: String,
    val dateLabel: String,
    val methodLabel: String,
    val treatmentLabel: String?,
    val note: String?,
    val status: PatientPaymentDisplayStatus,
)

data class PaymentSummaryUiModel(
    val totalPaid: Double,
    val pending: Double,
    val total: Double,
)

data class PatientDetailUiState(
    val patient: PatientDetailUiModel,
    val kpis: PatientDetailKpis,
    val appointments: List<PatientDetailAppointmentUi>,
    val treatments: List<PatientTreatmentRow>,
    val payments: List<PatientDetailPaymentUi>,
    val selectedAppointmentFilter: AppointmentStatus?,
    val paymentSummary: PaymentSummaryUiModel,
)

fun computePendingBalanceFromTreatments(
    treatments: List<PatientTreatmentRow>,
    payments: List<PatientLedgerPayment>,
): Double {
    val billable = treatments.filter { it.status != TreatmentStatus.CANCELLED }.sumOf { it.totalPrice }
    val paid = payments.sumOf { it.amount }
    return (billable - paid).coerceAtLeast(0.0)
}

private val detailDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))
private val detailTimeFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))

fun Patient.toDetailUiModel(appointments: List<AppointmentRow>): PatientDetailUiModel {
    val now = LocalDateTime.now()
    val active =
        appointments.any { a ->
            val dt = parseAppointmentScheduledAt(a.scheduledAt)
            dt.isAfter(now) &&
                a.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
        }
    val age = patientAgeFromDob(dateOfBirth)
    return PatientDetailUiModel(
        patient = this,
        fullName = fullName,
        birthDateLabel = formatBirthDateLabel(dateOfBirth),
        ageYears = age,
        phone = contactPhone,
        email = email,
        isActive = active,
        medicalSummary = medicalHistorySummary?.takeIf { it.isNotBlank() },
    )
}

fun appointmentStatusAccent(status: AppointmentStatus): Color =
    when (status) {
        AppointmentStatus.COMPLETED -> AppointmentPremiumPalette.success
        AppointmentStatus.CONFIRMED -> AppointmentPremiumPalette.info
        AppointmentStatus.IN_PROGRESS -> AppointmentPremiumPalette.info
        AppointmentStatus.SCHEDULED,
        AppointmentStatus.RESCHEDULED,
        -> AppointmentPremiumPalette.primary
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW,
        -> AppointmentPremiumPalette.error
    }

fun appointmentStatusDetailLabel(status: AppointmentStatus): String =
    when (status) {
        AppointmentStatus.CONFIRMED -> "Confirmada"
        AppointmentStatus.SCHEDULED -> "Programada"
        AppointmentStatus.RESCHEDULED -> "Reprogramada"
        AppointmentStatus.COMPLETED -> "Completada"
        AppointmentStatus.IN_PROGRESS -> "En curso"
        AppointmentStatus.CANCELLED -> "Cancelada"
        AppointmentStatus.NO_SHOW -> "No asistió"
    }

fun AppointmentRow.toDetailUi(): PatientDetailAppointmentUi {
    val ldt = parseAppointmentScheduledAt(scheduledAt)
    val dur = estimatedDurationMinutes?.coerceAtLeast(5) ?: 30
    val treatment =
        procedureTypeName?.takeIf { it.isNotBlank() }
            ?: purpose?.takeIf { it.isNotBlank() }
            ?: "Consulta"
    return PatientDetailAppointmentUi(
        id = id,
        dateLabel = ldt.format(detailDateFmt),
        timeLabel = ldt.format(detailTimeFmt),
        durationLabel = "$dur min",
        doctorName = doctorName,
        treatmentName = treatment,
        notes = notes?.takeIf { it.isNotBlank() },
        status = status,
        statusLabel = appointmentStatusDetailLabel(status),
        statusColor = appointmentStatusAccent(status),
        scheduledAt = ldt,
    )
}

fun PatientLedgerPayment.toDetailUi(): PatientDetailPaymentUi {
    val ldt =
        try {
            parseAppointmentScheduledAt(paidAt)
        } catch (_: Exception) {
            LocalDateTime.now()
        }
    return PatientDetailPaymentUi(
        id = id,
        amount = amount,
        amountLabel = formatMoney(amount),
        dateLabel = ldt.format(detailDateFmt) + " · " + ldt.format(detailTimeFmt),
        methodLabel = method?.displayLabel ?: "Sin especificar",
        treatmentLabel = procedureTypeName?.takeIf { it.isNotBlank() },
        note = note?.takeIf { it.isNotBlank() },
        status = PatientPaymentDisplayStatus.PAID,
    )
}

fun buildPatientDetailKpis(
    appointments: List<AppointmentRow>,
    payments: List<PatientLedgerPayment>,
    treatments: List<PatientTreatmentRow>,
): PatientDetailKpis {
    val now = LocalDateTime.now()
    val completed =
        appointments.count {
            it.status == AppointmentStatus.COMPLETED
        }
    val next =
        appointments
            .map { a -> a to parseAppointmentScheduledAt(a.scheduledAt) }
            .filter { (a, dt) ->
                dt.isAfter(now) &&
                    a.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
            }
            .minByOrNull { (_, dt) -> dt }
    val nextLabel =
        next?.let { (a, dt) ->
            "${dt.format(detailDateFmt)} · ${dt.format(detailTimeFmt)}"
        } ?: "Sin programar"
    val paid = payments.sumOf { it.amount }
    val pending = computePendingBalanceFromTreatments(treatments, payments)
    return PatientDetailKpis(
        totalAppointments = appointments.size,
        completedAppointments = completed,
        nextAppointmentLabel = nextLabel,
        pendingBalance = pending.coerceAtLeast(0.0),
    )
}

fun buildPaymentSummary(
    payments: List<PatientLedgerPayment>,
    pendingBalance: Double,
): PaymentSummaryUiModel {
    val paid = payments.sumOf { it.amount }
    val pending = pendingBalance.coerceAtLeast(0.0)
    return PaymentSummaryUiModel(
        totalPaid = paid,
        pending = pending,
        total = paid + pending,
    )
}

fun buildPatientDetailUiState(
    patient: Patient,
    appointments: List<AppointmentRow>,
    treatments: List<PatientTreatmentRow>,
    payments: List<PatientLedgerPayment>,
    filter: AppointmentStatus? = null,
): PatientDetailUiState {
    val kpis = buildPatientDetailKpis(appointments, payments, treatments)
    val apptUi = appointments.map { it.toDetailUi() }.sortedByDescending { it.scheduledAt }
    val filtered =
        if (filter == null) {
            apptUi
        } else {
            apptUi.filter { it.status == filter }
        }
    return PatientDetailUiState(
        patient = patient.toDetailUiModel(appointments),
        kpis = kpis,
        appointments = filtered,
        treatments = treatments,
        payments = payments.map { it.toDetailUi() },
        selectedAppointmentFilter = filter,
        paymentSummary = buildPaymentSummary(payments, kpis.pendingBalance),
    )
}
