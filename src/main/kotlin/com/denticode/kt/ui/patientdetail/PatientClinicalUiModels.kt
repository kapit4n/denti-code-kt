package com.denticode.kt.ui.patientdetail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.DocumentCategory
import com.denticode.kt.data.FollowUp
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.MedicalRecordType
import com.denticode.kt.data.PatientDocument
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.PatientMedicalRecord
import com.denticode.kt.data.PatientNote
import com.denticode.kt.data.PatientDentalRecord
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.Prescription
import com.denticode.kt.data.PrescriptionStatus
import com.denticode.kt.data.TreatmentPlan
import com.denticode.kt.data.TreatmentPlanPhase
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.appointments.AppointmentPremiumPalette
import com.denticode.kt.ui.parseAppointmentScheduledAt
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Pestañas de la ficha clínica del paciente. */
enum class PatientWorkspaceTab(val label: String) {
    OVERVIEW("Resumen"),
    CLINICAL_HISTORY("Historial clínico"),
    TREATMENT_PLAN("Plan de tratamiento"),
    TIMELINE("Cronología"),
    APPOINTMENTS("Citas"),
    PAYMENTS("Pagos"),
    DOCUMENTS("Documentos"),
    NOTES("Notas"),
    PRESCRIPTIONS("Recetas"),
    FOLLOW_UPS("Seguimientos"),
}

private val workspaceDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

fun formatWorkspaceDate(isoDate: String): String =
    runCatching { LocalDate.parse(isoDate).format(workspaceDateFmt) }
        .getOrDefault(isoDate)

fun formatWorkspaceEpoch(epochMs: Long): String =
    Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate().format(workspaceDateFmt)

private fun formatFileSize(bytes: Long): String =
    when {
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.0f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }

// ── Medical history ────────────────────────────────────────────────────────

data class PatientMedicalRecordUi(
    val id: Int,
    val recordType: MedicalRecordType,
    val recordTypeLabel: String,
    val description: String,
    val recordedAt: String,
    val recordedAtLabel: String,
    val doctorName: String?,
    val isActive: Boolean,
    val notes: String?,
    val color: Color,
)

fun medicalRecordColor(type: MedicalRecordType): Color =
    when (type) {
        MedicalRecordType.ALLERGY -> Color(0xFFFF5A5F)
        MedicalRecordType.CONDITION -> Color(0xFFFFB020)
        MedicalRecordType.SURGERY -> Color(0xFF8B5CF6)
        MedicalRecordType.MEDICATION -> Color(0xFF4DA3FF)
    }

fun PatientMedicalRecord.toUi(): PatientMedicalRecordUi =
    PatientMedicalRecordUi(
        id = id,
        recordType = recordType,
        recordTypeLabel = recordType.labelEs,
        description = description,
        recordedAt = recordedAt,
        recordedAtLabel = formatWorkspaceDate(recordedAt),
        doctorName = doctorName,
        isActive = isActive,
        notes = notes,
        color = medicalRecordColor(recordType),
    )

// ── Dental history ─────────────────────────────────────────────────────────

data class PatientDentalRecordUi(
    val id: Int,
    val toothNumber: String?,
    val toothLabel: String,
    val diagnosis: String,
    val treatmentPerformed: String?,
    val procedureTypeName: String?,
    val recordedAt: String,
    val recordedAtLabel: String,
    val doctorName: String?,
    val notes: String?,
)

fun PatientDentalRecord.toUi(): PatientDentalRecordUi =
    PatientDentalRecordUi(
        id = id,
        toothNumber = toothNumber,
        toothLabel = toothNumber?.let { "Pieza $it" } ?: "Sin pieza",
        diagnosis = diagnosis,
        treatmentPerformed = treatmentPerformed,
        procedureTypeName = procedureTypeName,
        recordedAt = recordedAt,
        recordedAtLabel = formatWorkspaceDate(recordedAt),
        doctorName = doctorName,
        notes = notes,
    )

// ── Documents ──────────────────────────────────────────────────────────────

data class PatientDocumentUi(
    val id: Int,
    val title: String,
    val category: DocumentCategory,
    val categoryLabel: String,
    val categoryColor: Color,
    val fileName: String,
    val fileSizeLabel: String,
    val filePath: String?,
    val notes: String?,
    val uploadedAtLabel: String,
)

