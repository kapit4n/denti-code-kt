@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.denticode.kt.data.PatientTreatmentRegisterRequest
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
}
