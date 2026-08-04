package com.denticode.kt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Patient
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.patientdetail.ModernPatientDetailContent
import com.denticode.kt.ui.patientdetail.PatientDetailDialogsHost
import com.denticode.kt.ui.patientdetail.PatientDetailFocusSection
import com.denticode.kt.ui.patientdetail.PatientDetailWindowActions
import com.denticode.kt.ui.patients.PatientsPremiumPalette

@Composable
fun PatientDetailWindow(
    repo: DentiRepository,
    patient: Patient,
    focusSection: PatientDetailFocusSection = PatientDetailFocusSection.OVERVIEW,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val messenger = LocalAppMessenger.current
    val actions =
        remember(patient.id) {
            PatientDetailWindowActions(
                repo = repo,
                patient = patient,
                messenger = messenger,
                scope = scope,
                onClose = onClose,
            )
        }

    LaunchedEffect(patient.id, actions.refreshNonce) {
        actions.load()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PatientsPremiumPalette.background,
    ) {
        if (!actions.loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            val workspaceUi =
                remember(
                    patient,
                    actions.appointments,
                    actions.treatments,
                    actions.payments,
                    actions.clinicalProfile,
                    actions.treatmentPlans,
                ) {
                    actions.buildWorkspaceUi()
                }
            ModernPatientDetailContent(
                workspace = workspaceUi,
                focusSection = focusSection,
                onClose = onClose,
                onRegisterAppointment = actions.onRegisterAppointment,
                onRegisterTreatment = actions.onRegisterTreatment,
                onRegisterPayment = actions.onRegisterPayment,
                registerAppointmentEnabled = actions.registerAppointmentEnabled,
                registerTreatmentEnabled = actions.registerTreatmentEnabled,
                onEditPatient = actions.onEditPatient,
                onArchivePatient = actions.onArchivePatient,
                onDeletePatient = actions.onDeletePatient,
                isArchived = patient.isArchived,
                onAddMedical = actions.onAddMedical,
                onEditMedical = actions.onEditMedical,
                onDeleteMedical = actions.onDeleteMedical,
                onAddDental = actions.onAddDental,
                onEditDental = actions.onEditDental,
                onDeleteDental = actions.onDeleteDental,
                onAddNote = actions.onAddNote,
                onTogglePinNote = actions.onTogglePinNote,
                onDeleteNote = actions.onDeleteNote,
                onAddPrescription = actions.onAddPrescription,
                onPrescriptionStatusChange = actions.onPrescriptionStatusChange,
                onDeletePrescription = actions.onDeletePrescription,
                onAddFollowUp = actions.onAddFollowUp,
                onFollowUpStatusChange = actions.onFollowUpStatusChange,
                onDeleteFollowUp = actions.onDeleteFollowUp,
                onUploadDocument = actions.onUploadDocument,
                onOpenDocument = actions.onOpenDocument,
                onDeleteDocument = actions.onDeleteDocument,
                onAddPlan = actions.onAddPlan,
                onEditPlan = actions.onEditPlan,
                onDeletePlan = actions.onDeletePlan,
                onPlanStatusChange = actions.onPlanStatusChange,
                onAddPhase = actions.onAddPhase,
                onEditPhase = actions.onEditPhase,
                onDeletePhase = actions.onDeletePhase,
                onPhaseStatusChange = actions.onPhaseStatusChange,
                onViewReceipt = actions.onViewReceipt,
                onExportFicha = actions.onExportFicha,
                onPrintSummary = actions.onPrintSummary,
            )
        }
    }

    PatientDetailDialogsHost(
        actions = actions,
        onClose = onClose,
    )
}
