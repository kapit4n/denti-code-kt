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
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.AppointmentVisitRequest
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientPaymentRegisterRequest
import com.denticode.kt.data.PatientTreatmentRegisterRequest
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.data.ProcedureTypeOption
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentPaymentOption
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.parseAppointmentScheduledAt
import com.denticode.kt.ui.procedureTypeDropdownOptions
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.AppVisitDateTimeFields
import com.denticode.kt.ui.components.inputs.defaultPaymentDateShortcuts
import com.denticode.kt.ui.components.inputs.snapFormMinuteToStep5
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.parseMoneyAmount
import com.denticode.kt.ui.treatments.TreatmentPricingFields
import com.denticode.kt.ui.treatments.applyStandardPriceIfBlank
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val IsoLocalDateTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

private val PaidAtPreviewFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale("es", "ES"))

@Composable
fun PatientNewVisitDialog(
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
    val durationMinutes = durationText.trim().toIntOrNull()
    val canSubmit = selectedDoctor != null && !isSaving && doctors.isNotEmpty()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Nueva cita", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Paciente: ${patient.fullName}",
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
                "Fecha y hora: ${visitDateTime.format(PaidAtPreviewFormatter)}",
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
                                treatmentStatus =
                                    selectedProcedure.procedureTypeId?.let {
                                        treatmentStatus
                                    },
                                treatmentUnitPrice =
                                    selectedProcedure.procedureTypeId?.let {
                                        parseMoneyAmount(priceText)
                                    },
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
fun PatientNewPaymentDialog(
    procedureTypes: List<ProcedureTypeRow>,
    treatmentOptions: List<TreatmentPaymentOption> = emptyList(),
    initialAmountText: String? = null,
    initialProcedureTypeId: Int? = null,
    initialPerformedActionId: Int? = null,
    initialNote: String? = null,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientPaymentRegisterRequest) -> Unit,
) {
    val defaultPaid = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    var paidDate by remember { mutableStateOf(defaultPaid.toLocalDate()) }
    var paidHour by remember { mutableStateOf(defaultPaid.hour) }
    var paidMinute by remember { mutableStateOf(snapFormMinuteToStep5(defaultPaid.minute)) }
    var amountText by remember(initialAmountText) { mutableStateOf(initialAmountText.orEmpty()) }
    val paymentMethodOptions: List<PaymentMethod?> = remember { listOf(null) + PaymentMethod.entries }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var note by remember(initialNote) { mutableStateOf(initialNote.orEmpty()) }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember(initialProcedureTypeId, procedureOptions) {
        mutableStateOf(
            procedureOptions.find { it.procedureTypeId == initialProcedureTypeId }
                ?: ProcedureTypeOption.none(),
        )
    }
    val performedOptions = remember(treatmentOptions) { listOf(TreatmentPaymentOption.none()) + treatmentOptions }
    var selectedPerformed by remember(initialPerformedActionId, performedOptions) {
        mutableStateOf(
            performedOptions.find { it.performedActionId == initialPerformedActionId }
                ?: TreatmentPaymentOption.none(),
        )
    }

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
            Text("Registrar pago", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            if (treatmentOptions.isNotEmpty()) {
                AppDropdownField(
                    label = "Tratamiento con pago pendiente",
                    options = performedOptions,
                    selected = selectedPerformed,
                    onSelected = { opt ->
                        selectedPerformed = opt
                        if (!TreatmentPaymentOption.isNone(opt)) {
                            amountText = formatMoney(opt.amount)
                            selectedProcedure =
                                procedureOptions.find { it.procedureTypeId == opt.procedureTypeId }
                                    ?: ProcedureTypeOption.none()
                        }
                    },
                    enabled = !isSaving,
                    optionLabel = { it.label },
                    placeholder = "Vincular tratamiento…",
                )
            } else {
                Text(
                    "No hay tratamientos con pago pendiente para este paciente.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppDropdownField(
                label = "Catálogo (opcional)",
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
            AppVisitDateTimeFields(
                date = paidDate,
                onDateChange = { paidDate = it },
                hour = paidHour,
                minute = paidMinute,
                onTimeChange = { h, m ->
                    paidHour = h
                    paidMinute = m
                },
                enabled = !isSaving,
                dateLabel = "Fecha del pago",
                timeLabel = "Hora",
                dateShortcuts = defaultPaymentDateShortcuts(),
            )
            Text(
                "Fecha y hora del cobro: ${paidDateTime.format(PaidAtPreviewFormatter)}",
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
                                performedActionId =
                                    selectedPerformed.performedActionId.takeIf { it > 0 },
                                appointmentId = null,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

data class TreatmentAppointmentLinkOption(
    val appointmentId: Int?,
    val label: String,
) {
    companion object {
        fun createNewAtTreatmentDate(): TreatmentAppointmentLinkOption =
            TreatmentAppointmentLinkOption(null, "Nueva cita en la fecha del tratamiento")
    }
}

fun buildTreatmentAppointmentLinkOptions(appointments: List<AppointmentRow>): List<TreatmentAppointmentLinkOption> {
    val active =
        appointments.filter {
            it.status !in setOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
        }
    return listOf(TreatmentAppointmentLinkOption.createNewAtTreatmentDate()) +
        active.map { ap ->
            val ldt = parseAppointmentScheduledAt(ap.scheduledAt)
            val treatment = ap.procedureTypeName?.takeIf { it.isNotBlank() } ?: ap.purpose ?: "Consulta"
            TreatmentAppointmentLinkOption(
                appointmentId = ap.id,
                label = "#${ap.id} · ${ldt.toLocalDate()} ${ldt.hour}:${"%02d".format(ldt.minute)} · $treatment",
            )
        }
}

@Composable
fun RegisterTreatmentDialog(
    patientLabel: String,
    doctors: List<Doctor>,
    procedureTypes: List<ProcedureTypeRow>,
    appointmentLinkOptions: List<TreatmentAppointmentLinkOption>,
    lockedAppointmentId: Int? = null,
    initialDoctorId: Int? = null,
    initialProcedureTypeId: Int? = null,
    initialPriceText: String? = null,
    initialTreatmentStatus: TreatmentStatus = TreatmentStatus.PLANNED,
    initialActionDate: LocalDate? = null,
    initialActionHour: Int? = null,
    initialActionMinute: Int? = null,
    initialNotes: String? = null,
    isUpdate: Boolean = false,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientTreatmentRegisterRequest) -> Unit,
) {
    val defaultAction = remember {
        LocalDateTime.now().withSecond(0).withNano(0)
    }
    var selectedDoctor by remember(initialDoctorId, doctors) {
        mutableStateOf(doctors.find { it.id == initialDoctorId } ?: doctors.firstOrNull())
    }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember(initialProcedureTypeId, procedureOptions) {
        mutableStateOf(
            procedureOptions.find { it.procedureTypeId == initialProcedureTypeId }
                ?: ProcedureTypeOption.none(),
        )
    }
    var priceText by remember(initialPriceText, initialProcedureTypeId) {
        mutableStateOf(initialPriceText.orEmpty())
    }
    var treatmentStatus by remember(initialTreatmentStatus) { mutableStateOf(initialTreatmentStatus) }
    var actionDate by remember(initialActionDate) {
        mutableStateOf(initialActionDate ?: defaultAction.toLocalDate())
    }
    var actionHour by remember(initialActionHour) {
        mutableStateOf(initialActionHour ?: defaultAction.hour)
    }
    var actionMinute by remember(initialActionMinute) {
        mutableStateOf(initialActionMinute ?: snapFormMinuteToStep5(defaultAction.minute))
    }
    var notes by remember(initialNotes) { mutableStateOf(initialNotes.orEmpty()) }
    val linkOptions =
        remember(appointmentLinkOptions, lockedAppointmentId) {
            if (lockedAppointmentId != null) {
                emptyList()
            } else if (appointmentLinkOptions.isEmpty()) {
                listOf(TreatmentAppointmentLinkOption.createNewAtTreatmentDate())
            } else {
                appointmentLinkOptions
            }
        }
    var selectedLink by remember(linkOptions) {
        mutableStateOf(linkOptions.firstOrNull() ?: TreatmentAppointmentLinkOption.createNewAtTreatmentDate())
    }

    val canSubmit =
        selectedDoctor != null &&
            selectedProcedure.procedureTypeId != null &&
            !isSaving &&
            doctors.isNotEmpty() &&
            procedureTypes.isNotEmpty()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                if (isUpdate) "Actualizar tratamiento" else "Registrar tratamiento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                patientLabel,
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (lockedAppointmentId != null) {
                Text(
                    "Vinculado a cita #$lockedAppointmentId",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (linkOptions.isNotEmpty()) {
                AppDropdownField(
                    label = "Vincular a cita",
                    options = linkOptions,
                    selected = selectedLink,
                    onSelected = { selectedLink = it },
                    enabled = !isSaving,
                    optionLabel = { it.label },
                    placeholder = "Selecciona cita…",
                )
            }
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
                label = "Tratamiento",
                options = procedureOptions,
                selected = selectedProcedure,
                onSelected = { opt ->
                    selectedProcedure = opt
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
                date = actionDate,
                onDateChange = { actionDate = it },
                hour = actionHour,
                minute = actionMinute,
                onTimeChange = { h, m ->
                    actionHour = h
                    actionMinute = m
                },
                enabled = !isSaving,
                dateLabel = "Fecha del tratamiento",
                timeLabel = "Hora",
            )
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas clínicas (opcional)",
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
                    text = if (isSaving) "Guardando..." else if (isUpdate) "Guardar" else "Registrar",
                    onClick = {
                        val doctor = selectedDoctor ?: return@AppButton
                        val procId = selectedProcedure.procedureTypeId ?: return@AppButton
                        onSubmit(
                            PatientTreatmentRegisterRequest(
                                patientId = 0,
                                primaryDoctorId = doctor.id,
                                procedureTypeId = procId,
                                actionDate = actionDate,
                                actionHour = actionHour,
                                actionMinute = actionMinute,
                                status = treatmentStatus,
                                unitPrice = parseMoneyAmount(priceText),
                                descriptionNotes = notes.trim().takeIf { it.isNotEmpty() },
                                appointmentId = lockedAppointmentId ?: selectedLink?.appointmentId,
                                createAppointmentIfMissing = lockedAppointmentId == null &&
                                    selectedLink?.appointmentId == null,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
