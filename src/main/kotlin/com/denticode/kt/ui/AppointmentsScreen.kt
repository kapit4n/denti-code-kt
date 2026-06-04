@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.denticode.kt.data.AppointmentEditRequest
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.AppointmentVisitRequest
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.isCitasDataLogEnabled
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.data.ProcedureTypeOption
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.parseMoneyAmount
import com.denticode.kt.ui.treatments.TreatmentPricingFields
import com.denticode.kt.ui.treatments.applyStandardPriceIfBlank
import com.denticode.kt.data.visitStatusOptions
import com.denticode.kt.data.AppointmentActionsService
import com.denticode.kt.data.AppointmentAuditService
import com.denticode.kt.data.AppointmentDetailSnapshot
import com.denticode.kt.data.AppointmentDetailsService
import com.denticode.kt.data.AppointmentPaymentPrefill
import com.denticode.kt.data.AppointmentReminderService
import com.denticode.kt.data.AppointmentRescheduleRequest
import com.denticode.kt.data.AppointmentWorkflowService
import com.denticode.kt.data.PatientPaymentRegisterRequest
import com.denticode.kt.data.TreatmentPaymentOption
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.appointments.AppointmentQuickActionKind
import com.denticode.kt.ui.appointments.CancelAppointmentConfirmDialog
import com.denticode.kt.ui.appointments.RescheduleAppointmentDialog
import com.denticode.kt.data.TreatmentRegisterPrefill
import com.denticode.kt.data.PatientTreatmentRegisterRequest
import com.denticode.kt.ui.patientdetail.PatientDetailFocusSection
import com.denticode.kt.ui.patientdetail.PatientNewPaymentDialog
import com.denticode.kt.ui.patientdetail.RegisterTreatmentDialog
import com.denticode.kt.ui.appointments.AppointmentsPremiumContent
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.parseAppointmentScheduledAt
import com.denticode.kt.ui.procedureTypeDropdownOptions
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppVisitDateTimeFields
import com.denticode.kt.ui.components.inputs.snapFormMinuteToStep5
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val IsoLocalDateTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

private val VisitDateTimePreviewFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale("es", "ES"))