fun documentCategoryColor(category: DocumentCategory): Color =
    when (category) {
        DocumentCategory.RADIOGRAPH -> Color(0xFF4DA3FF)
        DocumentCategory.PHOTO -> Color(0xFF34C759)
        DocumentCategory.PDF -> Color(0xFFFF5A5F)
        DocumentCategory.CONSENT -> Color(0xFF8B5CF6)
        DocumentCategory.TREATMENT_DOC -> Color(0xFFFFB020)
        DocumentCategory.OTHER -> Color(0xFF6B7280)
    }

fun PatientDocument.toUi(): PatientDocumentUi =
    PatientDocumentUi(
        id = id,
        title = title,
        category = category,
        categoryLabel = category.labelEs,
        categoryColor = documentCategoryColor(category),
        fileName = fileName,
        fileSizeLabel = formatFileSize(fileSize),
        filePath = filePath,
        notes = notes,
        uploadedAtLabel = formatWorkspaceEpoch(uploadedAtEpochMs),
    )

// ── Notes ──────────────────────────────────────────────────────────────────

data class PatientNoteUi(
    val id: Int,
    val body: String,
    val authorLabel: String,
    val isPinned: Boolean,
    val createdAtLabel: String,
)

fun PatientNote.toUi(): PatientNoteUi =
    PatientNoteUi(
        id = id,
        body = body,
        authorLabel = authorLabel,
        isPinned = isPinned,
        createdAtLabel = formatWorkspaceEpoch(createdAtEpochMs),
    )

// ── Prescriptions ──────────────────────────────────────────────────────────

data class PatientPrescriptionUi(
    val id: Int,
    val medicine: String,
    val dosage: String,
    val frequency: String,
    val instructions: String?,
    val prescribedAtLabel: String,
    val doctorName: String?,
    val status: PrescriptionStatus,
    val statusLabel: String,
    val statusColor: Color,
)

fun prescriptionStatusColor(status: PrescriptionStatus): Color =
    when (status) {
        PrescriptionStatus.ACTIVE -> AppointmentPremiumPalette.success
        PrescriptionStatus.COMPLETED -> AppointmentPremiumPalette.info
        PrescriptionStatus.CANCELLED -> AppointmentPremiumPalette.textSecondary
    }

fun Prescription.toUi(): PatientPrescriptionUi =
    PatientPrescriptionUi(
        id = id,
        medicine = medicine,
        dosage = dosage,
        frequency = frequency,
        instructions = instructions,
        prescribedAtLabel = formatWorkspaceDate(prescribedAt),
        doctorName = doctorName,
        status = status,
        statusLabel = status.labelEs,
        statusColor = prescriptionStatusColor(status),
    )

// ── Follow-ups ─────────────────────────────────────────────────────────────

enum class FollowUpDueState(val labelEs: String) {
    OVERDUE("Vencido"),
    TODAY("Hoy"),
    UPCOMING("Próximo"),
    DONE("Finalizado"),
}

data class PatientFollowUpUi(
    val id: Int,
    val dueDate: String,
    val dueDateLabel: String,
    val dueState: FollowUpDueState,
    val notes: String?,
    val status: FollowUpStatus,
    val statusLabel: String,
    val statusColor: Color,
)

fun followUpStatusColor(status: FollowUpStatus): Color =
    when (status) {
        FollowUpStatus.PENDING -> AppointmentPremiumPalette.warning
        FollowUpStatus.COMPLETED -> AppointmentPremiumPalette.success
        FollowUpStatus.CANCELLED -> AppointmentPremiumPalette.textSecondary
    }

fun FollowUp.toUi(): PatientFollowUpUi {
    val today = LocalDate.now()
    val due = runCatching { LocalDate.parse(dueDate) }.getOrNull()
    val dueState =
        when (status) {
            FollowUpStatus.COMPLETED,
            FollowUpStatus.CANCELLED,
            -> FollowUpDueState.DONE
            FollowUpStatus.PENDING -> {
                when {
                    due == null -> FollowUpDueState.UPCOMING
                    due.isBefore(today) -> FollowUpDueState.OVERDUE
                    due == today -> FollowUpDueState.TODAY
                    else -> FollowUpDueState.UPCOMING
                }
            }
        }
    return PatientFollowUpUi(
        id = id,
        dueDate = dueDate,
        dueDateLabel = formatWorkspaceDate(dueDate),
        dueState = dueState,
        notes = notes,
        status = status,
        statusLabel = status.labelEs,
        statusColor = followUpStatusColor(status),
    )
}

