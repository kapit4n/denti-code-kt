@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.DentalRecordRegisterRequest
import com.denticode.kt.data.DentalRecordUpdateRequest
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.FollowUpRegisterRequest
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.MedicalRecordRegisterRequest
import com.denticode.kt.data.MedicalRecordUpdateRequest
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientClinicalProfile
import com.denticode.kt.data.PatientDentalRecord
import com.denticode.kt.data.PatientDocumentRegisterRequest
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.PatientMedicalRecord
import com.denticode.kt.data.PatientNoteRegisterRequest
import com.denticode.kt.data.PrescriptionRegisterRequest
import com.denticode.kt.data.PrescriptionStatus
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentPlan
import com.denticode.kt.data.TreatmentPlanPhase
import com.denticode.kt.data.TreatmentPlanPhaseRegisterRequest
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanPhaseUpdateRequest
import com.denticode.kt.data.TreatmentPlanRegisterRequest
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.data.TreatmentPlanUpdateRequest
import com.denticode.kt.data.visitStatusOptions
import com.denticode.kt.export.DocumentStore
import com.denticode.kt.export.ExportService
import com.denticode.kt.export.PatientSummaryBundle
import com.denticode.kt.export.renderPatientSummaryHtml
import com.denticode.kt.export.renderPatientSummaryText
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.patientdetail.ClinicalDeleteKind
import com.denticode.kt.ui.patientdetail.DeleteClinicalTarget
import com.denticode.kt.ui.patientdetail.FollowUpDialog
import com.denticode.kt.ui.patientdetail.MedicalRecordDialog
import com.denticode.kt.ui.patientdetail.ModernPatientDetailContent
import com.denticode.kt.ui.patientdetail.PatientDetailFocusSection
import com.denticode.kt.ui.patientdetail.PatientDocumentDialog
import com.denticode.kt.ui.patientdetail.PatientDetailPaymentUi
import com.denticode.kt.ui.patientdetail.PatientNoteDialog
import com.denticode.kt.ui.patientdetail.PatientNewPaymentDialog
import com.denticode.kt.ui.patientdetail.PatientNewVisitDialog
import com.denticode.kt.ui.patientdetail.PrescriptionDialog
import com.denticode.kt.ui.patientdetail.RegisterTreatmentDialog
import com.denticode.kt.ui.patientdetail.DentalRecordDialog
import com.denticode.kt.ui.patientdetail.DeleteClinicalConfirmDialog
import com.denticode.kt.ui.patientdetail.TreatmentPlanDialog
import com.denticode.kt.ui.patientdetail.TreatmentPlanPhaseDialog
import com.denticode.kt.ui.patientdetail.buildPaymentSummary
import com.denticode.kt.ui.patientdetail.buildPatientDetailKpis
import com.denticode.kt.ui.patientdetail.buildPatientTimeline
import com.denticode.kt.ui.patientdetail.buildPatientWorkspaceUiState
import com.denticode.kt.ui.patientdetail.buildTreatmentAppointmentLinkOptions
import com.denticode.kt.ui.patientdetail.toDetailUi
import com.denticode.kt.ui.patientdetail.toDetailUiModel
import com.denticode.kt.ui.patientdetail.toReceiptData
import com.denticode.kt.ui.patientdetail.toUi
import com.denticode.kt.ui.payments.PaymentReceiptDialog
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.data.PatientTreatmentRegisterRequest
import com.denticode.kt.data.PatientUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PatientDetailWindow(
    repo: DentiRepository,
    patient: Patient,
    focusSection: PatientDetailFocusSection = PatientDetailFocusSection.OVERVIEW,
    onClose: () -> Unit,
) {
    val visitStatuses = remember { visitStatusOptions() }
    val scope = rememberCoroutineScope()
    var appointments by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    var treatments by remember { mutableStateOf<List<com.denticode.kt.data.PatientTreatmentRow>>(emptyList()) }
    var payments by remember { mutableStateOf<List<PatientLedgerPayment>>(emptyList()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var procedureTypes by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    var clinicalProfile by remember { mutableStateOf<PatientClinicalProfile?>(null) }
    var treatmentPaymentOptions by remember { mutableStateOf<List<com.denticode.kt.data.TreatmentPaymentOption>>(emptyList()) }
    var treatmentPlans by remember { mutableStateOf<List<TreatmentPlan>>(emptyList()) }
    var refreshNonce by remember { mutableStateOf(0) }
    var showNewVisit by remember { mutableStateOf(false) }
    var showNewPayment by remember { mutableStateOf(false) }
    var showRegisterTreatment by remember { mutableStateOf(false) }
    var showMedicalRecord by remember { mutableStateOf(false) }
    var showDentalRecord by remember { mutableStateOf(false) }
    var showAddNote by remember { mutableStateOf(false) }
    var showAddPrescription by remember { mutableStateOf(false) }
    var showAddFollowUp by remember { mutableStateOf(false) }
    var showAddDocument by remember { mutableStateOf(false) }
    var showPlanDialog by remember { mutableStateOf(false) }
    var showPhaseDialog by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<TreatmentPlan?>(null) }
    var editingPhase by remember { mutableStateOf<TreatmentPlanPhase?>(null) }
    var phasePlanContext by remember { mutableStateOf<TreatmentPlan?>(null) }
    var editingMedical by remember { mutableStateOf<PatientMedicalRecord?>(null) }
    var editingDental by remember { mutableStateOf<PatientDentalRecord?>(null) }
    var confirmDelete by remember { mutableStateOf<DeleteClinicalTarget?>(null) }
    var saveVisitBusy by remember { mutableStateOf(false) }
    var savePaymentBusy by remember { mutableStateOf(false) }
    var saveTreatmentBusy by remember { mutableStateOf(false) }
    var saveMedicalBusy by remember { mutableStateOf(false) }
    var saveDentalBusy by remember { mutableStateOf(false) }
    var saveNoteBusy by remember { mutableStateOf(false) }
    var savePrescriptionBusy by remember { mutableStateOf(false) }
    var saveFollowUpBusy by remember { mutableStateOf(false) }
    var saveDocumentBusy by remember { mutableStateOf(false) }
    var savePlanBusy by remember { mutableStateOf(false) }
    var savePhaseBusy by remember { mutableStateOf(false) }
    var deleteClinicalBusy by remember { mutableStateOf(false) }
    var saveVisitError by remember { mutableStateOf<String?>(null) }
    var savePaymentError by remember { mutableStateOf<String?>(null) }
    var saveTreatmentError by remember { mutableStateOf<String?>(null) }
    var saveMedicalError by remember { mutableStateOf<String?>(null) }
    var saveDentalError by remember { mutableStateOf<String?>(null) }
    var saveNoteError by remember { mutableStateOf<String?>(null) }
    var savePrescriptionError by remember { mutableStateOf<String?>(null) }
    var saveFollowUpError by remember { mutableStateOf<String?>(null) }
    var saveDocumentError by remember { mutableStateOf<String?>(null) }
    var savePlanError by remember { mutableStateOf<String?>(null) }
    var savePhaseError by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var showEditPatient by remember { mutableStateOf(false) }
    var editPatientBusy by remember { mutableStateOf(false) }
    var editPatientError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var archiveDeleteBusy by remember { mutableStateOf(false) }
    var selectedReceipt by remember { mutableStateOf<PatientDetailPaymentUi?>(null) }
    var receiptSaving by remember { mutableStateOf(false) }
    var exportBusy by remember { mutableStateOf(false) }
    val messenger = LocalAppMessenger.current

    LaunchedEffect(patient.id, refreshNonce) {
        withContext(Dispatchers.IO) {
            appointments = repo.listAppointmentsForPatient(patient.id)
            treatments = repo.listTreatmentsForPatient(patient.id)
            payments = repo.listPaymentsForPatient(patient.id)
            doctors = repo.listDoctors()
            procedureTypes = repo.listProcedureTypes()
            treatmentPaymentOptions = repo.listTreatmentPaymentOptionsForPatient(patient.id)
            clinicalProfile = repo.loadPatientClinicalProfile(patient.id)
            treatmentPlans = repo.listTreatmentPlansForPatient(patient.id)
        }
        loaded = true
    }

    val activeDoctors = remember(doctors) { doctors.filter { it.isActive } }
    val treatmentLinkOptions = remember(appointments) { buildTreatmentAppointmentLinkOptions(appointments) }

    val workspaceUi =
        remember(patient, appointments, treatments, payments, clinicalProfile, treatmentPlans) {
            val kpis = buildPatientDetailKpis(appointments, payments, treatments)
            val medicalRows = clinicalProfile?.medicalRecords.orEmpty()
            val dentalRows = clinicalProfile?.dentalRecords.orEmpty()
            val documentRows = clinicalProfile?.documents.orEmpty()
            val noteRows = clinicalProfile?.notes.orEmpty()
            val prescriptionRows = clinicalProfile?.prescriptions.orEmpty()
            val followUpRows = clinicalProfile?.followUps.orEmpty()
            buildPatientWorkspaceUiState(
                patientUi = patient.toDetailUiModel(appointments),
                kpis = kpis,
                appointments = appointments.map { it.toDetailUi() }.sortedByDescending { it.scheduledAt },
                treatments = treatments,
                payments = payments.map { it.toDetailUi() },
                paymentSummary = buildPaymentSummary(payments, kpis.pendingBalance),
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

    fun rowModelById(uiId: Int): PatientMedicalRecord? =
        clinicalProfile?.medicalRecords?.find { it.id == uiId }

    fun dentalRowModelById(uiId: Int): PatientDentalRecord? =
        clinicalProfile?.dentalRecords?.find { it.id == uiId }

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

    fun safeFileName(): String =
        patient.fullName.trim().replace(Regex("[^\\p{L}\\p{N} ]"), "").replace(Regex("\\s+"), "_")

    fun exportFicha() {
        exportBusy = true
        scope.launch {
            val file =
                ExportService.pickSaveFile("ficha_${safeFileName()}.html") ?: run {
                    messenger.showSuccess("Exportación cancelada.")
                    exportBusy = false
                    return@launch
                }
            runCatching {
                withContext(Dispatchers.IO) {
                    ExportService.writeTextFile(file, renderPatientSummaryHtml(summaryBundle()))
                }
            }.onSuccess {
                messenger.showSuccess("Ficha exportada: ${file.name}")
                ExportService.openFile(file)
            }.onFailure { e ->
                messenger.showError(e.message ?: "No se pudo exportar la ficha.")
            }
            exportBusy = false
        }
    }

    fun printSummary() {
        exportBusy = true
        scope.launch {
            val file =
                ExportService.pickSaveFile("resumen_${safeFileName()}.txt") ?: run {
                    messenger.showSuccess("Exportación cancelada.")
                    exportBusy = false
                    return@launch
                }
            runCatching {
                withContext(Dispatchers.IO) {
                    ExportService.writeTextFile(file, renderPatientSummaryText(summaryBundle()))
                }
            }.onSuccess {
                messenger.showSuccess("Resumen guardado: ${file.name}")
                ExportService.openFile(file)
            }.onFailure { e ->
                messenger.showError(e.message ?: "No se pudo guardar el resumen.")
            }
            exportBusy = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PatientsPremiumPalette.background,
    ) {
        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            ModernPatientDetailContent(
            workspace = workspaceUi,
            focusSection = focusSection,
            onClose = onClose,
            onRegisterAppointment = {
                saveVisitError = null
                showNewVisit = true
            },
            onRegisterTreatment = {
                saveTreatmentError = null
                showRegisterTreatment = true
            },
            onRegisterPayment = {
                savePaymentError = null
                showNewPayment = true
            },
            registerAppointmentEnabled = activeDoctors.isNotEmpty(),
            registerTreatmentEnabled = activeDoctors.isNotEmpty() && procedureTypes.isNotEmpty(),
            onEditPatient = {
                editPatientError = null
                showEditPatient = true
            },
            onArchivePatient = {
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
            },
            onDeletePatient = {
                showDeleteDialog = true
            },
            isArchived = patient.isArchived,
            onAddMedical = {
                editingMedical = null
                saveMedicalError = null
                showMedicalRecord = true
            },
            onEditMedical = { ui ->
                editingMedical = rowModelById(ui.id)
                saveMedicalError = null
                showMedicalRecord = true
            },
            onDeleteMedical = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.MEDICAL,
                        id = ui.id,
                        title = "Eliminar registro médico",
                        message = "¿Eliminar el registro «${ui.recordTypeLabel}» de este paciente?",
                    )
            },
            onAddDental = {
                editingDental = null
                saveDentalError = null
                showDentalRecord = true
            },
            onEditDental = { ui ->
                editingDental = dentalRowModelById(ui.id)
                saveDentalError = null
                showDentalRecord = true
            },
            onDeleteDental = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.DENTAL,
                        id = ui.id,
                        title = "Eliminar registro dental",
                        message = "¿Eliminar el registro de la pieza ${ui.toothLabel}?",
                    )
            },
            onAddNote = {
                saveNoteError = null
                showAddNote = true
            },
            onTogglePinNote = { ui ->
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { repo.togglePinPatientNote(ui.id) }
                    }.onSuccess {
                        refreshNonce++
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al cambiar el estado de la nota.")
                    }
                }
            },
            onDeleteNote = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.NOTE,
                        id = ui.id,
                        title = "Eliminar nota",
                        message = "¿Eliminar esta nota del paciente?",
                    )
            },
            onAddPrescription = {
                savePrescriptionError = null
                showAddPrescription = true
            },
            onPrescriptionStatusChange = { ui, status ->
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { repo.updatePrescriptionStatus(ui.id, status) }
                    }.onSuccess {
                        refreshNonce++
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al actualizar la receta.")
                    }
                }
            },
            onDeletePrescription = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.PRESCRIPTION,
                        id = ui.id,
                        title = "Eliminar receta",
                        message = "¿Eliminar la receta de «${ui.medicine}»?",
                    )
            },
            onAddFollowUp = {
                saveFollowUpError = null
                showAddFollowUp = true
            },
            onFollowUpStatusChange = { ui, status ->
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { repo.updateFollowUpStatus(ui.id, status) }
                    }.onSuccess {
                        refreshNonce++
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al actualizar el seguimiento.")
                    }
                }
            },
            onDeleteFollowUp = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.FOLLOW_UP,
                        id = ui.id,
                        title = "Eliminar seguimiento",
                        message = "¿Eliminar este seguimiento del paciente?",
                    )
            },
            onUploadDocument = {
                saveDocumentError = null
                showAddDocument = true
            },
            onOpenDocument = { doc ->
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
            },
            onDeleteDocument = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.DOCUMENT,
                        id = ui.id,
                        title = "Eliminar documento",
                        message = "¿Eliminar el documento «${ui.title}»? Se borrará también su archivo físico.",
                        filePath = ui.filePath,
                    )
            },
            onAddPlan = {
                editingPlan = null
                savePlanError = null
                showPlanDialog = true
            },
            onEditPlan = { ui ->
                editingPlan = treatmentPlans.find { it.id == ui.id }
                savePlanError = null
                showPlanDialog = true
            },
            onDeletePlan = { ui ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.TREATMENT_PLAN,
                        id = ui.id,
                        title = "Eliminar plan de tratamiento",
                        message = "¿Eliminar el plan «${ui.title}» y todas sus fases?",
                    )
            },
            onPlanStatusChange = { ui, status ->
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { repo.updateTreatmentPlanStatus(ui.id, status) }
                    }.onSuccess {
                        refreshNonce++
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al actualizar el plan.")
                    }
                }
            },
            onAddPhase = { plan ->
                phasePlanContext = treatmentPlans.find { it.id == plan.id }
                editingPhase = null
                savePhaseError = null
                showPhaseDialog = true
            },
            onEditPhase = { plan, phase ->
                phasePlanContext = treatmentPlans.find { it.id == plan.id }
                editingPhase = treatmentPlans.asSequence()
                    .flatMap { it.phases.asSequence() }
                    .find { it.id == phase.id }
                savePhaseError = null
                showPhaseDialog = true
            },
            onDeletePhase = { plan, phase ->
                confirmDelete =
                    DeleteClinicalTarget(
                        kind = ClinicalDeleteKind.TREATMENT_PLAN_PHASE,
                        id = phase.id,
                        title = "Eliminar fase",
                        message = "¿Eliminar la fase «${phase.name}» del plan «${plan.title}»?",
                    )
            },
            onPhaseStatusChange = { _, phase, status ->
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { repo.updateTreatmentPlanPhaseStatus(phase.id, status) }
                    }.onSuccess {
                        refreshNonce++
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al actualizar la fase.")
                    }
                }
            },
            onViewReceipt = { selectedReceipt = it },
            onExportFicha = { exportFicha() },
            onPrintSummary = { printSummary() },
            )
        }
    }

    if (showNewVisit) {
        PatientNewVisitDialog(
            patient = patient,
            doctors = activeDoctors,
            procedureTypes = procedureTypes,
            visitStatuses = visitStatuses,
            isSaving = saveVisitBusy,
            errorMessage = saveVisitError,
            onDismiss = {
                if (!saveVisitBusy) {
                    showNewVisit = false
                    saveVisitError = null
                }
            },
            onSubmit = { request ->
                saveVisitBusy = true
                saveVisitError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.createAppointment(request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showNewVisit = false
                    }.onFailure { e ->
                        saveVisitError = e.message ?: "No se pudo registrar la cita."
                    }
                    saveVisitBusy = false
                }
            },
        )
    }

    if (showNewPayment) {
        PatientNewPaymentDialog(
            procedureTypes = procedureTypes,
            treatmentOptions = treatmentPaymentOptions,
            isSaving = savePaymentBusy,
            errorMessage = savePaymentError,
            onDismiss = {
                if (!savePaymentBusy) {
                    showNewPayment = false
                    savePaymentError = null
                }
            },
            onSubmit = { req ->
                savePaymentBusy = true
                savePaymentError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerPaymentForPatient(patient.id, req)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showNewPayment = false
                    }.onFailure { e ->
                        savePaymentError = e.message ?: "No se pudo registrar el pago."
                    }
                    savePaymentBusy = false
                }
            },
        )
    }

    if (showRegisterTreatment) {
        RegisterTreatmentDialog(
            patientLabel = "Paciente: ${patient.fullName}",
            doctors = activeDoctors,
            procedureTypes = procedureTypes,
            appointmentLinkOptions = treatmentLinkOptions,
            isSaving = saveTreatmentBusy,
            errorMessage = saveTreatmentError,
            onDismiss = {
                if (!saveTreatmentBusy) {
                    showRegisterTreatment = false
                    saveTreatmentError = null
                }
            },
            onSubmit = { request ->
                saveTreatmentBusy = true
                saveTreatmentError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerTreatmentForPatient(request.copy(patientId = patient.id))
                        }
                    }.onSuccess {
                        refreshNonce++
                        showRegisterTreatment = false
                        messenger.showSuccess("Tratamiento registrado correctamente.")
                    }.onFailure { e ->
                        saveTreatmentError = e.message ?: "No se pudo registrar el tratamiento."
                    }
                    saveTreatmentBusy = false
                }
            },
        )
    }

    if (showMedicalRecord) {
        MedicalRecordDialog(
            initial = editingMedical,
            doctors = activeDoctors,
            isSaving = saveMedicalBusy,
            errorMessage = saveMedicalError,
            onDismiss = {
                if (!saveMedicalBusy) {
                    showMedicalRecord = false
                    editingMedical = null
                    saveMedicalError = null
                }
            },
            onSubmit = { request ->
                val editing = editingMedical
                saveMedicalBusy = true
                saveMedicalError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            if (editing == null) {
                                repo.registerMedicalRecord(patient.id, request)
                            } else {
                                repo.updateMedicalRecord(
                                    editing.id,
                                    MedicalRecordUpdateRequest(
                                        recordType = request.recordType,
                                        description = request.description,
                                        recordedAt = request.recordedAt,
                                        doctorId = request.doctorId,
                                        isActive = request.isActive,
                                        notes = request.notes,
                                    ),
                                )
                            }
                        }
                    }.onSuccess {
                        refreshNonce++
                        showMedicalRecord = false
                        editingMedical = null
                        messenger.showSuccess(if (editing == null) "Registro médico agregado." else "Registro médico actualizado.")
                    }.onFailure { e ->
                        saveMedicalError = e.message ?: "No se pudo guardar el registro médico."
                    }
                    saveMedicalBusy = false
                }
            },
        )
    }

    if (showDentalRecord) {
        DentalRecordDialog(
            initial = editingDental,
            doctors = activeDoctors,
            procedureTypes = procedureTypes,
            isSaving = saveDentalBusy,
            errorMessage = saveDentalError,
            onDismiss = {
                if (!saveDentalBusy) {
                    showDentalRecord = false
                    editingDental = null
                    saveDentalError = null
                }
            },
            onSubmit = { request ->
                val editing = editingDental
                saveDentalBusy = true
                saveDentalError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            if (editing == null) {
                                repo.registerDentalRecord(patient.id, request)
                            } else {
                                repo.updateDentalRecord(
                                    editing.id,
                                    DentalRecordUpdateRequest(
                                        toothNumber = request.toothNumber,
                                        toothQuadrant = request.toothQuadrant,
                                        diagnosis = request.diagnosis,
                                        treatmentPerformed = request.treatmentPerformed,
                                        procedureTypeId = request.procedureTypeId,
                                        recordedAt = request.recordedAt,
                                        doctorId = request.doctorId,
                                        notes = request.notes,
                                    ),
                                )
                            }
                        }
                    }.onSuccess {
                        refreshNonce++
                        showDentalRecord = false
                        editingDental = null
                        messenger.showSuccess(if (editing == null) "Registro dental agregado." else "Registro dental actualizado.")
                    }.onFailure { e ->
                        saveDentalError = e.message ?: "No se pudo guardar el registro dental."
                    }
                    saveDentalBusy = false
                }
            },
        )
    }

    if (showAddNote) {
        PatientNoteDialog(
            isSaving = saveNoteBusy,
            errorMessage = saveNoteError,
            onDismiss = {
                if (!saveNoteBusy) {
                    showAddNote = false
                    saveNoteError = null
                }
            },
            onSubmit = { request ->
                saveNoteBusy = true
                saveNoteError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.addPatientNote(patient.id, request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showAddNote = false
                        messenger.showSuccess("Nota guardada.")
                    }.onFailure { e ->
                        saveNoteError = e.message ?: "No se pudo guardar la nota."
                    }
                    saveNoteBusy = false
                }
            },
        )
    }

    if (showAddPrescription) {
        PrescriptionDialog(
            doctors = activeDoctors,
            isSaving = savePrescriptionBusy,
            errorMessage = savePrescriptionError,
            onDismiss = {
                if (!savePrescriptionBusy) {
                    showAddPrescription = false
                    savePrescriptionError = null
                }
            },
            onSubmit = { request ->
                savePrescriptionBusy = true
                savePrescriptionError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerPrescription(patient.id, request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showAddPrescription = false
                        messenger.showSuccess("Receta registrada.")
                    }.onFailure { e ->
                        savePrescriptionError = e.message ?: "No se pudo registrar la receta."
                    }
                    savePrescriptionBusy = false
                }
            },
        )
    }

    if (showAddFollowUp) {
        FollowUpDialog(
            appointmentLinkOptions = treatmentLinkOptions,
            isSaving = saveFollowUpBusy,
            errorMessage = saveFollowUpError,
            onDismiss = {
                if (!saveFollowUpBusy) {
                    showAddFollowUp = false
                    saveFollowUpError = null
                }
            },
            onSubmit = { request ->
                saveFollowUpBusy = true
                saveFollowUpError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerFollowUp(patient.id, request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showAddFollowUp = false
                        messenger.showSuccess("Seguimiento registrado.")
                    }.onFailure { e ->
                        saveFollowUpError = e.message ?: "No se pudo registrar el seguimiento."
                    }
                    saveFollowUpBusy = false
                }
            },
        )
    }

    if (showAddDocument) {
        PatientDocumentDialog(
            patientId = patient.id,
            isSaving = saveDocumentBusy,
            errorMessage = saveDocumentError,
            onDismiss = {
                if (!saveDocumentBusy) {
                    showAddDocument = false
                    saveDocumentError = null
                }
            },
            onSubmit = { request ->
                saveDocumentBusy = true
                saveDocumentError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerPatientDocument(patient.id, request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showAddDocument = false
                        messenger.showSuccess("Documento registrado.")
                    }.onFailure { e ->
                        saveDocumentError = e.message ?: "No se pudo registrar el documento."
                    }
                    saveDocumentBusy = false
                }
            },
        )
    }

    if (showPlanDialog) {
        TreatmentPlanDialog(
            initial = editingPlan,
            isSaving = savePlanBusy,
            errorMessage = savePlanError,
            onDismiss = {
                if (!savePlanBusy) {
                    showPlanDialog = false
                    editingPlan = null
                    savePlanError = null
                }
            },
            onSubmit = { request ->
                val editing = editingPlan
                savePlanBusy = true
                savePlanError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            if (editing == null) {
                                repo.registerTreatmentPlan(patient.id, request)
                            } else {
                                repo.updateTreatmentPlan(
                                    editing.id,
                                    TreatmentPlanUpdateRequest(
                                        title = request.title,
                                        description = request.description,
                                        status = request.status,
                                    ),
                                )
                            }
                        }
                    }.onSuccess {
                        refreshNonce++
                        showPlanDialog = false
                        editingPlan = null
                        messenger.showSuccess(if (editing == null) "Plan de tratamiento creado." else "Plan de tratamiento actualizado.")
                    }.onFailure { e ->
                        savePlanError = e.message ?: "No se pudo guardar el plan."
                    }
                    savePlanBusy = false
                }
            },
        )
    }

    if (showPhaseDialog) {
        TreatmentPlanPhaseDialog(
            initial = editingPhase,
            isSaving = savePhaseBusy,
            errorMessage = savePhaseError,
            onDismiss = {
                if (!savePhaseBusy) {
                    showPhaseDialog = false
                    editingPhase = null
                    phasePlanContext = null
                    savePhaseError = null
                }
            },
            onSubmit = { request ->
                val editing = editingPhase
                val contextPlan = phasePlanContext
                savePhaseBusy = true
                savePhaseError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            if (editing == null) {
                                contextPlan ?: throw IllegalStateException("Plan de contexto no disponible.")
                                repo.addTreatmentPlanPhase(contextPlan.id, request)
                            } else {
                                repo.updateTreatmentPlanPhase(
                                    editing.id,
                                    TreatmentPlanPhaseUpdateRequest(
                                        name = request.name,
                                        description = request.description,
                                        estimatedCost = request.estimatedCost,
                                        status = request.status,
                                    ),
                                )
                            }
                        }
                    }.onSuccess {
                        refreshNonce++
                        showPhaseDialog = false
                        editingPhase = null
                        phasePlanContext = null
                        messenger.showSuccess(if (editing == null) "Fase agregada." else "Fase actualizada.")
                    }.onFailure { e ->
                        savePhaseError = e.message ?: "No se pudo guardar la fase."
                    }
                    savePhaseBusy = false
                }
            },
        )
    }

    confirmDelete?.let { target ->
        DeleteClinicalConfirmDialog(
            target = target,
            isSaving = deleteClinicalBusy,
            onDismiss = {
                if (!deleteClinicalBusy) {
                    confirmDelete = null
                }
            },
            onConfirm = {
                deleteClinicalBusy = true
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            when (target.kind) {
                                ClinicalDeleteKind.MEDICAL -> repo.deleteMedicalRecord(target.id)
                                ClinicalDeleteKind.DENTAL -> repo.deleteDentalRecord(target.id)
                                ClinicalDeleteKind.NOTE -> repo.deletePatientNote(target.id)
                                ClinicalDeleteKind.PRESCRIPTION -> repo.deletePrescription(target.id)
                                ClinicalDeleteKind.FOLLOW_UP -> repo.deleteFollowUp(target.id)
                                ClinicalDeleteKind.DOCUMENT -> {
                                    DocumentStore.delete(target.filePath)
                                    repo.deletePatientDocument(target.id)
                                }
                                ClinicalDeleteKind.TREATMENT_PLAN -> repo.deleteTreatmentPlan(target.id)
                                ClinicalDeleteKind.TREATMENT_PLAN_PHASE -> repo.deleteTreatmentPlanPhase(target.id)
                            }
                        }
                    }.onSuccess {
                        confirmDelete = null
                        refreshNonce++
                        messenger.showSuccess("Elemento eliminado.")
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al eliminar.")
                    }
                    deleteClinicalBusy = false
                }
            },
        )
    }

    selectedReceipt?.let { payment ->
        PaymentReceiptDialog(
            receipt = payment.toReceiptData(patientName = patient.fullName, patientId = patient.id),
            isSaving = receiptSaving,
            onDismiss = {
                if (!receiptSaving) {
                    selectedReceipt = null
                }
            },
            onSave = { receipt ->
                receiptSaving = true
                scope.launch {
                    val file =
                        ExportService.pickSaveFile("recibo_${payment.id}.txt") ?: run {
                            messenger.showSuccess("Guardado cancelado.")
                            receiptSaving = false
                            return@launch
                        }
                    runCatching {
                        withContext(Dispatchers.IO) {
                            ExportService.writeTextFile(file, com.denticode.kt.export.renderReceiptText(receipt))
                        }
                    }.onSuccess {
                        messenger.showSuccess("Recibo guardado: ${file.name}")
                        ExportService.openFile(file)
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "No se pudo guardar el recibo.")
                    }
                    receiptSaving = false
                }
            },
        )
    }

    if (showEditPatient) {
        PatientEditDialog(
            patient = patient,
            isSaving = editPatientBusy,
            errorMessage = editPatientError,
            onDismiss = {
                if (!editPatientBusy) {
                    showEditPatient = false
                    editPatientError = null
                }
            },
            onSubmit = { request ->
                editPatientBusy = true
                editPatientError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            // Check for duplicate name
                            val existing = repo.findPatientByFullName(request.firstName, request.lastName, patient.id)
                            if (existing != null) {
                                throw IllegalArgumentException("Ya existe un paciente con ese nombre.")
                            }
                            repo.updatePatient(patient.id, request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showEditPatient = false
                        messenger.showSuccess("Paciente actualizado correctamente.")
                    }.onFailure { e ->
                        editPatientError = e.message ?: "No se pudo actualizar el paciente."
                    }
                    editPatientBusy = false
                }
            },
        )
    }

    if (showDeleteDialog) {
        PatientDeleteDialog(
            patientName = patient.fullName,
            isSaving = archiveDeleteBusy,
            onDismiss = {
                if (!archiveDeleteBusy) {
                    showDeleteDialog = false
                }
            },
            onConfirm = {
                scope.launch {
                    archiveDeleteBusy = true
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.hardDeletePatient(patient.id)
                        }
                    }.onSuccess {
                        showDeleteDialog = false
                        messenger.showSuccess("Paciente eliminado permanentemente.")
                        onClose()
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al eliminar paciente.")
                    }
                    archiveDeleteBusy = false
                }
            },
        )
    }
}

