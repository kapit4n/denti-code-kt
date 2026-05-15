@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.data.ProcedureTypeOption
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.visitStatusOptions
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.feedback.StatusChip
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.models.StatusKind
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val IsoLocalDateTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

private val VisitDateTimePreviewFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale("es", "ES"))

private val VisitMinuteOptions: List<Int> = (0..55 step 5).toList()

private fun snapMinuteToStep5(minute: Int): Int =
    VisitMinuteOptions.minByOrNull { abs(it - minute) } ?: 0

@Composable
fun AppointmentsScreen(repo: DentiRepository) {
    val visitStatuses = remember { visitStatusOptions() }
    var rows by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    var showCreateVisit by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<AppointmentRow?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saveEditError by remember { mutableStateOf<String?>(null) }
    var patients by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var procedureTypes by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            rows = repo.listAppointments(200)
            patients = repo.listPatients()
            doctors = repo.listDoctors()
            procedureTypes = repo.listProcedureTypes()
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PageHeader(
            title = "Citas",
            subtitle = "Citas con paciente y doctor principal (modelo Appointment de Denti-Code).",
            modifier = Modifier.fillMaxWidth(),
            actions = {
                AppButton(
                    text = "Nueva visita",
                    onClick = {
                        saveError = null
                        showCreateVisit = true
                    },
                )
            },
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(rows, key = { it.id }) { a ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            Text(
                                a.scheduledAt,
                                style = AppTypography.CardTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "${a.patientName} · ${a.doctorName}",
                                style = AppTypography.Body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            a.procedureTypeName?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    "Tratamiento: $it",
                                    style = AppTypography.BodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            a.purpose?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val kind =
                                when (a.status) {
                                    AppointmentStatus.COMPLETED -> StatusKind.Success
                                    AppointmentStatus.CANCELLED,
                                    AppointmentStatus.NO_SHOW,
                                    -> StatusKind.Error
                                    AppointmentStatus.IN_PROGRESS -> StatusKind.Info
                                    else -> StatusKind.Neutral
                                }
                            StatusChip(text = a.status.displayLabel, kind = kind)
                        }
                        AppOutlinedButton(
                            text = "Editar",
                            onClick = {
                                saveEditError = null
                                editingAppointment = a
                            },
                        )
                    }
                }
            }
        }
    }

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
                            repo.listAppointments(200)
                        }
                    }.onSuccess { refreshed ->
                        rows = refreshed
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
            patients = patients,
            doctors = doctors,
            procedureTypes = procedureTypes,
            visitStatuses = visitStatuses,
            isSaving = isEditSaving,
            errorMessage = saveEditError,
            onDismiss = {
                if (!isEditSaving) {
                    editingAppointment = null
                    saveEditError = null
                }
            },
            onSubmit = { appointmentId, request ->
                isEditSaving = true
                saveEditError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.updateAppointment(appointmentId, request)
                            repo.listAppointments(200)
                        }
                    }.onSuccess { refreshed ->
                        rows = refreshed
                        editingAppointment = null
                    }.onFailure { error ->
                        saveEditError = error.message ?: "No se pudo guardar la visita."
                    }
                    isEditSaving = false
                }
            },
        )
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
    var visitMinute by remember { mutableStateOf(snapMinuteToStep5(defaultVisit.minute)) }
    var durationText by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember { mutableStateOf(ProcedureTypeOption.none()) }

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
                },
                enabled = !isSaving,
                optionLabel = { it.displayName },
                placeholder = "Tratamiento…",
                searchable = true,
                searchPlaceholder = "Buscar tratamiento…",
            )
            AppDatePickerField(
                label = "Fecha de la visita",
                value = visitDate,
                onValueChange = { visitDate = it },
                enabled = !isSaving,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppDropdownField(
                    label = "Hora",
                    options = (0..23).toList(),
                    selected = visitHour,
                    onSelected = { visitHour = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    placeholder = "Hora…",
                    modifier = Modifier.weight(1f),
                )
                AppDropdownField(
                    label = "Minuto",
                    options = VisitMinuteOptions,
                    selected = visitMinute,
                    onSelected = { visitMinute = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    placeholder = "Min…",
                    modifier = Modifier.weight(1f),
                )
            }
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
    var visitMinute by remember(appointment.id) { mutableStateOf(snapMinuteToStep5(initialLdt.minute)) }
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
                },
                enabled = !isSaving,
                optionLabel = { it.displayName },
                placeholder = "Tratamiento…",
                searchable = true,
                searchPlaceholder = "Buscar tratamiento…",
            )
            AppDatePickerField(
                label = "Fecha de la visita",
                value = visitDate,
                onValueChange = { visitDate = it },
                enabled = !isSaving,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppDropdownField(
                    label = "Hora",
                    options = (0..23).toList(),
                    selected = visitHour,
                    onSelected = { visitHour = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    placeholder = "Hora…",
                    modifier = Modifier.weight(1f),
                )
                AppDropdownField(
                    label = "Minuto",
                    options = VisitMinuteOptions,
                    selected = visitMinute,
                    onSelected = { visitMinute = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    placeholder = "Min…",
                    modifier = Modifier.weight(1f),
                )
            }
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
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
