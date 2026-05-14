package com.denticode.kt.data

import java.time.LocalDate

/** Mirrors `com.denticode.desktop.domain.model.AppointmentStatus`. */
enum class AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED,
    ;

    companion object {
        fun fromDb(value: String): AppointmentStatus =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: SCHEDULED
    }

    val displayLabel: String
        get() =
            when (this) {
                SCHEDULED -> "Programada"
                CONFIRMED -> "Confirmada"
                IN_PROGRESS -> "En curso"
                COMPLETED -> "Completada"
                CANCELLED -> "Cancelada"
                NO_SHOW -> "No asistió"
                RESCHEDULED -> "Reprogramada"
            }
}

/**
 * Estados que la clínica puede asignar al crear o editar una visita en la aplicación.
 * (Subconjunto explícito del enum; amplía o reduce aquí según reglas de negocio.)
 */
fun visitStatusOptions(): List<AppointmentStatus> =
    listOf(
        AppointmentStatus.SCHEDULED,
        AppointmentStatus.CONFIRMED,
        AppointmentStatus.IN_PROGRESS,
        AppointmentStatus.COMPLETED,
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW,
        AppointmentStatus.RESCHEDULED,
    )

/** Mirrors `com.denticode.desktop.domain.model.PaymentMethod`. */
enum class PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    INSURANCE,
    OTHER,
    ;

    companion object {
        fun fromDb(value: String?): PaymentMethod? =
            value?.trim()?.takeIf { it.isNotEmpty() }?.let { v ->
                entries.firstOrNull { it.name.equals(v, ignoreCase = true) }
            }
    }

    val displayLabel: String
        get() =
            when (this) {
                CASH -> "Efectivo"
                CARD -> "Tarjeta"
                TRANSFER -> "Transferencia"
                INSURANCE -> "Seguro"
                OTHER -> "Otro"
            }
}

data class Doctor(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val contactPhone: String?,
    val specialization: String?,
    val isActive: Boolean,
) {
    val fullName: String get() = "Dr. $firstName $lastName".trim()
}

data class Patient(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val contactPhone: String,
    val email: String?,
    val medicalHistorySummary: String?,
    val createdAtEpochMs: Long,
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class PatientRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val contactPhone: String,
    val email: String?,
    val medicalHistorySummary: String?,
)

data class ProcedureTypeRow(
    val id: Int,
    val name: String,
    val description: String?,
    val defaultDurationMinutes: Int?,
    val standardPrice: Double?,
    val requiresToothSpecification: Boolean,
    val category: String?,
    val isActive: Boolean,
)

data class Consultory(
    val id: Int,
    val name: String,
    val shortCode: String?,
    val sortOrder: Int,
)

data class TreatmentFacility(
    val id: Int,
    val facilityCode: String,
    val categoryKey: String,
    val displayName: String,
    val sortOrder: Int,
)

data class AppointmentRow(
    val id: Int,
    val patientId: Int,
    val patientName: String,
    val primaryDoctorId: Int,
    val doctorName: String,
    val scheduledAt: String,
    val estimatedDurationMinutes: Int?,
    val purpose: String?,
    val notes: String?,
    val status: AppointmentStatus,
)

/** Visit to schedule: date and clock time are persisted together as `appointments.scheduled_at` (ISO local date-time). */
data class AppointmentVisitRequest(
    val patientId: Int,
    val primaryDoctorId: Int,
    val visitDate: LocalDate,
    val visitHour: Int,
    val visitMinute: Int,
    val estimatedDurationMinutes: Int?,
    val purpose: String?,
    val notes: String?,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
)

/** Actualización de una visita existente (misma forma de fecha/hora que al crear). */
data class AppointmentEditRequest(
    val patientId: Int,
    val primaryDoctorId: Int,
    val visitDate: LocalDate,
    val visitHour: Int,
    val visitMinute: Int,
    val estimatedDurationMinutes: Int?,
    val purpose: String?,
    val notes: String?,
    val status: AppointmentStatus,
)

data class MaterialStockRow(
    val consultoryName: String,
    val consultoryShortCode: String?,
    val facilityDisplayName: String,
    val facilityCode: String,
    val quantity: Int,
)

data class PaymentRow(
    val id: Int,
    val patientName: String,
    val amount: Double,
    val method: PaymentMethod?,
    val paidAt: String,
    val note: String?,
)

/** One payment row for a single patient (ficha / ledger). */
data class PatientLedgerPayment(
    val id: Int,
    val amount: Double,
    val method: PaymentMethod?,
    val paidAt: String,
    val note: String?,
)

data class ClinicOverview(
    val patientCount: Int,
    val doctorCount: Int,
    val appointmentCount: Int,
    val upcomingAppointmentCount: Int,
    val paymentTotalRecent: Double,
)