@Composable
private fun PatientDeleteDialog(
    patientName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Eliminar paciente",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "¿Estás seguro de que deseas eliminar permanentemente a $patientName? Esta acción no se puede deshacer.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, alignment = Alignment.End),
            ) {
                AppOutlinedButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSaving,
                )
                AppButton(
                    text = if (isSaving) "Eliminando..." else "Eliminar permanentemente",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
private fun PatientEditDialog(
    patient: Patient,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientUpdateRequest) -> Unit,
) {
    var firstName by remember { mutableStateOf(patient.firstName) }
    var lastName by remember { mutableStateOf(patient.lastName) }
    var birthDate by remember {
        mutableStateOf(
            try {
                java.time.LocalDate.parse(patient.dateOfBirth)
            } catch (_: Exception) {
                java.time.LocalDate.now().minusYears(25)
            },
        )
    }
    val birthSelectableDates = rememberPastOrTodaySelectableDates()
    var documentNumber by remember { mutableStateOf(patient.documentNumber.orEmpty()) }
    var contactPhone by remember { mutableStateOf(patient.contactPhone) }
    var email by remember { mutableStateOf(patient.email.orEmpty()) }
    var medicalHistorySummary by remember { mutableStateOf(patient.medicalHistorySummary.orEmpty()) }
    val canSubmit =
        firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            contactPhone.isNotBlank() &&
            !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Editar paciente",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Actualiza los datos del paciente.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "Nombre",
                placeholder = "Ana",
                enabled = !isSaving,
            )
            AppTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Apellido",
                placeholder = "López",
                enabled = !isSaving,
            )
            AppDatePickerField(
                label = "Fecha de nacimiento",
                value = birthDate,
                onValueChange = { birthDate = it },
                enabled = !isSaving,
                selectableDates = birthSelectableDates,
                shortcuts = null,
            )
            AppTextField(
                value = documentNumber,
                onValueChange = { documentNumber = it },
                label = "Documento de identidad",
                placeholder = "1234567",
                enabled = !isSaving,
            )
            AppTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = "Teléfono",
                placeholder = "+34 600 000 000",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "cliente@correo.com",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            AppTextArea(
                value = medicalHistorySummary,
                onValueChange = { medicalHistorySummary = it },
                label = "Resumen médico",
                placeholder = "Alergias, condiciones relevantes o notas iniciales",
                enabled = !isSaving,
                minLines = 3,
                maxLines = 5,
            )
            errorMessage?.let {
                Text(
                    text = it,
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, alignment = Alignment.End),
            ) {
                AppOutlinedButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSaving,
                )
                AppButton(
                    text = if (isSaving) "Guardando..." else "Guardar cambios",
                    onClick = {
                        onSubmit(
                            PatientUpdateRequest(
                                firstName = firstName,
                                lastName = lastName,
                                dateOfBirth = birthDate.toString(),
                                documentNumber = documentNumber,
                                contactPhone = contactPhone,
                                email = email,
                                medicalHistorySummary = medicalHistorySummary,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
