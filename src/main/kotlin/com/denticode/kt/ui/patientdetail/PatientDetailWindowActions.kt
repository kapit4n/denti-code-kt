package com.denticode.kt.ui.patientdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientClinicalProfile
import com.denticode.kt.data.PatientDentalRecord
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.PatientMedicalRecord
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.PatientTreatmentSettlement
import com.denticode.kt.data.PrescriptionStatus
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentPlan
import com.denticode.kt.data.TreatmentPlanPhase
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.data.TreatmentPaymentOption
import com.denticode.kt.data.visitStatusOptions
import com.denticode.kt.export.ExportService
import com.denticode.kt.export.PatientSummaryBundle
import com.denticode.kt.ui.app.AppMessenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado, carga y callbacks del workspace de detalle de paciente. */
class PatientDetailWindowActions(
    val repo: DentiRepository,
    val patient: Patient,
    internal val messenger: AppMessenger,
    internal val scope: CoroutineScope,
    internal val onClose: () -> Unit,
) {
    var appointments by mutableStateOf<List<AppointmentRow>>(emptyList())
    var treatments by mutableStateOf<List<PatientTreatmentRow>>(emptyList())
    var payments by mutableStateOf<List<PatientLedgerPayment>>(emptyList())
    var doctors by mutableStateOf<List<Doctor>>(emptyList())
    var procedureTypes by mutableStateOf<List<ProcedureTypeRow>>(emptyList())
    var clinicalProfile by mutableStateOf<PatientClinicalProfile?>(null)
    var treatmentPaymentOptions by mutableStateOf<List<TreatmentPaymentOption>>(emptyList())
    var treatmentSettlements by mutableStateOf<List<PatientTreatmentSettlement>>(emptyList())
    var paymentPrefillTreatmentId by mutableStateOf<Int?>(null)
    var treatmentPlans by mutableStateOf<List<TreatmentPlan>>(emptyList())
    var refreshNonce by mutableStateOf(0)
    var loaded by mutableStateOf(false)

    var showNewVisit by mutableStateOf(false)
    var showNewPayment by mutableStateOf(false)
    var showRegisterTreatment by mutableStateOf(false)
    var showMedicalRecord by mutableStateOf(false)
    var showDentalRecord by mutableStateOf(false)
    var showAddNote by mutableStateOf(false)
    var showAddPrescription by mutableStateOf(false)
    var showAddFollowUp by mutableStateOf(false)
    var showAddDocument by mutableStateOf(false)
    var showPlanDialog by mutableStateOf(false)
    var showPhaseDialog by mutableStateOf(false)
    var editingPlan by mutableStateOf<TreatmentPlan?>(null)
    var editingPhase by mutableStateOf<TreatmentPlanPhase?>(null)
    var phasePlanContext by mutableStateOf<TreatmentPlan?>(null)
    var editingMedical by mutableStateOf<PatientMedicalRecord?>(null)
    var editingDental by mutableStateOf<PatientDentalRecord?>(null)
    var confirmDelete by mutableStateOf<DeleteClinicalTarget?>(null)

    var saveVisitBusy by mutableStateOf(false)
    var savePaymentBusy by mutableStateOf(false)
    var saveTreatmentBusy by mutableStateOf(false)
    var saveMedicalBusy by mutableStateOf(false)
    var saveDentalBusy by mutableStateOf(false)
    var saveNoteBusy by mutableStateOf(false)
    var savePrescriptionBusy by mutableStateOf(false)
    var saveFollowUpBusy by mutableStateOf(false)
    var saveDocumentBusy by mutableStateOf(false)
    var savePlanBusy by mutableStateOf(false)
    var savePhaseBusy by mutableStateOf(false)
    var deleteClinicalBusy by mutableStateOf(false)

    var saveVisitError by mutableStateOf<String?>(null)
    var savePaymentError by mutableStateOf<String?>(null)
    var saveTreatmentError by mutableStateOf<String?>(null)
    var saveMedicalError by mutableStateOf<String?>(null)
    var saveDentalError by mutableStateOf<String?>(null)
    var saveNoteError by mutableStateOf<String?>(null)
    var savePrescriptionError by mutableStateOf<String?>(null)
    var saveFollowUpError by mutableStateOf<String?>(null)
    var saveDocumentError by mutableStateOf<String?>(null)
    var savePlanError by mutableStateOf<String?>(null)
    var savePhaseError by mutableStateOf<String?>(null)

    var showEditPatient by mutableStateOf(false)
    var editPatientBusy by mutableStateOf(false)
    var editPatientError by mutableStateOf<String?>(null)
    var showDeleteDialog by mutableStateOf(false)
    var archiveDeleteBusy by mutableStateOf(false)
    var selectedReceipt by mutableStateOf<PatientDetailPaymentUi?>(null)
    var receiptSaving by mutableStateOf(false)
    var exportBusy by mutableStateOf(false)

    val visitStatuses = visitStatusOptions()

    val activeDoctors: List<Doctor> get() = doctors.filter { it.isActive }

    val treatmentLinkOptions: List<TreatmentAppointmentLinkOption>
        get() = buildTreatmentAppointmentLinkOptions(appointments)

    val registerAppointmentEnabled: Boolean get() = activeDoctors.isNotEmpty()

    val registerTreatmentEnabled: Boolean
        get() = activeDoctors.isNotEmpty() && procedureTypes.isNotEmpty()

    suspend fun load() {
        withContext(Dispatchers.IO) {
            appointments = repo.listAppointmentsForPatient(patient.id)
            treatments = repo.listTreatmentsForPatient(patient.id)
            payments = repo.listPaymentsForPatient(patient.id)
            doctors = repo.listDoctors()
            procedureTypes = repo.listProcedureTypes()
            treatmentPaymentOptions = repo.listTreatmentPaymentOptionsForPatient(patient.id)
            treatmentSettlements = repo.listTreatmentSettlementsForPatient(patient.id)
            clinicalProfile = repo.loadPatientClinicalProfile(patient.id)
            treatmentPlans = repo.listTreatmentPlansForPatient(patient.id)
        }
        loaded = true
    }

    fun refresh() {
        refreshNonce++
    }

    fun rowModelById(uiId: Int): PatientMedicalRecord? =
        clinicalProfile?.medicalRecords?.find { it.id == uiId }

    fun dentalRowModelById(uiId: Int): PatientDentalRecord? =
        clinicalProfile?.dentalRecords?.find { it.id == uiId }

    fun buildWorkspaceUi(): PatientWorkspaceUiState {
        val kpis = buildPatientDetailKpis(appointments, payments, treatments)
        val medicalRows = clinicalProfile?.medicalRecords.orEmpty()
        val dentalRows = clinicalProfile?.dentalRecords.orEmpty()
        val documentRows = clinicalProfile?.documents.orEmpty()
        val noteRows = clinicalProfile?.notes.orEmpty()
        val prescriptionRows = clinicalProfile?.prescriptions.orEmpty()
        val followUpRows = clinicalProfile?.followUps.orEmpty()
        return buildPatientWorkspaceUiState(
            patientUi = patient.toDetailUiModel(appointments),
            kpis = kpis,
            appointments = appointments.map { it.toDetailUi() }.sortedByDescending { it.scheduledAt },
            treatments = treatments,
            payments = payments.map { it.toDetailUi() },
            paymentSummary = buildPaymentSummary(payments, kpis.pendingBalance),
            treatmentPayments = buildTreatmentPaymentUiState(treatmentSettlements),
            medicalRecords = medicalRows.map { it.toUi() },
            dentalRecords = dentalRows.map { it.toUi() },
            documents = documentRows.map { it.toUi() },
            notes = noteRows.map { it.toUi() },
            prescriptions = prescriptionRows.map { it.toUi() },
            followUps = followUpRows.map { it.toUi() },
            treatmentPlans = treatmentPlans.map { it.toUi() },
            timeline =
                buildPatientTimeline(
                    appointments,
                    treatments,
                    payments,
                    medicalRows,
                    dentalRows,
                    documentRows,
                    noteRows,
                    prescriptionRows,
                    followUpRows,
                    treatmentPlans,
                ),
        )
    }

    fun summaryBundle(): PatientSummaryBundle =
        PatientSummaryBundle(
            patient = patient,
            appointments = appointments,
            treatments = treatments,
            payments = payments,
            medicalRecords = clinicalProfile?.medicalRecords.orEmpty(),
            dentalRecords = clinicalProfile?.dentalRecords.orEmpty(),
            documents = clinicalProfile?.documents.orEmpty(),
            notes = clinicalProfile?.notes.orEmpty(),
            prescriptions = clinicalProfile?.prescriptions.orEmpty(),
            followUps = clinicalProfile?.followUps.orEmpty(),
            treatmentPlans = treatmentPlans,
        )

    internal fun safeFileName(): String =
        patient.fullName.trim().replace(Regex("[^\\p{L}\\p{N} ]"), "").replace(Regex("\\s+"), "_")

    val onRegisterAppointment: () -> Unit = {
        saveVisitError = null
        showNewVisit = true
    }

    val onRegisterTreatment: () -> Unit = {
        saveTreatmentError = null
        showRegisterTreatment = true
    }

    val onRegisterPayment: () -> Unit = {
        paymentPrefillTreatmentId = null
        savePaymentError = null
        showNewPayment = true
    }

    /** Abre el diálogo de pago precargado con el saldo del tratamiento indicado. */
    val onRegisterPaymentForTreatment: (PatientTreatmentSettlementUi) -> Unit = { settlement ->
        paymentPrefillTreatmentId = settlement.treatmentId
        savePaymentError = null
        showNewPayment = true
    }

    val onEditPatient: () -> Unit = {
        editPatientError = null
        showEditPatient = true
    }

    val onArchivePatient: () -> Unit = {
        scope.launch {
            archiveDeleteBusy = true
            runCatching {
                withContext(Dispatchers.IO) {
                    if (patient.isArchived) repo.restorePatient(patient.id)
                    else repo.archivePatient(patient.id)
                }
            }.onSuccess {
                refreshNonce++
                messenger.showSuccess(if (patient.isArchived) "Paciente restaurado." else "Paciente archivado.")
            }.onFailure { e ->
                messenger.showError(e.message ?: "Error al archivar/restaurar.")
            }
            archiveDeleteBusy = false
        }
    }

    val onDeletePatient: () -> Unit = {
        showDeleteDialog = true
    }

    val onAddMedical: () -> Unit = {
        editingMedical = null
        saveMedicalError = null
        showMedicalRecord = true
    }

    val onEditMedical: (PatientMedicalRecordUi) -> Unit = { ui ->
        editingMedical = rowModelById(ui.id)
        saveMedicalError = null
        showMedicalRecord = true
    }

    val onDeleteMedical: (PatientMedicalRecordUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.MEDICAL,
                id = ui.id,
                title = "Eliminar registro médico",
                message = "¿Eliminar el registro «${ui.recordTypeLabel}» de este paciente?",
            )
    }

    val onAddDental: () -> Unit = {
        editingDental = null
        saveDentalError = null
        showDentalRecord = true
    }

    val onEditDental: (PatientDentalRecordUi) -> Unit = { ui ->
        editingDental = dentalRowModelById(ui.id)
        saveDentalError = null
        showDentalRecord = true
    }

    val onDeleteDental: (PatientDentalRecordUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.DENTAL,
                id = ui.id,
                title = "Eliminar registro dental",
                message = "¿Eliminar el registro de la pieza ${ui.toothLabel}?",
            )
    }

    val onAddNote: () -> Unit = {
        saveNoteError = null
        showAddNote = true
    }

    val onTogglePinNote: (PatientNoteUi) -> Unit = { ui ->
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repo.togglePinPatientNote(ui.id) }
            }.onSuccess {
                refreshNonce++
            }.onFailure { e ->
                messenger.showError(e.message ?: "Error al cambiar el estado de la nota.")
            }
        }
    }

    val onDeleteNote: (PatientNoteUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.NOTE,
                id = ui.id,
                title = "Eliminar nota",
                message = "¿Eliminar esta nota del paciente?",
            )
    }

    val onAddPrescription: () -> Unit = {
        savePrescriptionError = null
        showAddPrescription = true
    }

    val onPrescriptionStatusChange: (PatientPrescriptionUi, PrescriptionStatus) -> Unit = { ui, status ->
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repo.updatePrescriptionStatus(ui.id, status) }
            }.onSuccess {
                refreshNonce++
            }.onFailure { e ->
                messenger.showError(e.message ?: "Error al actualizar la receta.")
            }
        }
    }

    val onDeletePrescription: (PatientPrescriptionUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.PRESCRIPTION,
                id = ui.id,
                title = "Eliminar receta",
                message = "¿Eliminar la receta de «${ui.medicine}»?",
            )
    }

    val onAddFollowUp: () -> Unit = {
        saveFollowUpError = null
        showAddFollowUp = true
    }

    val onFollowUpStatusChange: (PatientFollowUpUi, FollowUpStatus) -> Unit = { ui, status ->
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repo.updateFollowUpStatus(ui.id, status) }
            }.onSuccess {
                refreshNonce++
            }.onFailure { e ->
                messenger.showError(e.message ?: "Error al actualizar el seguimiento.")
            }
        }
    }

    val onDeleteFollowUp: (PatientFollowUpUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.FOLLOW_UP,
                id = ui.id,
                title = "Eliminar seguimiento",
                message = "¿Eliminar este seguimiento del paciente?",
            )
    }

    val onUploadDocument: () -> Unit = {
        saveDocumentError = null
        showAddDocument = true
    }

    val onOpenDocument: (PatientDocumentUi) -> Unit = { doc ->
        val path = doc.filePath
        if (path.isNullOrBlank()) {
            messenger.showSuccess("Este documento no tiene un archivo físico vinculado.")
        } else {
            val file = java.io.File(path)
            if (!file.exists()) {
                messenger.showError("El archivo físico no existe (${file.name}).")
            } else {
                ExportService.openFile(file)
                messenger.showSuccess("Abriendo ${doc.fileName}…")
            }
        }
    }

    val onDeleteDocument: (PatientDocumentUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.DOCUMENT,
                id = ui.id,
                title = "Eliminar documento",
                message = "¿Eliminar el documento «${ui.title}»? Se borrará también su archivo físico.",
                filePath = ui.filePath,
            )
    }

    val onAddPlan: () -> Unit = {
        editingPlan = null
        savePlanError = null
        showPlanDialog = true
    }

    val onEditPlan: (PatientTreatmentPlanUi) -> Unit = { ui ->
        editingPlan = treatmentPlans.find { it.id == ui.id }
        savePlanError = null
        showPlanDialog = true
    }

    val onDeletePlan: (PatientTreatmentPlanUi) -> Unit = { ui ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.TREATMENT_PLAN,
                id = ui.id,
                title = "Eliminar plan de tratamiento",
                message = "¿Eliminar el plan «${ui.title}» y todas sus fases?",
            )
    }

    val onPlanStatusChange: (PatientTreatmentPlanUi, TreatmentPlanStatus) -> Unit = { ui, status ->
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repo.updateTreatmentPlanStatus(ui.id, status) }
            }.onSuccess {
                refreshNonce++
            }.onFailure { e ->
                messenger.showError(e.message ?: "Error al actualizar el plan.")
            }
        }
    }

    val onAddPhase: (PatientTreatmentPlanUi) -> Unit = { plan ->
        phasePlanContext = treatmentPlans.find { it.id == plan.id }
        editingPhase = null
        savePhaseError = null
        showPhaseDialog = true
    }

    val onEditPhase: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi) -> Unit = { plan, phase ->
        phasePlanContext = treatmentPlans.find { it.id == plan.id }
        editingPhase =
            treatmentPlans.asSequence()
                .flatMap { it.phases.asSequence() }
                .find { it.id == phase.id }
        savePhaseError = null
        showPhaseDialog = true
    }

    val onDeletePhase: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi) -> Unit = { plan, phase ->
        confirmDelete =
            DeleteClinicalTarget(
                kind = ClinicalDeleteKind.TREATMENT_PLAN_PHASE,
                id = phase.id,
                title = "Eliminar fase",
                message = "¿Eliminar la fase «${phase.name}» del plan «${plan.title}»?",
            )
    }

    val onPhaseStatusChange: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi, TreatmentPlanPhaseStatus) -> Unit =
        { _, phase, status ->
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) { repo.updateTreatmentPlanPhaseStatus(phase.id, status) }
                }.onSuccess {
                    refreshNonce++
                }.onFailure { e ->
                    messenger.showError(e.message ?: "Error al actualizar la fase.")
                }
            }
        }

    val onViewReceipt: (PatientDetailPaymentUi) -> Unit = { selectedReceipt = it }

    val onExportFicha: () -> Unit = { exportFicha() }

    val onPrintSummary: () -> Unit = { printSummary() }
}
