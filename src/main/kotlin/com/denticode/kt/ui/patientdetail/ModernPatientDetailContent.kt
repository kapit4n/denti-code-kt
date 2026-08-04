@file:OptIn(ExperimentalFoundationApi::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.PrescriptionStatus
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppSpacing

@Composable
fun ModernPatientDetailContent(
    workspace: PatientWorkspaceUiState,
    focusSection: PatientDetailFocusSection = PatientDetailFocusSection.OVERVIEW,
    onClose: () -> Unit,
    onRegisterAppointment: () -> Unit,
    onRegisterTreatment: () -> Unit,
    onRegisterPayment: () -> Unit,
    registerAppointmentEnabled: Boolean,
    registerTreatmentEnabled: Boolean = true,
    onEditPatient: () -> Unit,
    onArchivePatient: () -> Unit = {},
    onDeletePatient: () -> Unit = {},
    isArchived: Boolean = false,
    onAddMedical: () -> Unit,
    onEditMedical: (PatientMedicalRecordUi) -> Unit,
    onDeleteMedical: (PatientMedicalRecordUi) -> Unit,
    onAddDental: () -> Unit,
    onEditDental: (PatientDentalRecordUi) -> Unit,
    onDeleteDental: (PatientDentalRecordUi) -> Unit,
    onAddNote: () -> Unit,
    onTogglePinNote: (PatientNoteUi) -> Unit,
    onDeleteNote: (PatientNoteUi) -> Unit,
    onAddPrescription: () -> Unit,
    onPrescriptionStatusChange: (PatientPrescriptionUi, PrescriptionStatus) -> Unit,
    onDeletePrescription: (PatientPrescriptionUi) -> Unit,
    onAddFollowUp: () -> Unit,
    onFollowUpStatusChange: (PatientFollowUpUi, FollowUpStatus) -> Unit,
    onDeleteFollowUp: (PatientFollowUpUi) -> Unit,
    onUploadDocument: () -> Unit,
    onOpenDocument: (PatientDocumentUi) -> Unit,
    onDeleteDocument: (PatientDocumentUi) -> Unit,
    onAddPlan: () -> Unit,
    onEditPlan: (PatientTreatmentPlanUi) -> Unit,
    onDeletePlan: (PatientTreatmentPlanUi) -> Unit,
    onPlanStatusChange: (PatientTreatmentPlanUi, TreatmentPlanStatus) -> Unit,
    onAddPhase: (PatientTreatmentPlanUi) -> Unit,
    onEditPhase: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi) -> Unit,
    onDeletePhase: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi) -> Unit,
    onPhaseStatusChange: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi, TreatmentPlanPhaseStatus) -> Unit,
    onViewReceipt: (PatientDetailPaymentUi) -> Unit = {},
    onExportFicha: () -> Unit = {},
    onPrintSummary: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    val initialTab =
        remember(focusSection) {
            when (focusSection) {
                PatientDetailFocusSection.OVERVIEW -> PatientWorkspaceTab.OVERVIEW
                PatientDetailFocusSection.CLINICAL_HISTORY -> PatientWorkspaceTab.CLINICAL_HISTORY
            }
        }
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var appointmentFilter by remember { mutableStateOf<AppointmentStatus?>(null) }

    val summaryCounts = remember(workspace.treatments) { buildClinicalSummaryCounts(workspace.treatments) }

    val tabs =
        remember(workspace) {
            listOf(
                WorkspaceTabBadge(PatientWorkspaceTab.OVERVIEW, null),
                WorkspaceTabBadge(
                    PatientWorkspaceTab.CLINICAL_HISTORY,
                    workspace.medicalRecords.size + workspace.dentalRecords.size,
                ),
                WorkspaceTabBadge(PatientWorkspaceTab.TIMELINE, workspace.timeline.size),
                WorkspaceTabBadge(PatientWorkspaceTab.APPOINTMENTS, workspace.appointments.size),
                WorkspaceTabBadge(PatientWorkspaceTab.PAYMENTS, workspace.payments.size),
                WorkspaceTabBadge(PatientWorkspaceTab.DOCUMENTS, workspace.documents.size),
                WorkspaceTabBadge(PatientWorkspaceTab.NOTES, workspace.notes.size),
                WorkspaceTabBadge(PatientWorkspaceTab.PRESCRIPTIONS, workspace.activePrescriptions),
                WorkspaceTabBadge(PatientWorkspaceTab.FOLLOW_UPS, workspace.pendingFollowUps),
                WorkspaceTabBadge(PatientWorkspaceTab.TREATMENT_PLAN, workspace.treatmentPlans.size),
            )
        }

    val statusFilterLabel =
        appointmentFilter?.let { appointmentStatusDetailLabel(it) } ?: "Todos los estados"
    val statusOptions =
        remember {
            listOf("Todos los estados") +
                listOf(
                    AppointmentStatus.CONFIRMED,
                    AppointmentStatus.SCHEDULED,
                    AppointmentStatus.COMPLETED,
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.IN_PROGRESS,
                ).map { appointmentStatusDetailLabel(it) }
        }

    val filteredAppointments =
        remember(workspace.appointments, appointmentFilter) {
            if (appointmentFilter == null) {
                workspace.appointments
            } else {
                workspace.appointments.filter { it.status == appointmentFilter }
            }
        }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(PatientsPremiumPalette.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            PatientHeaderCard(
                patient = workspace.patient,
                kpis = workspace.kpis,
                onViewFullProfile = { messenger.showSuccess("Perfil completo (próximamente).") },
                onClose = onClose,
            )

            WorkspaceTabBar(
                tabs = tabs,
                selected = selectedTab,
                onSelect = { selectedTab = it },
            )

            when (selectedTab) {
                PatientWorkspaceTab.OVERVIEW -> {
                    ClinicalSummaryPanel(
                        summary = summaryCounts,
                        activePrescriptions = workspace.activePrescriptions,
                        pendingFollowUps = workspace.pendingFollowUps,
                        activePlans = workspace.activePlans,
                        pendingBalance = workspace.paymentSummary.pending,
                        onExportFicha = onExportFicha,
                    )
                    TimelinePanel(
                        timeline = workspace.timeline.take(6),
                        onJumpToSection = { selectedTab = it },
                    )
                }

                PatientWorkspaceTab.CLINICAL_HISTORY -> {
                    ClinicalHistoryPanel(
                        medicalRecords = workspace.medicalRecords,
                        dentalRecords = workspace.dentalRecords,
                        summary = summaryCounts,
                        onAddMedical = onAddMedical,
                        onEditMedical = onEditMedical,
                        onDeleteMedical = onDeleteMedical,
                        onAddDental = onAddDental,
                        onEditDental = onEditDental,
                        onDeleteDental = onDeleteDental,
                        enabled = true,
                    )
                }

                PatientWorkspaceTab.TIMELINE -> {
                    TimelinePanel(
                        timeline = workspace.timeline,
                        onJumpToSection = { selectedTab = it },
                    )
                }

                PatientWorkspaceTab.APPOINTMENTS -> {
                    AppointmentsPanel(
                        appointments = filteredAppointments,
                        totalCount = workspace.appointments.size,
                        statusFilterLabel = statusFilterLabel,
                        statusOptions = statusOptions,
                        onStatusSelect = { label ->
                            appointmentFilter =
                                if (label == "Todos los estados") {
                                    null
                                } else {
                                    AppointmentStatus.entries.first { appointmentStatusDetailLabel(it) == label }
                                }
                        },
                        onRegisterAppointment = onRegisterAppointment,
                        onViewAll = { messenger.showSuccess("Abrir módulo Citas.") },
                        registerEnabled = registerAppointmentEnabled,
                    )
                }

                PatientWorkspaceTab.PAYMENTS -> {
                    PaymentsPanel(
                        payments = workspace.payments,
                        summary = workspace.paymentSummary,
                        onRegisterPayment = onRegisterPayment,
                        onViewHistory = { messenger.showSuccess("Historial de pagos (próximamente).") },
                        onViewReceipt = onViewReceipt,
                    )
                }

                PatientWorkspaceTab.DOCUMENTS -> {
                    DocumentsPanel(
                        documents = workspace.documents,
                        onUpload = onUploadDocument,
                        onOpen = onOpenDocument,
                        onDelete = onDeleteDocument,
                    )
                }

                PatientWorkspaceTab.NOTES -> {
                    NotesPanel(
                        notes = workspace.notes,
                        onAdd = onAddNote,
                        onTogglePin = onTogglePinNote,
                        onDelete = onDeleteNote,
                    )
                }

                PatientWorkspaceTab.PRESCRIPTIONS -> {
                    PrescriptionsPanel(
                        prescriptions = workspace.prescriptions,
                        onAdd = onAddPrescription,
                        onStatusChange = onPrescriptionStatusChange,
                        onDelete = onDeletePrescription,
                    )
                }

                PatientWorkspaceTab.FOLLOW_UPS -> {
                    FollowUpsPanel(
                        followUps = workspace.followUps,
                        onAdd = onAddFollowUp,
                        onStatusChange = onFollowUpStatusChange,
                        onDelete = onDeleteFollowUp,
                    )
                }

                PatientWorkspaceTab.TREATMENT_PLAN -> {
                    TreatmentPlanPanel(
                        plans = workspace.treatmentPlans,
                        onAddPlan = onAddPlan,
                        onEditPlan = onEditPlan,
                        onDeletePlan = onDeletePlan,
                        onPlanStatusChange = onPlanStatusChange,
                        onAddPhase = onAddPhase,
                        onEditPhase = onEditPhase,
                        onDeletePhase = onDeletePhase,
                        onPhaseStatusChange = onPhaseStatusChange,
                    )
                }
            }

            QuickActionsFooter(
                onEditPatient = onEditPatient,
                onNewAppointment = onRegisterAppointment,
                onRegisterTreatment = onRegisterTreatment,
                onRegisterPayment = onRegisterPayment,
                onClinicalHistory = { selectedTab = PatientWorkspaceTab.CLINICAL_HISTORY },
                onSendReminder = { messenger.showSuccess("Recordatorio preparado (simulación).") },
                onExportFicha = onExportFicha,
                onPrintSummary = onPrintSummary,
                onArchivePatient = onArchivePatient,
                onDeletePatient = onDeletePatient,
                isArchived = isArchived,
            )
        }
    }
}
