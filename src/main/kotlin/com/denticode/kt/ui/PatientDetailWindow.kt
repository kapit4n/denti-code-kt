@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.AppointmentVisitRequest
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.PatientPaymentRegisterRequest
import com.denticode.kt.data.PaymentMethod
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

private val PaidAtPreviewFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale("es", "ES"))

private val MinuteStep5: List<Int> = (0..55 step 5).toList()

private fun snapMinuteToStep5(minute: Int): Int =
    MinuteStep5.minByOrNull { abs(it - minute) } ?: 0

@Composable
fun PatientDetailWindow(
    repo: DentiRepository,
    patient: Patient,
    onClose: () -> Unit,
) {
    val visitStatuses = remember { visitStatusOptions() }
    val scope = rememberCoroutineScope()
    var appointments by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    var payments by remember { mutableStateOf<List<PatientLedgerPayment>>(emptyList()) }
    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var procedureTypes by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    var refreshNonce by remember { mutableStateOf(0) }
    var showNewVisit by remember { mutableStateOf(false) }
    var showNewPayment by remember { mutableStateOf(false) }
    var saveVisitBusy by remember { mutableStateOf(false) }
    var savePaymentBusy by remember { mutableStateOf(false) }
    var saveVisitError by remember { mutableStateOf<String?>(null) }
    var savePaymentError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(patient.id, refreshNonce) {
        withContext(Dispatchers.IO) {
            appointments = repo.listAppointmentsForPatient(patient.id)
            payments = repo.listPaymentsForPatient(patient.id)
            doctors = repo.listDoctors()
            procedureTypes = repo.listProcedureTypes()
        }
    }

    val activeDoctors = remember(doctors) { doctors.filter { it.isActive } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(
                            text = patient.fullName,
                            style = AppTypography.PageTitle,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Citas y pagos vinculados a este paciente.",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AppOutlinedButton(text = "Cerrar", onClick = onClose)
                }
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(
                            "Nac. ${patient.dateOfBirth} · ${patient.contactPhone}",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        patient.email?.let {
                            Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        patient.medicalHistorySummary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                "Resumen: $it",
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Citas (${appointments.size})",
                        style = AppTypography.SectionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AppButton(
                        text = "Registrar cita",
                        onClick = {
                            saveVisitError = null
                            showNewVisit = true
                        },
                        enabled = activeDoctors.isNotEmpty(),
                    )
                }
            }
            if (activeDoctors.isEmpty()) {
                item {
                    Text(
                        "No hay doctores activos; no se pueden registrar citas desde aquí.",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (appointments.isEmpty()) {
                item {
                    Text(
                        "No hay citas registradas para este paciente.",
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(appointments, key = { "appointment-${it.id}" }) { a ->
                    PatientVisitCard(a)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Pagos (${payments.size})",
                        style = AppTypography.SectionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = AppSpacing.sm),
                    )
                    AppButton(
                        text = "Registrar pago",
                        onClick = {
                            savePaymentError = null
                            showNewPayment = true
                        },
                    )
                }
            }
            if (payments.isEmpty()) {
                item {
                    Text(
                        "No hay pagos registrados para este paciente.",
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(payments, key = { "payment-${it.id}" }) { pay ->
                    PatientPaymentCard(pay)
                }
            }
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
}

@Composable
private fun PatientNewVisitDialog(
    patient: Patient,
    doctors: List<Doctor>,
    procedureTypes: List<ProcedureTypeRow>,
    visitStatuses: List<AppointmentStatus>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (AppointmentVisitRequest) -> Unit,
) {
    var selectedDoctor by remember { mutableStateOf<Doctor?>(doctors.firstOrNull()) }
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
    val durationMinutes = durationText.trim().toIntOrNull()
    val canSubmit = selectedDoctor != null && !isSaving && doctors.isNotEmpty()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Nueva cita",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Paciente: ${patient.fullName}",
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    options = MinuteStep5,
                    selected = visitMinute,
                    onSelected = { visitMinute = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    placeholder = "Min…",
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Fecha y hora: ${visitDateTime.format(PaidAtPreviewFormatter)} (${visitDateTime.format(IsoLocalDateTime)})",
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
                enabled = !isSaving,
            )
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
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
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Registrar cita",
                    onClick = {
                        val d = selectedDoctor ?: return@AppButton
                        onSubmit(
                            AppointmentVisitRequest(
                                patientId = patient.id,
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
private fun PatientNewPaymentDialog(
    procedureTypes: List<ProcedureTypeRow>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientPaymentRegisterRequest) -> Unit,
) {
    val defaultPaid = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    var paidDate by remember { mutableStateOf(defaultPaid.toLocalDate()) }
    var paidHour by remember { mutableStateOf(defaultPaid.hour) }
    var paidMinute by remember { mutableStateOf(snapMinuteToStep5(defaultPaid.minute)) }
    var amountText by remember { mutableStateOf("") }
    val paymentMethodOptions: List<PaymentMethod?> = remember { listOf(null) + PaymentMethod.entries }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var note by remember { mutableStateOf("") }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember { mutableStateOf(ProcedureTypeOption.none()) }

    val paidDateTime =
        remember(paidDate, paidHour, paidMinute) {
            LocalDateTime.of(paidDate, LocalTime.of(paidHour, paidMinute))
        }
    val paidAtIso = remember(paidDateTime) { paidDateTime.format(IsoLocalDateTime) }
    val amount = remember(amountText) { parseMoneyAmount(amountText) }
    val canSubmit = amount != null && amount > 0 && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 480.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Registrar pago",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppDropdownField(
                label = "Tratamiento (opcional)",
                options = procedureOptions,
                selected = selectedProcedure,
                onSelected = { opt ->
                    selectedProcedure = opt
                    val row = procedureTypes.find { it.id == opt.procedureTypeId }
                    val sp = row?.standardPrice
                    if (sp != null) {
                        amountText = formatMoney(sp)
                    }
                },
                enabled = !isSaving,
                optionLabel = { it.displayName },
                placeholder = "Tratamiento…",
                searchable = true,
                searchPlaceholder = "Buscar tratamiento…",
            )
            AppTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "Importe",
                placeholder = "120,50",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AppDropdownField(
                label = "Método de pago",
                options = paymentMethodOptions,
                selected = selectedMethod,
                onSelected = { selectedMethod = it },
                enabled = !isSaving,
                optionLabel = { m -> m?.displayLabel ?: "Sin especificar" },
                placeholder = "Método…",
            )
            AppDatePickerField(
                label = "Fecha del pago",
                value = paidDate,
                onValueChange = { paidDate = it },
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
                    selected = paidHour,
                    onSelected = { paidHour = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    modifier = Modifier.weight(1f),
                )
                AppDropdownField(
                    label = "Minuto",
                    options = MinuteStep5,
                    selected = paidMinute,
                    onSelected = { paidMinute = it },
                    enabled = !isSaving,
                    optionLabel = { "%02d".format(it) },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Fecha y hora del cobro: ${paidDateTime.format(PaidAtPreviewFormatter)} (${paidDateTime.format(IsoLocalDateTime)})",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextArea(
                value = note,
                onValueChange = { note = it },
                label = "Nota (opcional)",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Registrar pago",
                    onClick = {
                        val amt = amount ?: return@AppButton
                        onSubmit(
                            PatientPaymentRegisterRequest(
                                amount = amt,
                                method = selectedMethod,
                                paidAtIso = paidAtIso,
                                note = note,
                                procedureTypeId = selectedProcedure.procedureTypeId,
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
private fun PatientVisitCard(a: AppointmentRow) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                a.scheduledAt,
                style = AppTypography.CardTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                a.doctorName,
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
            a.notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "Notas: $it",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.outline,
                )
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
    }
}

@Composable
private fun PatientPaymentCard(p: PatientLedgerPayment) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                formatMoney(p.amount),
                style = AppTypography.MetricMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "${p.paidAt} · ${p.method?.displayLabel ?: "—"}",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            p.procedureTypeName?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "Tratamiento: $it",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            p.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
