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

/** Tratamiento/insumo del catálogo de facilidades (para registrarlo como línea de stock). */
data class TreatmentFacilityRow(
    val id: Int,
    val code: String,
    val categoryKey: String,
    val displayName: String,
    val isActive: Boolean = true,
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

// ── Reports ────────────────────────────────────────────────────────────────

/** Ingreso de un día dentro del rango del reporte. */
data class RevenueDayPoint(
    val date: LocalDate,
    val revenue: Double,
)

/** Citas programadas en un día del rango (para el gráfico de agendamiento). */
data class AppointmentDayPoint(
    val date: LocalDate,
    val count: Int,
)

/** Ingreso y cantidad de pagos agrupados por método. */
data class RevenueByMethodSlice(
    val method: String,
    val count: Int,
    val revenue: Double,
)

/** Conteo de citas por estado dentro del rango. */
data class AppointmentStatusSlice(
    val status: AppointmentStatus,
    val count: Int,
)

/** Procedimiento más facturado en el rango (cantidad + ingreso). */
data class TopProcedureRow(
    val name: String,
    val count: Int,
    val revenue: Double,
)

/** Doctor con mayor ingreso facturado en el rango (cantidad de pagos + ingreso). */
data class TopDoctorRow(
    val doctorName: String,
    val count: Int,
    val revenue: Double,
)

/** Pacientes nuevos registrados en un mes del rango (etiqueta "yyyy-MM"). */
data class PatientsPerMonthPoint(
    val month: String,
    val count: Int,
)

/** Ingreso y cantidad de pagos agrupados por categoría de procedimiento. */
data class ProcedureCategorySlice(
    val category: String,
    val count: Int,
    val revenue: Double,
)

/** Comparación entre el precio de catálogo y lo efectivamente cobrado por acción. */
data class RevenueVsCatalog(
    val totalCatalog: Double,
    val totalCharged: Double,
    val actionCount: Int,
) {
    val discount: Double get() = (totalCatalog - totalCharged).coerceAtLeast(0.0)

    val discountRate: Double
        get() = if (totalCatalog > 0) discount / totalCatalog * 100.0 else 0.0
}

/** Movimientos de stock de un tipo en el período (entradas/salidas en unidades y valor). */
data class StockMovementSlice(
    val label: String,
    val count: Int,
    val unitsIn: Int,
    val unitsOut: Int,
    val valueIn: Double,
    val valueOut: Double,
)

/** Resumen de movimientos de inventario del período. */
data class StockMovements(
    val movementCount: Int,
    val unitsIn: Int,
    val unitsOut: Int,
    val valueIn: Double,
    val valueOut: Double,
    val byType: List<StockMovementSlice>,
)

/** Resumen de reporte para un rango de fechas (todo leído de la base real). */
data class ReportsOverview(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalRevenue: Double,
    val paymentCount: Int,
    val newPatientsCount: Int,
    val appointmentCount: Int,
    val completedCount: Int,
    val cancelledCount: Int,
    val noShowCount: Int,
    val dailyRevenue: List<RevenueDayPoint>,
    val appointmentsPerDay: List<AppointmentDayPoint>,
    val revenueByMethod: List<RevenueByMethodSlice>,
    val appointmentByStatus: List<AppointmentStatusSlice>,
    val topProcedures: List<TopProcedureRow>,
    val topDoctors: List<TopDoctorRow>,
    val patientsPerMonth: List<PatientsPerMonthPoint>,
    val revenueByCategory: List<ProcedureCategorySlice>,
    val revenueVsCatalog: RevenueVsCatalog,
    val stockMovements: StockMovements,
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

data class PurchaseOrderRow(
    val id: Int,
    val supplierId: Int?,
    val supplierName: String?,
    val status: String,
    val orderDateEpochMs: Long,
    val receivedAtEpochMs: Long?,
    val notes: String?,
    val totalCost: Double,
    val itemCount: Int,
)

data class PurchaseOrderItemRow(
    val orderItemId: Int,
    val orderId: Int,
    val consultoryId: Int,
    val facilityId: Int,
    val quantity: Int,
    val unitCost: Double,
)

data class PurchaseOrderItemRequest(
    val consultoryId: Int,
    val facilityId: Int,
    val quantity: Int,
    val unitCost: Double,
)

data class PurchaseOrderRegisterRequest(
    val supplierId: Int? = null,
    val notes: String? = null,
    val items: List<PurchaseOrderItemRequest>,
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

// ── Patient Clinical Workspace ────────────────────────────────────────────

/** Tipo de registro en el historial médico del paciente. */
enum class MedicalRecordType {
    ALLERGY,
    CONDITION,
    SURGERY,
    MEDICATION,
    ;

    companion object {
        fun fromDb(value: String?): MedicalRecordType =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: CONDITION
    }

    val labelEs: String
        get() =
            when (this) {
                ALLERGY -> "Alergia"
                CONDITION -> "Condición"
                SURGERY -> "Cirugía"
                MEDICATION -> "Medicación"
            }
}

fun medicalRecordTypeOptions(): List<MedicalRecordType> =
    listOf(
        MedicalRecordType.ALLERGY,
        MedicalRecordType.CONDITION,
        MedicalRecordType.SURGERY,
        MedicalRecordType.MEDICATION,
    )

/** Entrada del historial médico del paciente. */
data class PatientMedicalRecord(
    val id: Int,
    val patientId: Int,
    val recordType: MedicalRecordType,
    val description: String,
    val recordedAt: String,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val isActive: Boolean,
    val notes: String?,
    val createdAtEpochMs: Long,
)

data class MedicalRecordRegisterRequest(
    val recordType: MedicalRecordType,
    val description: String,
    val recordedAt: String,
    val doctorId: Int? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
)

data class MedicalRecordUpdateRequest(
    val recordType: MedicalRecordType,
    val description: String,
    val recordedAt: String,
    val doctorId: Int? = null,
    val isActive: Boolean,
    val notes: String? = null,
)

/** Entrada del historial dental del paciente. */
data class PatientDentalRecord(
    val id: Int,
    val patientId: Int,
    val toothNumber: String?,
    val toothQuadrant: String?,
    val diagnosis: String,
    val treatmentPerformed: String?,
    val procedureTypeId: Int? = null,
    val procedureTypeName: String? = null,
    val recordedAt: String,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val notes: String?,
    val createdAtEpochMs: Long,
)

data class DentalRecordRegisterRequest(
    val toothNumber: String?,
    val toothQuadrant: String?,
    val diagnosis: String,
    val treatmentPerformed: String? = null,
    val procedureTypeId: Int? = null,
    val recordedAt: String,
    val doctorId: Int? = null,
    val notes: String? = null,
)

data class DentalRecordUpdateRequest(
    val toothNumber: String?,
    val toothQuadrant: String?,
    val diagnosis: String,
    val treatmentPerformed: String? = null,
    val procedureTypeId: Int? = null,
    val recordedAt: String,
    val doctorId: Int? = null,
    val notes: String? = null,
)

/** Categoría de documento clínico del paciente. */
enum class DocumentCategory {
    RADIOGRAPH,
    PHOTO,
    PDF,
    CONSENT,
    TREATMENT_DOC,
    OTHER,
    ;

    companion object {
        fun fromDb(value: String?): DocumentCategory =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: OTHER
    }

    val labelEs: String
        get() =
            when (this) {
                RADIOGRAPH -> "Radiografía"
                PHOTO -> "Foto"
                PDF -> "PDF"
                CONSENT -> "Consentimiento"
                TREATMENT_DOC -> "Doc. tratamiento"
                OTHER -> "Otro"
            }
}

fun documentCategoryOptions(): List<DocumentCategory> =
    listOf(
        DocumentCategory.RADIOGRAPH,
        DocumentCategory.PHOTO,
        DocumentCategory.PDF,
        DocumentCategory.CONSENT,
        DocumentCategory.TREATMENT_DOC,
        DocumentCategory.OTHER,
    )

/** Documento adjunto a la ficha del paciente (metadatos + ruta local). */
data class PatientDocument(
    val id: Int,
    val patientId: Int,
    val title: String,
    val category: DocumentCategory,
    val fileName: String,
    val filePath: String?,
    val mimeType: String?,
    val fileSize: Long,
    val notes: String?,
    val uploadedAtEpochMs: Long,
)

data class PatientDocumentRegisterRequest(
    val title: String,
    val category: DocumentCategory,
    val fileName: String,
    val filePath: String? = null,
    val mimeType: String? = null,
    val fileSize: Long = 0,
    val notes: String? = null,
)

/** Nota libre de la ficha del paciente. */
data class PatientNote(
    val id: Int,
    val patientId: Int,
    val body: String,
    val authorLabel: String,
    val isPinned: Boolean,
    val createdAtEpochMs: Long,
)

data class PatientNoteRegisterRequest(
    val body: String,
    val authorLabel: String = "Recepción",
    val isPinned: Boolean = false,
)

/** Estado de una receta médica. */
enum class PrescriptionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    ;

    companion object {
        fun fromDb(value: String?): PrescriptionStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: ACTIVE
    }

    val labelEs: String
        get() =
            when (this) {
                ACTIVE -> "Activa"
                COMPLETED -> "Completada"
                CANCELLED -> "Cancelada"
            }
}

fun prescriptionStatusOptions(): List<PrescriptionStatus> =
    listOf(
        PrescriptionStatus.ACTIVE,
        PrescriptionStatus.COMPLETED,
        PrescriptionStatus.CANCELLED,
    )

/** Receta médica emitida a un paciente. */
data class Prescription(
    val id: Int,
    val patientId: Int,
    val medicine: String,
    val dosage: String,
    val frequency: String,
    val instructions: String?,
    val prescribedAt: String,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val status: PrescriptionStatus,
    val createdAtEpochMs: Long,
)

data class PrescriptionRegisterRequest(
    val medicine: String,
    val dosage: String,
    val frequency: String,
    val instructions: String? = null,
    val prescribedAt: String,
    val doctorId: Int? = null,
    val status: PrescriptionStatus = PrescriptionStatus.ACTIVE,
)

/** Estado de un seguimiento programado. */
enum class FollowUpStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    ;

    companion object {
        fun fromDb(value: String?): FollowUpStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: PENDING
    }

    val labelEs: String
        get() =
            when (this) {
                PENDING -> "Pendiente"
                COMPLETED -> "Completado"
                CANCELLED -> "Cancelado"
            }
}

fun followUpStatusOptions(): List<FollowUpStatus> =
    listOf(
        FollowUpStatus.PENDING,
        FollowUpStatus.COMPLETED,
        FollowUpStatus.CANCELLED,
    )

/** Seguimiento programado para el paciente. */
data class FollowUp(
    val id: Int,
    val patientId: Int,
    val dueDate: String,
    val notes: String?,
    val status: FollowUpStatus,
    val appointmentId: Int? = null,
    val appointmentDateLabel: String? = null,
    val createdAtEpochMs: Long,
)

data class FollowUpRegisterRequest(
    val dueDate: String,
    val notes: String? = null,
    val status: FollowUpStatus = FollowUpStatus.PENDING,
    val appointmentId: Int? = null,
)

/** Carga completa de la ficha clínica del paciente. */
data class PatientClinicalProfile(
    val patient: Patient,
    val medicalRecords: List<PatientMedicalRecord>,
    val dentalRecords: List<PatientDentalRecord>,
    val documents: List<PatientDocument>,
    val notes: List<PatientNote>,
    val prescriptions: List<Prescription>,
    val followUps: List<FollowUp>,
)

// ── Treatment Plans ────────────────────────────────────────────────────────

/** Estado general de un plan de tratamiento. */
enum class TreatmentPlanStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    ;

    companion object {
        fun fromDb(value: String?): TreatmentPlanStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: DRAFT
    }

    val labelEs: String
        get() =
            when (this) {
                DRAFT -> "Borrador"
                ACTIVE -> "Activo"
                COMPLETED -> "Completado"
                CANCELLED -> "Cancelado"
            }
}

