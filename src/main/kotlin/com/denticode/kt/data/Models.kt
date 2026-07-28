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
    val licenseNumber: String? = null,
    val address: String? = null,
    val workingDays: String? = null,
    val workingHours: String? = null,
    val consultoryId: Int? = null,
    val notes: String? = null,
    val isActive: Boolean,
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long? = null,
    val updatedAtEpochMs: Long? = null,
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

data class DoctorRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val contactPhone: String? = null,
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val officeRoom: String? = null,
    val address: String? = null,
    val workingDays: String? = null,
    val workingHours: String? = null,
    val consultoryId: Int? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
)

data class DoctorUpdateRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val contactPhone: String? = null,
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val officeRoom: String? = null,
    val address: String? = null,
    val workingDays: String? = null,
    val workingHours: String? = null,
    val consultoryId: Int? = null,
    val notes: String? = null,
    val isActive: Boolean,
)

data class Patient(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val documentNumber: String?,
    val gender: String?,
    val address: String?,
    val contactPhone: String,
    val email: String?,
    val medicalHistorySummary: String?,
    val isArchived: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long?,
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val age: Int
        get() = try {
            val birth = java.time.LocalDate.parse(dateOfBirth)
            val today = java.time.LocalDate.now()
            java.time.Period.between(birth, today).years
        } catch (_: Exception) { 0 }
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
    val documentNumber: String?,
    val contactPhone: String,
    val email: String?,
    val medicalHistorySummary: String?,
)

data class PatientUpdateRequest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val documentNumber: String?,
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
    /** Saldo pendiente sugerido para el pago. */
    val amount: Double,
    val status: TreatmentStatus,
    val totalPrice: Double = amount,
    val amountPaid: Double = 0.0,
) {
    val remainingBalance: Double get() = (totalPrice - amountPaid).coerceAtLeast(0.0)

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
    val categoryId: Int? = null,
    val currency: String = "BOB",
    val color: String? = null,
    val icon: String? = null,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val isActive: Boolean,
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long? = null,
    val updatedAtEpochMs: Long? = null,
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
    val categoryId: Int? = null,
    val currency: String = "BOB",
    val color: String? = null,
    val icon: String? = null,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val isActive: Boolean = true,
)

data class ProcedureTypeUpdateRequest(
    val name: String,
    val description: String? = null,
    val defaultDurationMinutes: Int? = null,
    val standardPrice: Double? = null,
    val requiresToothSpecification: Boolean = false,
    val category: String? = null,
    val categoryId: Int? = null,
    val currency: String = "BOB",
    val color: String? = null,
    val icon: String? = null,
    val isFavorite: Boolean = false,
    val notes: String? = null,
    val isActive: Boolean = true,
)

data class Consultory(
    val id: Int,
    val name: String,
    val shortCode: String?,
    val sortOrder: Int,
)

data class TreatmentCategory(
    val id: Int,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long? = null,
    val updatedAtEpochMs: Long? = null,
)

