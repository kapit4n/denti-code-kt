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
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.visitStatusOptions
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.patientdetail.ModernPatientDetailContent
import com.denticode.kt.ui.patientdetail.PatientDetailFocusSection
import com.denticode.kt.ui.patientdetail.PatientNewPaymentDialog
import com.denticode.kt.ui.patientdetail.PatientNewVisitDialog
import com.denticode.kt.ui.patientdetail.RegisterTreatmentDialog
import com.denticode.kt.ui.patientdetail.buildTreatmentAppointmentLinkOptions
import com.denticode.kt.ui.patientdetail.buildPatientDetailUiState
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
    var treatmentPaymentOptions by remember { mutableStateOf<List<com.denticode.kt.data.TreatmentPaymentOption>>(emptyList()) }
    var refreshNonce by remember { mutableStateOf(0) }
    var showNewVisit by remember { mutableStateOf(false) }
    var showNewPayment by remember { mutableStateOf(false) }
    var showRegisterTreatment by remember { mutableStateOf(false) }
    var saveVisitBusy by remember { mutableStateOf(false) }
    var savePaymentBusy by remember { mutableStateOf(false) }
    var saveTreatmentBusy by remember { mutableStateOf(false) }
    var saveVisitError by remember { mutableStateOf<String?>(null) }
    var savePaymentError by remember { mutableStateOf<String?>(null) }
    var saveTreatmentError by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var showEditPatient by remember { mutableStateOf(false) }
    var editPatientBusy by remember { mutableStateOf(false) }
    var editPatientError by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var archiveDeleteBusy by remember { mutableStateOf(false) }
    val messenger = LocalAppMessenger.current

    LaunchedEffect(patient.id, refreshNonce) {
        withContext(Dispatchers.IO) {
            appointments = repo.listAppointmentsForPatient(patient.id)
            treatments = repo.listTreatmentsForPatient(patient.id)
            payments = repo.listPaymentsForPatient(patient.id)
            doctors = repo.listDoctors()
            procedureTypes = repo.listProcedureTypes()
            treatmentPaymentOptions = repo.listTreatmentPaymentOptionsForPatient(patient.id)
        }
        loaded = true
    }

    val activeDoctors = remember(doctors) { doctors.filter { it.isActive } }
    val treatmentLinkOptions = remember(appointments) { buildTreatmentAppointmentLinkOptions(appointments) }

    val uiState =
        remember(patient, appointments, treatments, payments) {
            buildPatientDetailUiState(
                patient = patient,
                appointments = appointments,
                treatments = treatments,
                payments = payments,
                filter = null,
            )
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
            uiState = uiState,
            totalAppointmentsUnfiltered = appointments.size,
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
