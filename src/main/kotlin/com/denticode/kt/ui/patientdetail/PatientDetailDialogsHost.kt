@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientUpdateRequest
import com.denticode.kt.export.renderReceiptText
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.payments.PaymentReceiptDialog
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun PatientDetailDialogsHost(
    actions: PatientDetailWindowActions,
    onClose: () -> Unit,
) {
    val patient = actions.patient

    if (actions.showNewVisit) {
        PatientNewVisitDialog(
            patient = patient,
            doctors = actions.activeDoctors,
            procedureTypes = actions.procedureTypes,
            visitStatuses = actions.visitStatuses,
            isSaving = actions.saveVisitBusy,
            errorMessage = actions.saveVisitError,
            onDismiss = {
                if (!actions.saveVisitBusy) {
                    actions.showNewVisit = false
                    actions.saveVisitError = null
                }
            },
            onSubmit = actions::submitVisit,
        )
    }

    if (actions.showNewPayment) {
        PatientNewPaymentDialog(
            procedureTypes = actions.procedureTypes,
            treatmentOptions = actions.treatmentPaymentOptions,
            isSaving = actions.savePaymentBusy,
            errorMessage = actions.savePaymentError,
            onDismiss = {
                if (!actions.savePaymentBusy) {
                    actions.showNewPayment = false
                    actions.savePaymentError = null
                }
            },
            onSubmit = actions::submitPayment,
        )
    }

    if (actions.showRegisterTreatment) {
        RegisterTreatmentDialog(
            patientLabel = "Paciente: ${patient.fullName}",
            doctors = actions.activeDoctors,
            procedureTypes = actions.procedureTypes,
            appointmentLinkOptions = actions.treatmentLinkOptions,
            isSaving = actions.saveTreatmentBusy,
            errorMessage = actions.saveTreatmentError,
            onDismiss = {
                if (!actions.saveTreatmentBusy) {
                    actions.showRegisterTreatment = false
                    actions.saveTreatmentError = null
                }
            },
            onSubmit = actions::submitTreatment,
        )
    }

    if (actions.showMedicalRecord) {
        MedicalRecordDialog(
            initial = actions.editingMedical,
            doctors = actions.activeDoctors,
            isSaving = actions.saveMedicalBusy,
            errorMessage = actions.saveMedicalError,
            onDismiss = {
                if (!actions.saveMedicalBusy) {
                    actions.showMedicalRecord = false
                    actions.editingMedical = null
                    actions.saveMedicalError = null
                }
            },
            onSubmit = actions::submitMedical,
        )
    }

    if (actions.showDentalRecord) {
        DentalRecordDialog(
            initial = actions.editingDental,
            doctors = actions.activeDoctors,
            procedureTypes = actions.procedureTypes,
            isSaving = actions.saveDentalBusy,
            errorMessage = actions.saveDentalError,
            onDismiss = {
                if (!actions.saveDentalBusy) {
                    actions.showDentalRecord = false
                    actions.editingDental = null
                    actions.saveDentalError = null
                }
            },
            onSubmit = actions::submitDental,
        )
    }

    if (actions.showAddNote) {
        PatientNoteDialog(
            isSaving = actions.saveNoteBusy,
            errorMessage = actions.saveNoteError,
            onDismiss = {
                if (!actions.saveNoteBusy) {
                    actions.showAddNote = false
                    actions.saveNoteError = null
                }
            },
            onSubmit = actions::submitNote,
        )
    }

    if (actions.showAddPrescription) {
        PrescriptionDialog(
            doctors = actions.activeDoctors,
            isSaving = actions.savePrescriptionBusy,
            errorMessage = actions.savePrescriptionError,
            onDismiss = {
                if (!actions.savePrescriptionBusy) {
                    actions.showAddPrescription = false
                    actions.savePrescriptionError = null
                }
            },
            onSubmit = actions::submitPrescription,
        )
    }

    if (actions.showAddFollowUp) {
        FollowUpDialog(
            appointmentLinkOptions = actions.treatmentLinkOptions,
            isSaving = actions.saveFollowUpBusy,
            errorMessage = actions.saveFollowUpError,
            onDismiss = {
                if (!actions.saveFollowUpBusy) {
                    actions.showAddFollowUp = false
                    actions.saveFollowUpError = null
                }
            },
            onSubmit = actions::submitFollowUp,
        )
    }

    if (actions.showAddDocument) {
        PatientDocumentDialog(
            patientId = patient.id,
            isSaving = actions.saveDocumentBusy,
            errorMessage = actions.saveDocumentError,
            onDismiss = {
                if (!actions.saveDocumentBusy) {
                    actions.showAddDocument = false
                    actions.saveDocumentError = null
                }
            },
            onSubmit = actions::submitDocument,
        )
    }

    if (actions.showPlanDialog) {
        TreatmentPlanDialog(
            initial = actions.editingPlan,
            isSaving = actions.savePlanBusy,
            errorMessage = actions.savePlanError,
            onDismiss = {
                if (!actions.savePlanBusy) {
                    actions.showPlanDialog = false
                    actions.editingPlan = null
                    actions.savePlanError = null
                }
            },
            onSubmit = actions::submitPlan,
        )
    }

    if (actions.showPhaseDialog) {
        TreatmentPlanPhaseDialog(
            initial = actions.editingPhase,
            isSaving = actions.savePhaseBusy,
            errorMessage = actions.savePhaseError,
            onDismiss = {
                if (!actions.savePhaseBusy) {
                    actions.showPhaseDialog = false
                    actions.editingPhase = null
                    actions.phasePlanContext = null
                    actions.savePhaseError = null
                }
            },
            onSubmit = actions::submitPhase,
        )
    }

    actions.confirmDelete?.let { target ->
        DeleteClinicalConfirmDialog(
            target = target,
            isSaving = actions.deleteClinicalBusy,
            onDismiss = {
                if (!actions.deleteClinicalBusy) {
                    actions.confirmDelete = null
                }
            },
            onConfirm = { actions.confirmDeleteClinical(target) },
        )
    }

    actions.selectedReceipt?.let { payment ->
        PaymentReceiptDialog(
            receipt = payment.toReceiptData(patientName = patient.fullName, patientId = patient.id),
            isSaving = actions.receiptSaving,
            onDismiss = {
                if (!actions.receiptSaving) {
                    actions.selectedReceipt = null
                }
            },
            onSave = { receipt -> actions.saveReceipt(receipt, payment) },
        )
    }

    if (actions.showEditPatient) {
        PatientEditDialog(
            patient = patient,
            isSaving = actions.editPatientBusy,
            errorMessage = actions.editPatientError,
            onDismiss = {
                if (!actions.editPatientBusy) {
                    actions.showEditPatient = false
                    actions.editPatientError = null
                }
            },
            onSubmit = actions::editPatient,
        )
    }

    if (actions.showDeleteDialog) {
        PatientDeleteDialog(
            patientName = patient.fullName,
            isSaving = actions.archiveDeleteBusy,
            onDismiss = {
                if (!actions.archiveDeleteBusy) {
                    actions.showDeleteDialog = false
                }
            },
            onConfirm = { actions.hardDeletePatient() },
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