// ── Treatment plans ─────────────────────────────────────────────────────────

data class PatientTreatmentPlanUi(
    val id: Int,
    val title: String,
    val description: String?,
    val status: TreatmentPlanStatus,
    val statusLabel: String,
    val statusColor: Color,
    val estimatedCost: Double,
    val createdAtLabel: String,
    val phaseProgress: Float,
    val completedPhases: Int,
    val phases: List<PatientTreatmentPlanPhaseUi>,
)

data class PatientTreatmentPlanPhaseUi(
    val id: Int,
    val name: String,
    val description: String?,
    val estimatedCost: Double,
    val status: TreatmentPlanPhaseStatus,
    val statusLabel: String,
    val statusColor: Color,
)

fun treatmentPlanStatusColor(status: TreatmentPlanStatus): Color =
    when (status) {
        TreatmentPlanStatus.DRAFT -> AppointmentPremiumPalette.textSecondary
        TreatmentPlanStatus.ACTIVE -> AppointmentPremiumPalette.primary
        TreatmentPlanStatus.COMPLETED -> AppointmentPremiumPalette.success
        TreatmentPlanStatus.CANCELLED -> AppointmentPremiumPalette.error
    }

fun treatmentPlanPhaseStatusColor(status: TreatmentPlanPhaseStatus): Color =
    when (status) {
        TreatmentPlanPhaseStatus.PENDING -> AppointmentPremiumPalette.warning
        TreatmentPlanPhaseStatus.IN_PROGRESS -> AppointmentPremiumPalette.info
        TreatmentPlanPhaseStatus.COMPLETED -> AppointmentPremiumPalette.success
        TreatmentPlanPhaseStatus.CANCELLED -> AppointmentPremiumPalette.textSecondary
    }

fun TreatmentPlanPhase.toUi(): PatientTreatmentPlanPhaseUi =
    PatientTreatmentPlanPhaseUi(
        id = id,
        name = name,
        description = description,
        estimatedCost = estimatedCost,
        status = status,
        statusLabel = status.labelEs,
        statusColor = treatmentPlanPhaseStatusColor(status),
    )

fun TreatmentPlan.toUi(): PatientTreatmentPlanUi =
    PatientTreatmentPlanUi(
        id = id,
        title = title,
        description = description,
        status = status,
        statusLabel = status.labelEs,
        statusColor = treatmentPlanStatusColor(status),
        estimatedCost = estimatedCost,
        createdAtLabel = formatWorkspaceEpoch(createdAtEpochMs),
        phaseProgress = phaseProgress,
        completedPhases = completedPhases,
        phases = phases.map { it.toUi() },
    )

// ── Timeline ───────────────────────────────────────────────────────────────

enum class TimelineEntryKind {
    APPOINTMENT,
    TREATMENT,
    PAYMENT,
    NOTE,
    PRESCRIPTION,
    FOLLOW_UP,
    DOCUMENT,
    MEDICAL_RECORD,
    DENTAL_RECORD,
    TREATMENT_PLAN,
}

data class PatientTimelineEntry(
    val id: Long,
    val timestamp: Long,
    val kind: TimelineEntryKind,
    val title: String,
    val subtitle: String?,
    val amountLabel: String?,
    val color: Color,
)