@Composable
fun AppointmentsScreen(
    repo: DentiRepository,
    onNavigate: (ScreenRoute) -> Unit = {},
    onOpenPatient: (Patient, PatientDetailFocusSection) -> Unit = { _, _ -> },
    initialDoctorFilterId: Int? = null,
    onInitialDoctorFilterConsumed: () -> Unit = {},
) {
    val messenger = LocalAppMessenger.current
    val auditService = remember(repo) { AppointmentAuditService(repo) }
    val actionsService = remember(repo) { AppointmentActionsService(repo) }
    val detailsService = remember(repo) { AppointmentDetailsService(repo) }
    val workflowService = remember(actionsService) { AppointmentWorkflowService(actionsService) }
    val reminderService = remember(auditService) { AppointmentReminderService(auditService) }
    val visitStatuses = remember { visitStatusOptions() }
    var rows by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    var appointmentCountsByDate by remember { mutableStateOf<Map<LocalDate, Int>>(emptyMap()) }
    var selectedAppointmentId by remember { mutableStateOf<Int?>(null) }
    var appointmentDetail by remember { mutableStateOf<AppointmentDetailSnapshot?>(null) }
    var detailLoading by remember { mutableStateOf(false) }
    var reminderPreview by remember { mutableStateOf("") }
    var showCreateVisit by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<AppointmentRow?>(null) }
    var reschedulingAppointment by remember { mutableStateOf<AppointmentRow?>(null) }
    var cancellingAppointment by remember { mutableStateOf<AppointmentRow?>(null) }
    var paymentAppointmentId by remember { mutableStateOf<Int?>(null) }
    var paymentPrefill by remember { mutableStateOf<AppointmentPaymentPrefill?>(null) }
    var paymentTreatmentOptions by remember { mutableStateOf<List<TreatmentPaymentOption>>(emptyList()) }
    var editingTreatment by remember { mutableStateOf<PatientTreatmentRow?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditSaving by remember { mutableStateOf(false) }
    var isRescheduleSaving by remember { mutableStateOf(false) }
    var isCancelSaving by remember { mutableStateOf(false) }
    var isPaymentSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveEditError by remember { mutableStateOf<String?>(null) }
    var rescheduleError by remember { mutableStateOf<String?>(null) }
    var cancelError by remember { mutableStateOf<String?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var treatmentAppointmentId by remember { mutableStateOf<Int?>(null) }
    var treatmentPrefill by remember { mutableStateOf<TreatmentRegisterPrefill?>(null) }
    var isTreatmentSaving by remember { mutableStateOf(false) }
    var treatmentError by remember { mutableStateOf<String?>(null) }
    var busyQuickAction by remember { mutableStateOf<AppointmentQuickActionKind?>(null) }
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var procedureTypes by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    val scope = rememberCoroutineScope()

    suspend fun reloadDetail(appointmentId: Int?) {
        val id = appointmentId ?: run {
            appointmentDetail = null
            reminderPreview = ""
            return
        }
        detailLoading = true
        val detail =
            withContext(Dispatchers.IO) {
                detailsService.loadDetail(id)
            }
        appointmentDetail = detail
        reminderPreview = detail?.let { reminderService.buildMessage(it) } ?: ""
        detailLoading = false
    }

    suspend fun reload() {
        val newRows = withContext(Dispatchers.IO) { repo.listAppointments(10_000) }
        val newPatients = withContext(Dispatchers.IO) { repo.listPatients() }
        val newDoctors = withContext(Dispatchers.IO) { repo.listDoctors() }
        val newProc = withContext(Dispatchers.IO) { repo.listProcedureTypes() }
        val counts = withContext(Dispatchers.IO) { detailsService.appointmentCountByDate() }
        if (isCitasDataLogEnabled()) {
            println(
                "[Citas UI] reload: appointmentRows=${newRows.size} (from listAppointments(10000)); " +
                    "patients=${newPatients.size}; doctors=${newDoctors.size}; procedureTypes=${newProc.size}",
            )
        }
        rows = newRows
        patients = newPatients
        doctors = newDoctors
        procedureTypes = newProc
        appointmentCountsByDate = counts
        reloadDetail(selectedAppointmentId)
    }

    LaunchedEffect(Unit) {
        reload()
    }

    LaunchedEffect(selectedAppointmentId) {
        reloadDetail(selectedAppointmentId)
    }

    fun runWorkflowAction(
        kind: AppointmentQuickActionKind,
        appointmentId: Int,
        block: suspend () -> Unit,
    ) {
        busyQuickAction = kind
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                reload()
                messenger.showSuccess("Cita actualizada correctamente.")
            }.onFailure { error ->
                messenger.showError(error.message ?: "No se pudo completar la acción.")
            }
            busyQuickAction = null
        }
    }

    AppointmentsPremiumContent(
        appointmentRows = rows,
        patients = patients,
        doctors = doctors,
        appointmentDetail = appointmentDetail,
        detailLoading = detailLoading,
        appointmentCountsByDate = appointmentCountsByDate,
        selectedAppointmentId = selectedAppointmentId,
        onSelectedAppointmentChange = { selectedAppointmentId = it },
        reminderPreview = reminderPreview,
        onReminderPreviewChange = { reminderPreview = it },
        initialDoctorFilterId = initialDoctorFilterId,
        onInitialDoctorFilterConsumed = onInitialDoctorFilterConsumed,
        onNewAppointment = {
            saveError = null
            showCreateVisit = true
        },
        onEditAppointment = { id ->
            saveEditError = null
            selectedAppointmentId = id
            editingAppointment = rows.find { it.id == id }
            scope.launch {
                editingTreatment =
                    withContext(Dispatchers.IO) {
                        actionsService.findTreatmentForAppointment(id)
                    }
            }
        },
        onRescheduleAppointment = { id ->
            rescheduleError = null
            selectedAppointmentId = id
            reschedulingAppointment = rows.find { it.id == id }
                ?: run {
                    messenger.showError("No se encontró la cita seleccionada.")
                    null
                }
        },
        onCancelAppointment = { id ->
            cancelError = null
            selectedAppointmentId = id
            cancellingAppointment = rows.find { it.id == id }
                ?: run {
                    messenger.showError("No se encontró la cita seleccionada.")
                    null
                }
        },
        onConfirmAppointment = { id ->
            selectedAppointmentId = id
            runWorkflowAction(AppointmentQuickActionKind.CONFIRM, id) {
                workflowService.confirmAppointment(id)
            }
        },
        onStartAppointment = { id ->
            selectedAppointmentId = id
            runWorkflowAction(AppointmentQuickActionKind.START, id) {
                workflowService.startAppointment(id)
            }
        },
        onCompleteAppointment = { id ->
            selectedAppointmentId = id
            runWorkflowAction(AppointmentQuickActionKind.COMPLETE, id) {
                workflowService.completeAppointment(id)
            }
        },
        onRegisterPaymentForAppointment = { id ->
            paymentError = null
            selectedAppointmentId = id
            busyQuickAction = AppointmentQuickActionKind.REGISTER_PAYMENT
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val prefill = actionsService.buildPaymentPrefill(id)
                        val options = repo.listTreatmentPaymentOptionsForPatient(prefill.patientId)
                        prefill to options
                    }
                }.onSuccess { (prefill, options) ->
                    paymentAppointmentId = id
                    paymentPrefill = prefill
                    paymentTreatmentOptions = options
                }.onFailure { error ->
                    messenger.showError(error.message ?: "No se pudo preparar el pago.")
                }
                busyQuickAction = null
            }
        },
        onRegisterTreatmentForAppointment = { id ->
            treatmentError = null
            selectedAppointmentId = id
            busyQuickAction = AppointmentQuickActionKind.REGISTER_TREATMENT
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        actionsService.buildTreatmentPrefill(id)
                    }
                }.onSuccess { prefill ->
                    treatmentAppointmentId = id
                    treatmentPrefill = prefill
                }.onFailure { error ->
                    messenger.showError(error.message ?: "No se pudo preparar el registro de tratamiento.")
                }
                busyQuickAction = null
            }
        },
        onAddNote = { appointmentId, body ->
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        detailsService.addNote(appointmentId, body)
                    }
                }.onSuccess {
                    reloadDetail(appointmentId)
                    messenger.showSuccess("Nota añadida.")
                }.onFailure { error ->
                    messenger.showError(error.message ?: "No se pudo añadir la nota.")
                }
            }
        },
        onEditNote = { appointmentId, noteId, body ->
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        detailsService.updateNote(noteId, appointmentId, body)
                    }
                }.onSuccess {
                    reloadDetail(appointmentId)
                    messenger.showSuccess("Nota actualizada.")
                }.onFailure { error ->
                    messenger.showError(error.message ?: "No se pudo editar la nota.")
                }
            }
        },
        onDeleteNote = { appointmentId, noteId ->
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        detailsService.deleteNote(noteId, appointmentId)
                    }
                }.onSuccess {
                    reloadDetail(appointmentId)
                    messenger.showSuccess("Nota eliminada.")
                }.onFailure { error ->
                    messenger.showError(error.message ?: "No se pudo eliminar la nota.")
                }
            }
        },
        onCopyReminder = {
            val id = selectedAppointmentId
            val detail = appointmentDetail
            if (id == null || detail == null) {
                messenger.showError("Selecciona una cita primero.")
            } else {
                reminderService.copyToClipboard(reminderPreview)
                reminderService.logReminderSent(id)
                scope.launch { reloadDetail(id) }
                messenger.showSuccess("Mensaje copiado al portapapeles.")
            }
        },
        onOpenWhatsAppReminder = {
            val id = selectedAppointmentId
            val detail = appointmentDetail
            if (id == null || detail == null) {
                messenger.showError("Selecciona una cita primero.")
            } else {
                val url = reminderService.buildWhatsAppUrl(detail.patientPhone, reminderPreview)
                if (url == null) {
                    messenger.showError("No hay teléfono válido para WhatsApp.")
                } else {
                    runCatching {
                        reminderService.openUrl(url)
                        reminderService.logReminderSent(id)
                    }.onSuccess {
                        scope.launch { reloadDetail(id) }
                        messenger.showSuccess("WhatsApp abierto con el mensaje preparado.")
                    }.onFailure {
                        messenger.showError("No se pudo abrir WhatsApp.")
                    }
                }
            }
        },
        onViewPayments = {
            onNavigate(ScreenRoute.Payments)
            messenger.showSuccess("Abriendo módulo de pagos.")
        },
        onNavigate = onNavigate,
        onOpenPatient = onOpenPatient,
        busyQuickAction = busyQuickAction,
        modifier = Modifier.fillMaxSize(),
    )

    if (showCreateVisit) {
        CreateVisitDialog(
            patients = patients,
            doctors = doctors.filter { it.isActive },
            procedureTypes = procedureTypes,
            visitStatuses = visitStatuses,
            isSaving = isSaving,
            errorMessage = saveError,
            onDismiss = {
                if (!isSaving) {
                    showCreateVisit = false
                    saveError = null
                }
            },
            onSubmit = { request ->
                isSaving = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.createAppointment(request)
                        }
                    }.onSuccess {
                        reload()
                        showCreateVisit = false
                    }.onFailure { error ->
                        saveError = error.message ?: "No se pudo crear la visita."
                    }
                    isSaving = false
                }
            },
        )
    }

    editingAppointment?.let { ap ->
        EditVisitDialog(
            appointment = ap,
            initialTreatment = editingTreatment,
            patients = patients,
            doctors = doctors,
            procedureTypes = procedureTypes,
            visitStatuses = visitStatuses,
            isSaving = isEditSaving,
            errorMessage = saveEditError,
            onDismiss = {
                if (!isEditSaving) {
                    editingAppointment = null
                    editingTreatment = null
                    saveEditError = null
                }
            },
            onSubmit = { appointmentId, request ->
                isEditSaving = true
                saveEditError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            actionsService.updateAppointment(appointmentId, request)
                        }
                    }.onSuccess {
                        reload()
                        editingAppointment = null
                        editingTreatment = null
                        messenger.showSuccess("Cita actualizada correctamente.")
                    }.onFailure { error ->
                        saveEditError = error.message ?: "No se pudo guardar la visita."
                    }
                    isEditSaving = false
                }
            },
        )
    }

    reschedulingAppointment?.let { ap ->
        RescheduleAppointmentDialog(
            appointment = ap,
            isSaving = isRescheduleSaving,
            errorMessage = rescheduleError,
            onDismiss = {
                if (!isRescheduleSaving) {
                    reschedulingAppointment = null
                    rescheduleError = null
                }
            },
            onConfirm = { newDate, newHour, newMinute, note ->
                isRescheduleSaving = true
                rescheduleError = null
                busyQuickAction = AppointmentQuickActionKind.RESCHEDULE
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            actionsService.rescheduleAppointment(
                                ap.id,
                                AppointmentRescheduleRequest(
                                    newDate = newDate,
                                    newHour = newHour,
                                    newMinute = newMinute,
                                    note = note,
                                ),
                            )
                        }
                    }.onSuccess {
                        reload()
                        reschedulingAppointment = null
                        messenger.showSuccess("Cita reprogramada correctamente.")
                    }.onFailure { error ->
                        rescheduleError = error.message ?: "No se pudo reprogramar la cita."
                    }
                    isRescheduleSaving = false
                    busyQuickAction = null
                }
            },
        )
    }

    cancellingAppointment?.let { ap ->
        CancelAppointmentConfirmDialog(
            patientName = ap.patientName,
            isSaving = isCancelSaving,
            errorMessage = cancelError,
            onDismiss = {
                if (!isCancelSaving) {
                    cancellingAppointment = null
                    cancelError = null
                }
            },
            onConfirm = { reason ->
                isCancelSaving = true
                cancelError = null
                busyQuickAction = AppointmentQuickActionKind.CANCEL
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            actionsService.cancelAppointment(ap.id, reason)
                        }
                    }.onSuccess {
                        reload()
                        cancellingAppointment = null
                        messenger.showSuccess("Cita cancelada correctamente.")
                    }.onFailure { error ->
                        cancelError = error.message ?: "No se pudo cancelar la cita."
                    }
                    isCancelSaving = false
                    busyQuickAction = null
                }
            },
        )
    }

    paymentAppointmentId?.let { apptId ->
        val prefill = paymentPrefill
        if (prefill != null) {
            PatientNewPaymentDialog(
                procedureTypes = procedureTypes,
                treatmentOptions = paymentTreatmentOptions,
                initialAmountText = prefill.suggestedAmount?.let { formatMoney(it) },
                initialProcedureTypeId = prefill.procedureTypeId,
                initialPerformedActionId = prefill.performedActionId,
                initialNote =
                    prefill.treatmentLabel?.let { "Pago cita · $it" },
                isSaving = isPaymentSaving,
                errorMessage = paymentError,
                onDismiss = {
                    if (!isPaymentSaving) {
                        paymentAppointmentId = null
                        paymentPrefill = null
                        paymentTreatmentOptions = emptyList()
                        paymentError = null
                    }
                },
                onSubmit = { request ->
                    isPaymentSaving = true
                    paymentError = null
                    busyQuickAction = AppointmentQuickActionKind.REGISTER_PAYMENT
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                actionsService.registerPaymentForAppointment(
                                    apptId,
                                    request.copy(appointmentId = apptId),
                                )
                            }
                        }.onSuccess {
                            reload()
                            paymentAppointmentId = null
                            paymentPrefill = null
                            paymentTreatmentOptions = emptyList()
                            messenger.showSuccess("Pago registrado correctamente.")
                        }.onFailure { error ->
                            paymentError = error.message ?: "No se pudo registrar el pago."
                        }
                        isPaymentSaving = false
                        busyQuickAction = null
                    }
                },
            )
        }
    }

    treatmentAppointmentId?.let { apptId ->
        val prefill = treatmentPrefill
        if (prefill != null) {
            val patientLabel =
                patients.find { it.id == prefill.patientId }?.let { "Paciente: ${it.fullName}" }
                    ?: "Paciente #${prefill.patientId}"
            RegisterTreatmentDialog(
                patientLabel = patientLabel,
                doctors = doctors.filter { it.isActive },
                procedureTypes = procedureTypes,
                appointmentLinkOptions = emptyList(),
                lockedAppointmentId = apptId,
                initialDoctorId = prefill.primaryDoctorId,
                initialProcedureTypeId = prefill.procedureTypeId,
                initialPriceText = prefill.unitPrice?.let { formatMoney(it) },
                initialTreatmentStatus = prefill.treatmentStatus,
                initialActionDate = prefill.actionDate,
                initialActionHour = prefill.actionHour,
                initialActionMinute = prefill.actionMinute,
                initialNotes = prefill.descriptionNotes,
                isUpdate = prefill.existingTreatmentId != null,
                isSaving = isTreatmentSaving,
                errorMessage = treatmentError,
                onDismiss = {
                    if (!isTreatmentSaving) {
                        treatmentAppointmentId = null
                        treatmentPrefill = null
                        treatmentError = null
                    }
                },
                onSubmit = { request ->
                    isTreatmentSaving = true
                    treatmentError = null
                    busyQuickAction = AppointmentQuickActionKind.REGISTER_TREATMENT
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                actionsService.registerTreatmentForAppointment(apptId, request)
                            }
                        }.onSuccess {
                            reload()
                            treatmentAppointmentId = null
                            treatmentPrefill = null
                            messenger.showSuccess(
                                if (prefill.existingTreatmentId != null) {
                                    "Tratamiento actualizado correctamente."
                                } else {
                                    "Tratamiento registrado correctamente."
                                },
                            )
                        }.onFailure { error ->
                            treatmentError = error.message ?: "No se pudo registrar el tratamiento."
                        }
                        isTreatmentSaving = false
                        busyQuickAction = null
                    }
                },
            )
        }
    }
}