fun treatmentPlanStatusOptions(): List<TreatmentPlanStatus> =
    listOf(
        TreatmentPlanStatus.DRAFT,
        TreatmentPlanStatus.ACTIVE,
        TreatmentPlanStatus.COMPLETED,
        TreatmentPlanStatus.CANCELLED,
    )

/** Estado de una fase dentro de un plan de tratamiento. */
enum class TreatmentPlanPhaseStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    ;

    companion object {
        fun fromDb(value: String?): TreatmentPlanPhaseStatus =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: PENDING
    }

    val labelEs: String
        get() =
            when (this) {
                PENDING -> "Pendiente"
                IN_PROGRESS -> "En progreso"
                COMPLETED -> "Completado"
                CANCELLED -> "Cancelado"
            }
}

fun treatmentPlanPhaseStatusOptions(): List<TreatmentPlanPhaseStatus> =
    listOf(
        TreatmentPlanPhaseStatus.PENDING,
        TreatmentPlanPhaseStatus.IN_PROGRESS,
        TreatmentPlanPhaseStatus.COMPLETED,
        TreatmentPlanPhaseStatus.CANCELLED,
    )

/** Fase de un plan de tratamiento. */
data class TreatmentPlanPhase(
    val id: Int,
    val planId: Int,
    val name: String,
    val description: String?,
    val estimatedCost: Double,
    val status: TreatmentPlanPhaseStatus,
    val sortOrder: Int,
    val createdAtEpochMs: Long,
)