data class CategoryRegisterRequest(
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

data class CategoryUpdateRequest(
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
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
    val createdAtEpochMs: Long? = null,
    val updatedAtEpochMs: Long? = null,
    val source: AppointmentSource = AppointmentSource.MANUAL,
    val cancellationReason: String? = null,
    val followUpAppointmentId: Int? = null,
)

enum class AppointmentSource {
    MANUAL,
    ONLINE,
    ;

    companion object {
        fun fromDb(value: String?): AppointmentSource =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: MANUAL
    }

    val labelEs: String
        get() =
            when (this) {
                MANUAL -> "Manual"
                ONLINE -> "En línea"
            }
}

enum class AppointmentPaymentStatus {
    PAID,
    PENDING,
    PARTIAL,
    NONE,
    ;

    val labelEs: String
        get() =
            when (this) {
                PAID -> "Pagado"
                PENDING -> "Pendiente"
                PARTIAL -> "Parcialmente pagado"
                NONE -> "Sin pagos"
            }
}

data class AppointmentNoteRow(
    val id: Int,
    val appointmentId: Int,
    val body: String,
    val authorLabel: String,
    val createdAtEpochMs: Long,
)

data class AppointmentAuditEntry(
    val id: Int,
    val appointmentId: Int,
    val action: String,
    val actorLabel: String,
    val detail: String?,
    val createdAtEpochMs: Long,
)

data class AppointmentPaymentSummary(
    val treatmentCost: Double?,
    val amountPaid: Double,
    val remainingBalance: Double,
    val status: AppointmentPaymentStatus,
    val paymentIds: List<Int>,
)

data class AppointmentDetailSnapshot(
    val appointment: AppointmentRow,
    val patientPhone: String?,
    val structuredNotes: List<AppointmentNoteRow>,
    val paymentSummary: AppointmentPaymentSummary,
    val auditLog: List<AppointmentAuditEntry>,
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
    val cancellationReason: String? = null,
)

data class MaterialStockRow(
    val consultoryName: String,
    val consultoryShortCode: String?,
    val facilityDisplayName: String,
    val facilityCode: String,
    val quantity: Int,
)

/** Estado de stock para UI de inventario. */
enum class StockStatus {
    OPTIMAL,
    LOW,
    OUT,
    ;

    val labelEs: String
        get() =
            when (this) {
                OPTIMAL -> "Óptimo"
                LOW -> "Stock bajo"
                OUT -> "Agotado"
            }
}

fun resolveInventoryStockStatus(quantity: Int, minQuantity: Int): StockStatus =
    when {
        quantity <= 0 -> StockStatus.OUT
        quantity <= minQuantity -> StockStatus.LOW
        else -> StockStatus.OPTIMAL
    }

data class InventoryLineRow(
    val lineId: Int,
    val consultoryId: Int,
    val facilityId: Int,
    val consultoryName: String,
    val consultoryShortCode: String?,
    val facilityDisplayName: String,
    val facilityCode: String,
    val categoryKey: String,
    val quantity: Int,
    val minQuantity: Int,
    val maxQuantity: Int,
    val unitLabel: String = "uds",
    val lastUpdatedEpochMs: Long,
    val lastUpdatedBy: String = "Recepción",
    val isActive: Boolean = true,
)

data class InventoryMovementRow(
    val id: Int,
    val consultoryId: Int,
    val facilityId: Int,
    val quantityChange: Int,
    val type: String,
    val note: String?,
    val createdAtEpochMs: Long,
    val actorLabel: String = "Recepción",
)

data class InventoryDirectoryKpis(
    val totalItems: Int,
    val totalUnits: Int,
    val lowStockCount: Int,
    val outOfStockCount: Int,
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

/** Registrar un tratamiento realizado vinculado a paciente y cita. */
data class PatientTreatmentRegisterRequest(
    val patientId: Int,
    val primaryDoctorId: Int,
    val procedureTypeId: Int,
    val actionDate: LocalDate,
    val actionHour: Int,
    val actionMinute: Int,
    val status: TreatmentStatus,
    val unitPrice: Double? = null,
    val descriptionNotes: String? = null,
    val appointmentId: Int? = null,
    /** Si no hay cita, crear una visita mínima en la fecha del tratamiento. */
    val createAppointmentIfMissing: Boolean = true,
)

data class PatientPaymentRegisterRequest(
    val amount: Double,
    val method: PaymentMethod?,
    val paidAtIso: String,
    val note: String?,
    val procedureTypeId: Int? = null,
    val performedActionId: Int? = null,
    val appointmentId: Int? = null,
)

data class ClinicOverview(
    val patientCount: Int,
    val doctorCount: Int,
    val appointmentCount: Int,
    val upcomingAppointmentCount: Int,
    val paymentTotalRecent: Double,
)

// ── Inventory Product Management ──────────────────────────────────────────

enum class InventoryProductStatus {
    ACTIVE,
    LOW_STOCK,
    OUT_OF_STOCK,
    EXPIRED,
    ;

    val labelEs: String
        get() = when (this) {
            ACTIVE -> "Activo"
            LOW_STOCK -> "Stock bajo"
            OUT_OF_STOCK -> "Agotado"
            EXPIRED -> "Vencido"
        }
}

fun resolveInventoryProductStatus(currentStock: Int, minStock: Int, expirationDate: String?): InventoryProductStatus =
    when {
        !expirationDate.isNullOrBlank() && runCatching { java.time.LocalDate.parse(expirationDate) }.getOrNull()
            ?.isBefore(java.time.LocalDate.now()) == true -> InventoryProductStatus.EXPIRED
        currentStock <= 0 -> InventoryProductStatus.OUT_OF_STOCK
        currentStock <= minStock -> InventoryProductStatus.LOW_STOCK
        else -> InventoryProductStatus.ACTIVE
    }

data class InventoryProduct(
    val id: Int,
    val name: String,
    val code: String,
    val description: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val unit: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minStock: Int,
    val maxStock: Int,
    val supplierId: Int?,
    val supplierName: String?,
    val expirationDate: String?,
    val barcode: String?,
    val color: String?,
    val icon: String?,
    val notes: String?,
    val isActive: Boolean,
    val isArchived: Boolean,
    val createdAtEpochMs: Long?,
    val updatedAtEpochMs: Long?,
)

data class InventoryProductRegisterRequest(
    val name: String,
    val code: String,
    val description: String? = null,
    val categoryId: Int? = null,
    val unit: String = "uds",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Int = 0,
    val minStock: Int = 0,
    val maxStock: Int = 0,
    val supplierId: Int? = null,
    val expirationDate: String? = null,
    val barcode: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
)

data class InventoryProductUpdateRequest(
    val name: String,
    val code: String,
    val description: String? = null,
    val categoryId: Int? = null,
    val unit: String = "uds",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Int = 0,
    val minStock: Int = 0,
    val maxStock: Int = 0,
    val supplierId: Int? = null,
    val expirationDate: String? = null,
    val barcode: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
)

enum class InventoryMovementType(val labelEs: String, val icon: String) {
    PURCHASE("Compra", "shopping_cart"),
    TREATMENT_CONSUMPTION("Consumo tratamiento", "medical_services"),
    MANUAL_ADJUSTMENT("Ajuste manual", "tune"),
    EXPIRED_DAMAGED_LOST("Vencido/Dañado/Perdido", "warning"),
    INVENTORY_CORRECTION("Corrección inventario", "fact_check"),
    RETURN_TO_SUPPLIER("Devolución proveedor", "undo"),
    INITIAL_INVENTORY("Inventario inicial", "inventory_2"),
    STOCK_TRANSFER("Transferencia", "swap_horiz"),
}

data class InventoryProductMovementRow(
    val id: Int,
    val productId: Int,
    val quantityChange: Int,
    val type: String,
    val note: String?,
    val previousStock: Int = 0,
    val currentStock: Int = 0,
    val unitCost: Double = 0.0,
    val reason: String? = null,
    val referenceNumber: String? = null,
    val status: String = "COMPLETED",
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long? = null,
)

data class InventoryMovementStats(
    val todayEntries: Int,
    val todayConsumptions: Int,
    val todayAdjustments: Int,
    val todayValue: Double,
    val recentMovementsCount: Int,
    val lowStockAlerts: Int,
)

data class InventoryProductSummary(
    val id: Int,
    val name: String,
    val code: String,
    val currentStock: Int,
    val unit: String,
    val purchasePrice: Double,
)

data class InventoryProductCategory(
    val id: Int,
    val name: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val sortOrder: Int,
    val isActive: Boolean,
    val isArchived: Boolean,
    val createdAtEpochMs: Long?,
    val updatedAtEpochMs: Long?,
)

data class InventoryCategoryRegisterRequest(
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

data class InventoryCategoryUpdateRequest(
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
)

data class Supplier(
    val id: Int,
    val name: String,
    val contactName: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val notes: String?,
    val isActive: Boolean,
    val isArchived: Boolean,
    val createdAtEpochMs: Long?,
    val updatedAtEpochMs: Long?,
)

data class SupplierRegisterRequest(
    val name: String,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
)

data class SupplierUpdateRequest(
    val name: String,
    val contactName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
)

data class InventoryProductKpis(
    val totalProducts: Int,
    val totalUnits: Int,
    val lowStockCount: Int,
    val outOfStockCount: Int,
    val expiringSoonCount: Int,
    val totalValue: Double,
)

interface TreatmentMaterialRequirement {
    val productId: Int
    val productName: String
    val quantityNeeded: Int
    val unit: String
    val isOptional: Boolean
}

interface InventoryConsumptionPlan {
    val treatmentId: Int
    val materials: List<TreatmentMaterialRequirement>
    val plannedDate: Long
    val notes: String?
}