@Composable
private fun CreateVisitDialog(
    patients: List<Patient>,
    doctors: List<Doctor>,
    procedureTypes: List<ProcedureTypeRow>,
    visitStatuses: List<AppointmentStatus>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (AppointmentVisitRequest) -> Unit,
) {
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var visitStatus by remember { mutableStateOf(AppointmentStatus.SCHEDULED) }
    val defaultVisit = remember {
        LocalDateTime.now().plusHours(1).withSecond(0).withNano(0)
    }
    var visitDate by remember { mutableStateOf(defaultVisit.toLocalDate()) }
    var visitHour by remember { mutableStateOf(defaultVisit.hour) }
    var visitMinute by remember { mutableStateOf(snapFormMinuteToStep5(defaultVisit.minute)) }
    var durationText by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember { mutableStateOf(ProcedureTypeOption.none()) }
    var priceText by remember { mutableStateOf("") }
    var treatmentStatus by remember { mutableStateOf(TreatmentStatus.PLANNED) }

    val visitDateTime =
        remember(visitDate, visitHour, visitMinute) {
            LocalDateTime.of(visitDate, LocalTime.of(visitHour, visitMinute))
        }
    val durationMinutes =
        durationText.trim().toIntOrNull()
    val canSubmit =
        selectedPatient != null &&
            selectedDoctor != null &&
            !isSaving &&
            patients.isNotEmpty() &&
            doctors.isNotEmpty()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Nueva visita",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Programa una cita eligiendo paciente, doctor, fecha en el calendario y hora.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (patients.isEmpty() || doctors.isEmpty()) {
                Text(
                    text =
                        when {
                            patients.isEmpty() && doctors.isEmpty() ->
                                "No hay pacientes ni doctores. Registra pacientes y doctores antes de crear visitas."
                            patients.isEmpty() -> "No hay pacientes. Registra al menos un paciente primero."
                            else -> "No hay doctores activos. Añade o activa un doctor primero."
                        },
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            AppDropdownField(
                label = "Paciente",
                options = patients,
                selected = selectedPatient,
                onSelected = { selectedPatient = it },
                enabled = !isSaving && patients.isNotEmpty(),
                optionLabel = { "${it.fullName} (id ${it.id})" },
                placeholder = "Selecciona paciente…",
                searchable = true,
                searchPlaceholder = "Buscar por nombre…",
            )
            AppDropdownField(
                label = "Doctor",
                options = doctors,
                selected = selectedDoctor,
                onSelected = { selectedDoctor = it },
                enabled = !isSaving && doctors.isNotEmpty(),
                optionLabel = { it.fullName },
                placeholder = "Selecciona doctor…",
            )
            AppDropdownField(
                label = "Tratamiento (opcional)",
                options = procedureOptions,
                selected = selectedProcedure,
                onSelected = { opt ->
                    selectedProcedure = opt
                    val row = procedureTypes.find { it.id == opt.procedureTypeId }
                    if (row?.defaultDurationMinutes != null && durationText.isBlank()) {
                        durationText = row.defaultDurationMinutes.toString()
                    }
                    priceText = applyStandardPriceIfBlank(procedureTypes, opt.procedureTypeId, priceText)
                },
                enabled = !isSaving,
                optionLabel = { it.displayName },
                placeholder = "Tratamiento…",
                searchable = true,
                searchPlaceholder = "Buscar tratamiento…",
            )
            TreatmentPricingFields(
                procedureTypes = procedureTypes,
                selectedProcedureTypeId = selectedProcedure.procedureTypeId,
                priceText = priceText,
                onPriceTextChange = { priceText = it },
                treatmentStatus = treatmentStatus,
                onTreatmentStatusChange = { treatmentStatus = it },
                enabled = !isSaving,
            )
            AppVisitDateTimeFields(
                date = visitDate,
                onDateChange = { visitDate = it },
                hour = visitHour,
                minute = visitMinute,
                onTimeChange = { h, m ->
                    visitHour = h
                    visitMinute = m
                },
                enabled = !isSaving,
                dateLabel = "Fecha de la visita",
                timeLabel = "Hora",
            )
            Text(
                text = "Fecha y hora registrada: ${visitDateTime.format(VisitDateTimePreviewFormatter)} (${visitDateTime.format(IsoLocalDateTime)})",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = durationText,
                onValueChange = { durationText = it.filter { ch -> ch.isDigit() }.take(4) },
                label = "Duración estimada (minutos)",
                placeholder = "45",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            AppTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = "Motivo / propósito",
                placeholder = "Revisión, limpieza…",
                enabled = !isSaving,
            )
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
                placeholder = "Notas internas (opcional)",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            AppDropdownField(
                label = "Estado de la visita",
                options = visitStatuses,
                selected = visitStatus,
                onSelected = { visitStatus = it },
                enabled = !isSaving && visitStatuses.isNotEmpty(),
                optionLabel = { it.displayLabel },
                placeholder = "Estado…",
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
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSaving,
                )
                AppButton(
                    text = if (isSaving) "Guardando..." else "Crear visita",
                    onClick = {
                        val p = selectedPatient ?: return@AppButton
                        val d = selectedDoctor ?: return@AppButton
                        onSubmit(
                            AppointmentVisitRequest(
                                patientId = p.id,
                                primaryDoctorId = d.id,
                                visitDate = visitDate,
                                visitHour = visitHour,
                                visitMinute = visitMinute,
                                estimatedDurationMinutes = durationMinutes,
                                purpose = purpose,
                                notes = notes,
                                procedureTypeId = selectedProcedure.procedureTypeId,
                                status = visitStatus,
                                treatmentStatus =
                                    selectedProcedure.procedureTypeId?.let { treatmentStatus },
                                treatmentUnitPrice =
                                    selectedProcedure.procedureTypeId?.let { parseMoneyAmount(priceText) },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

@Composable
private fun EditVisitDialog(
    appointment: AppointmentRow,
    initialTreatment: PatientTreatmentRow? = null,
    patients: List<Patient>,
    doctors: List<Doctor>,
    procedureTypes: List<ProcedureTypeRow>,
    visitStatuses: List<AppointmentStatus>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (Int, AppointmentEditRequest) -> Unit,
) {
    val initialLdt = remember(appointment.id) { parseAppointmentScheduledAt(appointment.scheduledAt) }
    var visitDate by remember(appointment.id) { mutableStateOf(initialLdt.toLocalDate()) }
    var visitHour by remember(appointment.id) { mutableStateOf(initialLdt.hour) }
    var visitMinute by remember(appointment.id) { mutableStateOf(snapFormMinuteToStep5(initialLdt.minute)) }
    var selectedPatient by remember(appointment.id) { mutableStateOf(patients.find { it.id == appointment.patientId }) }
    var selectedDoctor by remember(appointment.id) { mutableStateOf(doctors.find { it.id == appointment.primaryDoctorId }) }
    var durationText by remember(appointment.id) {
        mutableStateOf(appointment.estimatedDurationMinutes?.toString() ?: "")
    }
    var purpose by remember(appointment.id) { mutableStateOf(appointment.purpose ?: "") }
    var notes by remember(appointment.id) { mutableStateOf(appointment.notes ?: "") }
    val statusChoices =
        remember(visitStatuses, appointment.status) {
            if (appointment.status in visitStatuses) visitStatuses else visitStatuses + appointment.status
        }
    var visitStatus by remember(appointment.id) { mutableStateOf(appointment.status) }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember(appointment.id, procedureOptions) {
        mutableStateOf(
            procedureOptions.find { it.procedureTypeId == appointment.procedureTypeId }
                ?: ProcedureTypeOption.none(),
        )
    }
    var priceText by remember(appointment.id, initialTreatment) {
        mutableStateOf(
            initialTreatment?.unitPrice?.let { formatMoney(it) }
                ?: initialTreatment?.standardPrice?.let { formatMoney(it) }
                ?: "",
        )
    }
    var treatmentStatus by remember(appointment.id, initialTreatment) {
        mutableStateOf(initialTreatment?.status ?: TreatmentStatus.PLANNED)
    }

    val visitDateTime =
        remember(visitDate, visitHour, visitMinute) {
            LocalDateTime.of(visitDate, LocalTime.of(visitHour, visitMinute))
        }
    val durationMinutes = durationText.trim().toIntOrNull()
    val canSubmit =
        selectedPatient != null &&
            selectedDoctor != null &&
            !isSaving &&
            patients.isNotEmpty() &&
            doctors.isNotEmpty()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Editar visita",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Actualiza fecha, hora, participantes, notas y el estado de la visita.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selectedPatient == null) {
                Text(
                    text = "Este paciente ya no está en el directorio; no se puede guardar hasta que exista de nuevo.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (selectedDoctor == null) {
                Text(
                    text = "El doctor asignado no está en el directorio; elige otro doctor.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            AppDropdownField(
                label = "Paciente",
                options = patients,
                selected = selectedPatient,
                onSelected = { selectedPatient = it },
                enabled = !isSaving && patients.isNotEmpty(),
                optionLabel = { "${it.fullName} (id ${it.id})" },
                placeholder = "Selecciona paciente…",
                searchable = true,
                searchPlaceholder = "Buscar por nombre…",
            )
            AppDropdownField(
                label = "Doctor",
                options = doctors,
                selected = selectedDoctor,
                onSelected = { selectedDoctor = it },
                enabled = !isSaving && doctors.isNotEmpty(),
                optionLabel = { it.fullName },
                placeholder = "Selecciona doctor…",
            )
            AppDropdownField(
                label = "Tratamiento (opcional)",
                options = procedureOptions,
                selected = selectedProcedure,
                onSelected = { opt ->
                    selectedProcedure = opt
                    val row = procedureTypes.find { it.id == opt.procedureTypeId }
                    if (row?.defaultDurationMinutes != null && durationText.isBlank()) {
                        durationText = row.defaultDurationMinutes.toString()
                    }
                    priceText = applyStandardPriceIfBlank(procedureTypes, opt.procedureTypeId, priceText)
                },
                enabled = !isSaving,
                optionLabel = { it.displayName },
                placeholder = "Tratamiento…",
                searchable = true,
                searchPlaceholder = "Buscar tratamiento…",
            )
            TreatmentPricingFields(
                procedureTypes = procedureTypes,
                selectedProcedureTypeId = selectedProcedure.procedureTypeId,
                priceText = priceText,
                onPriceTextChange = { priceText = it },
                treatmentStatus = treatmentStatus,
                onTreatmentStatusChange = { treatmentStatus = it },
                enabled = !isSaving,
            )
            AppVisitDateTimeFields(
                date = visitDate,
                onDateChange = { visitDate = it },
                hour = visitHour,
                minute = visitMinute,
                onTimeChange = { h, m ->
                    visitHour = h
                    visitMinute = m
                },
                enabled = !isSaving,
                dateLabel = "Fecha de la visita",
                timeLabel = "Hora",
            )
            Text(
                text = "Fecha y hora: ${visitDateTime.format(VisitDateTimePreviewFormatter)} (${visitDateTime.format(IsoLocalDateTime)})",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = durationText,
                onValueChange = { durationText = it.filter { ch -> ch.isDigit() }.take(4) },
                label = "Duración estimada (minutos)",
                placeholder = "45",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            AppTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = "Motivo / propósito",
                placeholder = "Revisión, limpieza…",
                enabled = !isSaving,
            )
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
                placeholder = "Notas internas (opcional)",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            AppDropdownField(
                label = "Estado de la visita",
                options = statusChoices,
                selected = visitStatus,
                onSelected = { visitStatus = it },
                enabled = !isSaving && statusChoices.isNotEmpty(),
                optionLabel = { it.displayLabel },
                placeholder = "Estado…",
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
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSaving,
                )
                AppButton(
                    text = if (isSaving) "Guardando..." else "Guardar cambios",
                    onClick = {
                        val p = selectedPatient ?: return@AppButton
                        val d = selectedDoctor ?: return@AppButton
                        onSubmit(
                            appointment.id,
                            AppointmentEditRequest(
                                patientId = p.id,
                                primaryDoctorId = d.id,
                                visitDate = visitDate,
                                visitHour = visitHour,
                                visitMinute = visitMinute,
                                estimatedDurationMinutes = durationMinutes,
                                purpose = purpose,
                                notes = notes,
                                procedureTypeId = selectedProcedure.procedureTypeId,
                                status = visitStatus,
                                treatmentStatus =
                                    selectedProcedure.procedureTypeId?.let { treatmentStatus },
                                treatmentUnitPrice =
                                    selectedProcedure.procedureTypeId?.let { parseMoneyAmount(priceText) },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