/** Plan de tratamiento por paciente, con sus fases. */
data class TreatmentPlan(
    val id: Int,
    val patientId: Int,
    val patientName: String,
    val title: String,
    val description: String?,
    val status: TreatmentPlanStatus,
    val estimatedCost: Double,
    val createdAtEpochMs: Long,
    val phases: List<TreatmentPlanPhase> = emptyList(),
) {
    val completedPhases: Int get() = phases.count { it.status == TreatmentPlanPhaseStatus.COMPLETED }

    val phaseProgress: Float
        get() = if (phases.isEmpty()) 0f else completedPhases.toFloat() / phases.size
}

data class TreatmentPlanRegisterRequest(
    val title: String,
    val description: String? = null,
    val status: TreatmentPlanStatus = TreatmentPlanStatus.DRAFT,
)

data class TreatmentPlanUpdateRequest(
    val title: String,
    val description: String? = null,
    val status: TreatmentPlanStatus,
)

data class TreatmentPlanPhaseRegisterRequest(
    val name: String,
    val description: String? = null,
    val estimatedCost: Double = 0.0,
    val status: TreatmentPlanPhaseStatus = TreatmentPlanPhaseStatus.PENDING,
    val sortOrder: Int = 0,
)

data class TreatmentPlanPhaseUpdateRequest(
    val name: String,
    val description: String? = null,
    val estimatedCost: Double,
    val status: TreatmentPlanPhaseStatus,
    val sortOrder: Int = 0,
)

