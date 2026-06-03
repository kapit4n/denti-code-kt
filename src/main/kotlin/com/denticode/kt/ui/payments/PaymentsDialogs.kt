@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.payments

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
import com.denticode.kt.data.PatientPaymentRegisterRequest
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentPaymentOption
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
import com.denticode.kt.ui.procedureTypeDropdownOptions
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val IsoLocalDateTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val PaidAtPreviewFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
fun PaymentsNewPaymentDialog(
    patients: List<Patient>,
    procedureTypes: List<ProcedureTypeRow>,
    treatmentOptionsForPatient: (Int) -> List<TreatmentPaymentOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (patientId: Int, request: PatientPaymentRegisterRequest) -> Unit,
) {
    var selectedPatient by remember(patients) { mutableStateOf(patients.firstOrNull()) }
    val defaultPaid = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    var paidDate by remember { mutableStateOf(defaultPaid.toLocalDate()) }
    var paidHour by remember { mutableStateOf(defaultPaid.hour) }
    var paidMinute by remember { mutableStateOf(snapFormMinuteToStep5(defaultPaid.minute)) }
    var amountText by remember { mutableStateOf("") }
    val paymentMethodOptions: List<PaymentMethod?> = remember { listOf(null) + PaymentMethod.entries }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var note by remember { mutableStateOf("") }
    val procedureOptions = remember(procedureTypes) { procedureTypeDropdownOptions(procedureTypes) }
    var selectedProcedure by remember { mutableStateOf(com.denticode.kt.data.ProcedureTypeOption.none()) }
    val performedOptions =
        remember(selectedPatient) {
            val pid = selectedPatient?.id
            if (pid == null) {
                listOf(TreatmentPaymentOption.none())
            } else {
                listOf(TreatmentPaymentOption.none()) + treatmentOptionsForPatient(pid)
            }
        }
    var selectedPerformed by remember { mutableStateOf(TreatmentPaymentOption.none()) }

    val paidDateTime =
        remember(paidDate, paidHour, paidMinute) {
            LocalDateTime.of(paidDate, LocalTime.of(paidHour, paidMinute))
        }
    val paidAtIso = remember(paidDateTime) { paidDateTime.format(IsoLocalDateTime) }
    val amount = remember(amountText) { parseMoneyAmount(amountText) }
    val canSubmit = selectedPatient != null && amount != null && amount > 0 && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text("Nuevo pago", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Registra un cobro asociado a un paciente.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppDropdownField(
                label = "Paciente",
                options = patients,
                selected = selectedPatient,
                onSelected = {
                    selectedPatient = it
                    selectedPerformed = TreatmentPaymentOption.none()
                },
                enabled = !isSaving && patients.isNotEmpty(),
                optionLabel = { it.fullName },
                placeholder = "Seleccionar paciente…",
                searchable = true,
                searchPlaceholder = "Buscar paciente…",
            )
            if (performedOptions.size > 1) {
                AppDropdownField(
                    label = "Tratamiento realizado",
                    options = performedOptions,
                    selected = selectedPerformed,
                    onSelected = { opt ->
                        selectedPerformed = opt
                        if (!TreatmentPaymentOption.isNone(opt)) {
                            amountText = formatMoney(opt.amount)
                            selectedProcedure =
                                procedureOptions.find { it.procedureTypeId == opt.procedureTypeId }
                                    ?: com.denticode.kt.data.ProcedureTypeOption.none()
                        }
                    },
                    enabled = !isSaving && selectedPatient != null,
                    optionLabel = { it.label },
                    placeholder = "Vincular tratamiento…",
                )
            }
            AppDropdownField(
                label = "Tratamiento catálogo (opcional)",
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
                        val patient = selectedPatient ?: return@AppButton
                        val amt = amount ?: return@AppButton
                        onSubmit(
                            patient.id,
                            PatientPaymentRegisterRequest(
                                amount = amt,
                                method = selectedMethod,
                                paidAtIso = paidAtIso,
                                note = note,
                                procedureTypeId = selectedProcedure.procedureTypeId,
                                performedActionId = selectedPerformed.performedActionId.takeIf { it > 0 },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