private fun isoDateToEpoch(isoDate: String): Long =
    runCatching { LocalDate.parse(isoDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
        .getOrDefault(0L)

/** Combina todas las entradas clínicas en una cronología ordenada de más reciente a más antigua. */
fun buildPatientTimeline(
    appointments: List<AppointmentRow>,
    treatments: List<PatientTreatmentRow>,
    payments: List<PatientLedgerPayment>,
    medicalRecords: List<PatientMedicalRecord>,
    dentalRecords: List<PatientDentalRecord>,
    documents: List<PatientDocument>,
    notes: List<PatientNote>,
    prescriptions: List<Prescription>,
    followUps: List<FollowUp>,
    treatmentPlans: List<TreatmentPlan>,
): List<PatientTimelineEntry> {
    val entries = mutableListOf<PatientTimelineEntry>()

    appointments.forEach { a ->
        val ldt = parseAppointmentScheduledAt(a.scheduledAt)
        entries +=
            PatientTimelineEntry(
                id = 10_000_000L + a.id,
                timestamp = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                kind = TimelineEntryKind.APPOINTMENT,
                title = a.procedureTypeName ?: a.purpose ?: "Consulta",
                subtitle = "${a.doctorName} · ${a.status.displayLabel}",
                amountLabel = null,
                color = AppointmentPremiumPalette.primary,
            )
    }

    treatments.forEach { t ->
        val ldt = runCatching { parseAppointmentScheduledAt(t.actionAt) }.getOrNull()
        entries +=
            PatientTimelineEntry(
                id = 20_000_000L + t.id,
                timestamp = ldt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L,
                kind = TimelineEntryKind.TREATMENT,
                title = t.procedureTypeName,
                subtitle = "${t.doctorName} · ${t.status.labelEs}",
                amountLabel = t.totalPrice.takeIf { it > 0 }?.let { "Bs %.2f".format(it) },
                color = AppointmentPremiumPalette.success,
            )
    }

    payments.forEach { p ->
        val ldt = runCatching { parseAppointmentScheduledAt(p.paidAt) }.getOrNull()
        entries +=
            PatientTimelineEntry(
                id = 30_000_000L + p.id,
                timestamp = ldt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L,
                kind = TimelineEntryKind.PAYMENT,
                title = "Pago registrado",
                subtitle = p.method?.displayLabel ?: "Sin especificar",
                amountLabel = "Bs %.2f".format(p.amount),
                color = AppointmentPremiumPalette.info,
            )
    }

    medicalRecords.forEach { m ->
        entries +=
            PatientTimelineEntry(
                id = 40_000_000L + m.id,
                timestamp = isoDateToEpoch(m.recordedAt),
                kind = TimelineEntryKind.MEDICAL_RECORD,
                title = m.recordType.labelEs,
                subtitle = m.description,
                amountLabel = null,
                color = medicalRecordColor(m.recordType),
            )
    }

    dentalRecords.forEach { d ->
        entries +=
            PatientTimelineEntry(
                id = 50_000_000L + d.id,
                timestamp = isoDateToEpoch(d.recordedAt),
                kind = TimelineEntryKind.DENTAL_RECORD,
                title = d.toothNumber?.let { "Registro pieza $it" } ?: "Registro dental",
                subtitle = d.diagnosis,
                amountLabel = null,
                color = AppointmentPremiumPalette.warning,
            )
    }

    documents.forEach { doc ->
        entries +=
            PatientTimelineEntry(
                id = 60_000_000L + doc.id,
                timestamp = doc.uploadedAtEpochMs,
                kind = TimelineEntryKind.DOCUMENT,
                title = doc.title,
                subtitle = doc.category.labelEs,
                amountLabel = null,
                color = AppointmentPremiumPalette.textSecondary,
            )
    }

    notes.forEach { n ->
        entries +=
            PatientTimelineEntry(
                id = 70_000_000L + n.id,
                timestamp = n.createdAtEpochMs,
                kind = TimelineEntryKind.NOTE,
                title = if (n.isPinned) "Nota fijada" else "Nota",
                subtitle = n.body,
                amountLabel = null,
                color = AppointmentPremiumPalette.primary,
            )
    }

    prescriptions.forEach { rx ->
        entries +=
            PatientTimelineEntry(
                id = 80_000_000L + rx.id,
                timestamp = isoDateToEpoch(rx.prescribedAt),
                kind = TimelineEntryKind.PRESCRIPTION,
                title = rx.medicine,
                subtitle = "${rx.dosage} · ${rx.frequency}",
                amountLabel = null,
                color = Color(0xFF8B5CF6),
            )
    }

    followUps.forEach { f ->
        entries +=
            PatientTimelineEntry(
                id = 90_000_000L + f.id,
                timestamp = isoDateToEpoch(f.dueDate),
                kind = TimelineEntryKind.FOLLOW_UP,
                title = "Seguimiento",
                subtitle = f.notes ?: f.status.labelEs,
                amountLabel = null,
                color = AppointmentPremiumPalette.warning,
            )
    }

    treatmentPlans.forEach { plan ->
        entries +=
            PatientTimelineEntry(
                id = 100_000_000L + plan.id,
                timestamp = plan.createdAtEpochMs,
                kind = TimelineEntryKind.TREATMENT_PLAN,
                title = plan.title,
                subtitle = "${plan.status.labelEs} · ${plan.phases.size} fases",
                amountLabel = plan.estimatedCost.takeIf { it > 0 }?.let { "Bs %.2f".format(it) },
                color = AppointmentPremiumPalette.primary,
            )
    }

    return entries
        .filter { it.timestamp > 0L }
        .sortedByDescending { it.timestamp }
}

// ── Workspace aggregate state ──────────────────────────────────────────────

data class PatientWorkspaceUiState(
    val patient: PatientDetailUiModel,
    val kpis: PatientDetailKpis,
    val appointments: List<PatientDetailAppointmentUi>,
    val treatments: List<PatientTreatmentRow>,
    val payments: List<PatientDetailPaymentUi>,
    val paymentSummary: PaymentSummaryUiModel,
    val medicalRecords: List<PatientMedicalRecordUi>,
    val dentalRecords: List<PatientDentalRecordUi>,
    val documents: List<PatientDocumentUi>,
    val notes: List<PatientNoteUi>,
    val prescriptions: List<PatientPrescriptionUi>,
    val followUps: List<PatientFollowUpUi>,
    val treatmentPlans: List<PatientTreatmentPlanUi>,
    val timeline: List<PatientTimelineEntry>,
) {
    val pendingFollowUps: Int get() = followUps.count { it.status == FollowUpStatus.PENDING }
    val activePrescriptions: Int get() = prescriptions.count { it.status == PrescriptionStatus.ACTIVE }
    val activePlans: Int get() = treatmentPlans.count { it.status == TreatmentPlanStatus.ACTIVE }
}

fun buildPatientWorkspaceUiState(
    patientUi: PatientDetailUiModel,
    kpis: PatientDetailKpis,
    appointments: List<PatientDetailAppointmentUi>,
    treatments: List<PatientTreatmentRow>,
    payments: List<PatientDetailPaymentUi>,
    paymentSummary: PaymentSummaryUiModel,
    medicalRecords: List<PatientMedicalRecordUi>,
    dentalRecords: List<PatientDentalRecordUi>,
    documents: List<PatientDocumentUi>,
    notes: List<PatientNoteUi>,
    prescriptions: List<PatientPrescriptionUi>,
    followUps: List<PatientFollowUpUi>,
    treatmentPlans: List<PatientTreatmentPlanUi>,
    timeline: List<PatientTimelineEntry>,
): PatientWorkspaceUiState =
    PatientWorkspaceUiState(
        patient = patientUi,
        kpis = kpis,
        appointments = appointments,
        treatments = treatments,
        payments = payments,
        paymentSummary = paymentSummary,
        medicalRecords = medicalRecords,
        dentalRecords = dentalRecords,
        documents = documents,
        notes = notes,
        prescriptions = prescriptions,
        followUps = followUps,
        treatmentPlans = treatmentPlans,
        timeline = timeline,
    )

/** Conteo de tratamientos por estado para el resumen clínico. */
data class ClinicalSummaryCounts(
    val treatmentsByStatus: Map<TreatmentStatus, Int>,
) {
    val planned: Int get() = treatmentsByStatus[TreatmentStatus.PLANNED] ?: 0
    val inProgress: Int get() = treatmentsByStatus[TreatmentStatus.IN_PROGRESS] ?: 0
    val completed: Int get() = treatmentsByStatus[TreatmentStatus.COMPLETED] ?: 0
    val cancelled: Int get() = treatmentsByStatus[TreatmentStatus.CANCELLED] ?: 0
}

fun buildClinicalSummaryCounts(treatments: List<PatientTreatmentRow>): ClinicalSummaryCounts =
    ClinicalSummaryCounts(
        treatmentsByStatus = treatments.groupingBy { it.status }.eachCount(),
    )