// ── Users & Roles ─────────────────────────────────────────────────────────

/**
 * Permiso de acceso a un módulo/función. La asignación por rol vive en el
 * [RoleCatalog] (matriz estática, en código); la aplicación aún no impone
 * sesiones ni RBAC en las pantallas (pendiente del backlog post-milestones).
 */
enum class Permission(val labelEs: String) {
    DASHBOARD_VIEW("Ver dashboard"),
    APPOINTMENTS_VIEW("Ver citas"),
    APPOINTMENTS_MANAGE("Gestionar citas"),
    PATIENTS_VIEW("Ver pacientes"),
    PATIENTS_MANAGE("Gestionar pacientes"),
    DOCTORS_VIEW("Ver doctores"),
    DOCTORS_MANAGE("Gestionar doctores"),
    PROCEDURES_VIEW("Ver catálogo clínico"),
    PROCEDURES_MANAGE("Gestionar catálogo clínico"),
    INVENTORY_VIEW("Ver stock"),
    INVENTORY_MANAGE("Gestionar stock"),
    PAYMENTS_VIEW("Ver pagos"),
    PAYMENTS_MANAGE("Gestionar pagos"),
    REPORTS_VIEW("Ver reportes"),
    USERS_MANAGE("Gestionar usuarios"),
    SETTINGS_MANAGE("Gestionar configuración"),
    ;

