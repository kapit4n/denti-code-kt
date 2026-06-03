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
    val officeRoom: String?,
    val isActive: Boolean,
) {
    val fullName: String get() = "Dr. $firstName $lastName".trim()
}

/** Estado mostrado en el directorio de doctores. */
enum class DoctorListStatus {
    ACTIVE,
    INACTIVE,
    VACATION,
    ;

    val labelEs: String
        get() =
            when (this) {
                ACTIVE -> "Activo"
                INACTIVE -> "Inactivo"
                VACATION -> "Vacaciones"
            }
}

data class DoctorDirectoryKpis(
    val totalDoctors: Int,
    val activeDoctors: Int,
    val todayAppointments: Int,
    val specialtyCount: Int,
)

/** Fila enriquecida para la tabla de doctores (citas + experiencia). */
data class DoctorDirectoryRow(
    val doctor: Doctor,
    val status: DoctorListStatus,
    val todayAppointmentsCount: Int,
    val totalAppointmentsCount: Int,
    val yearsExperience: Int,
)

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

/** Estado mostrado en el directorio de pacientes. */
enum class PatientListStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    ;

    val labelEs: String
        get() =
            when (this) {
                ACTIVE -> "Activo"
                INACTIVE -> "Inactivo"
                PENDING -> "Pendiente"
            }
}

data class PatientDirectoryKpis(
    val totalPatients: Int,
    val activePatients: Int,
    val newThisMonth: Int,
    val scheduledAppointments: Int,
    val pendingDebt: Double,
)

/** Fila enriquecida para la tabla de pacientes (citas + doctor + saldo). */
data class PatientDirectoryRow(
    val patient: Patient,
    val status: PatientListStatus,
    val primaryDoctorName: String?,
    val lastAppointmentAt: String?,
    val lastAppointmentTreatment: String?,
    val nextAppointmentAt: String?,
    val nextAppointmentTimeLabel: String?,
    val pendingBalance: Double,
)

data class PatientRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val contactPhone: String,
    val email: String?,
    val medicalHistorySummary: String?,
)

/** Estado del tratamiento realizado vinculado a paciente/cita. */
enum class TreatmentStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    ;

    companion object {
        fun fromDb(value: String?): TreatmentStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: PLANNED
    }

    val labelEs: String
        get() =
            when (this) {
                PLANNED -> "Planificado"
                IN_PROGRESS -> "En progreso"
                COMPLETED -> "Completado"
                CANCELLED -> "Cancelado"
            }
}

fun treatmentStatusOptions(): List<TreatmentStatus> =
    listOf(
        TreatmentStatus.PLANNED,
        TreatmentStatus.IN_PROGRESS,
        TreatmentStatus.COMPLETED,
        TreatmentStatus.CANCELLED,
    )

fun inferTreatmentStatusFromAppointment(status: AppointmentStatus): TreatmentStatus =
    when (status) {
        AppointmentStatus.COMPLETED -> TreatmentStatus.COMPLETED
        AppointmentStatus.IN_PROGRESS -> TreatmentStatus.IN_PROGRESS
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW,
        -> TreatmentStatus.CANCELLED
        else -> TreatmentStatus.PLANNED
    }

data class PatientTreatmentRow(
    val id: Int,
    val patientId: Int,
    val patientName: String,
    val appointmentId: Int,
    val procedureTypeId: Int,
    val procedureTypeName: String,
    val doctorName: String,
    val status: TreatmentStatus,
    val standardPrice: Double?,
    val unitPrice: Double,
    val totalPrice: Double,
    val actionAt: String,
    val descriptionNotes: String?,
)

/** Opción para vincular un pago a un tratamiento ya registrado. */
data class TreatmentPaymentOption(
    val performedActionId: Int,
    val procedureTypeId: Int,
    val label: String,
    val amount: Double,
    val status: TreatmentStatus,
) {
    companion object {
        fun none(): TreatmentPaymentOption =
            TreatmentPaymentOption(
                performedActionId = -1,
                procedureTypeId = -1,
                label = "Sin vincular a tratamiento",
                amount = 0.0,
                status = TreatmentStatus.PLANNED,
            )

        fun isNone(option: TreatmentPaymentOption): Boolean = option.performedActionId < 0
    }
}

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

/** Opción de UI para vincular (o no) un tipo de procedimiento a una cita o pago. */
data class ProcedureTypeOption(val procedureTypeId: Int?, val displayName: String) {
    companion object {
        fun none(): ProcedureTypeOption = ProcedureTypeOption(null, "Sin tratamiento")
        fun from(row: ProcedureTypeRow): ProcedureTypeOption =
            ProcedureTypeOption(row.id, row.name)
    }
}

data class ProcedureTypeRegisterRequest(
    val name: String,
    val description: String? = null,
    val defaultDurationMinutes: Int? = null,
    val standardPrice: Double? = null,
    val requiresToothSpecification: Boolean = false,
    val category: String? = null,
    val isActive: Boolean = true,
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
    val procedureTypeId: Int?,
    val procedureTypeName: String?,
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
    val procedureTypeId: Int? = null,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val treatmentStatus: TreatmentStatus? = null,
    /** Precio acordado; null = precio estándar del catálogo. */
    val treatmentUnitPrice: Double? = null,
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
    val procedureTypeId: Int? = null,
    val status: AppointmentStatus,
    val treatmentStatus: TreatmentStatus? = null,
    val treatmentUnitPrice: Double? = null,
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
    val patientId: Int,
    val patientName: String,
    val amount: Double,
    val method: PaymentMethod?,
    val paidAt: String,
    val note: String?,
    val procedureTypeName: String?,
    val performedActionId: Int?,
    val treatmentStatus: TreatmentStatus?,
)

/** Estado mostrado en el listado de pagos (derivado de método/nota; sin columna en BD). */
enum class PaymentDisplayStatus {
    PAID,
    PENDING,
    FAILED,
    ;

    val labelEs: String
        get() =
            when (this) {
                PAID -> "Pagado"
                PENDING -> "Pendiente"
                FAILED -> "Fallido"
            }
}

data class PaymentDirectoryKpis(
    val totalCollected: Double,
    val completedCount: Int,
    val pendingCount: Int,
    val averageAmount: Double,
)

/** One payment row for a single patient (ficha / ledger). */
data class PatientLedgerPayment(
    val id: Int,
    val amount: Double,
    val method: PaymentMethod?,
    val paidAt: String,
    val note: String?,
    val procedureTypeId: Int?,
    val procedureTypeName: String?,
    val performedActionId: Int?,
)

/** Registrar un pago desde la ficha del paciente (`patient_id` fijado por el llamador). */
data class PatientPaymentRegisterRequest(
    val amount: Double,
    val method: PaymentMethod?,
    val paidAtIso: String,
    val note: String?,
    val procedureTypeId: Int? = null,
    val performedActionId: Int? = null,
)

data class ClinicOverview(
    val patientCount: Int,
    val doctorCount: Int,
    val appointmentCount: Int,
    val upcomingAppointmentCount: Int,
    val paymentTotalRecent: Double,
)