    /** Módulo funcional al que pertenece (prefijo del nombre). */
    val moduleEs: String
        get() = permissionModuleLabel(this)
}

private val moduleLabelByPrefix: Map<String, String> =
    mapOf(
        "DASHBOARD" to "Dashboard",
        "APPOINTMENTS" to "Citas",
        "PATIENTS" to "Pacientes",
        "DOCTORS" to "Doctores",
        "PROCEDURES" to "Catálogo clínico",
        "INVENTORY" to "Stock insumos",
        "PAYMENTS" to "Pagos",
        "REPORTS" to "Reportes",
        "USERS" to "Usuarios",
        "SETTINGS" to "Configuración",
    )

private fun permissionModuleLabel(p: Permission): String =
    moduleLabelByPrefix[p.name.substringBefore('_')] ?: p.name.substringBefore('_')

/** Catálogo de roles: descripción y matriz de permisos (cómo se muestra en pantalla). */
enum class UserRole {
    ADMIN,
    RECEPTIONIST,
    USER,
    ;

    val displayLabel: String
        get() =
            when (this) {
                ADMIN -> "Administrador"
                RECEPTIONIST -> "Recepcionista"
                USER -> "Usuario"
            }

    val description: String
        get() =
            when (this) {
                ADMIN -> "Control total del sistema: usuarios, configuración y todos los módulos."
                RECEPTIONIST -> "Gestión del día a día: citas, pacientes y pagos. Sin acceso a stock, catálogo o configuración."
                USER -> "Consulta de información clínica y del consultorio con acceso de solo lectura."
            }

    /** Matriz de permisos asignada a este rol. */
    val permissions: Set<Permission>
        get() =
            when (this) {
                ADMIN -> Permission.entries.toSet()
                RECEPTIONIST ->
                    setOf(
                        Permission.DASHBOARD_VIEW,
                        Permission.APPOINTMENTS_VIEW,
                        Permission.APPOINTMENTS_MANAGE,
                        Permission.PATIENTS_VIEW,
                        Permission.PATIENTS_MANAGE,
                        Permission.PAYMENTS_VIEW,
                        Permission.PAYMENTS_MANAGE,
                        Permission.REPORTS_VIEW,
                    )
                USER ->
                    setOf(
                        Permission.DASHBOARD_VIEW,
                        Permission.APPOINTMENTS_VIEW,
                        Permission.PATIENTS_VIEW,
                        Permission.DOCTORS_VIEW,
                        Permission.PROCEDURES_VIEW,
                        Permission.INVENTORY_VIEW,
                        Permission.PAYMENTS_VIEW,
                        Permission.REPORTS_VIEW,
                    )
            }

    companion object {
        fun fromDb(value: String?): UserRole =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: USER
    }
}

/** Catálogo estático de roles y sus permisos (la fuente de verdad de la matriz). */
object RoleCatalog {
    fun roles(): List<UserRole> = UserRole.entries

    fun permissionsFor(role: UserRole): Set<Permission> = role.permissions

    fun can(role: UserRole, permission: Permission): Boolean = permission in role.permissions
}

/** Usuario del sistema con su rol principal. */
data class AppUser(
    val id: Int,
    val email: String,
    val displayName: String?,
    val role: UserRole,
    val isActive: Boolean,
    val createdAtEpochMs: Long,
)

/** KPIs para la cabecera de la pantalla de usuarios. */
data class UserDirectoryKpis(
    val totalUsers: Int,
    val activeUsers: Int,
    val adminCount: Int,
)

data class UserRegistrationRequest(
    val email: String,
    val displayName: String?,
    val password: String,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
)

data class UserUpdateRequest(
    val email: String,
    val displayName: String?,
    val role: UserRole,
    val isActive: Boolean,
    val newPassword: String? = null,
)
