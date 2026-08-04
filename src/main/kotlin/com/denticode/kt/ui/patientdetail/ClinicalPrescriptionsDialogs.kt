@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.FollowUpRegisterRequest
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.PrescriptionRegisterRequest
import com.denticode.kt.data.followUpStatusOptions
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.defaultAppointmentDateShortcuts
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate

// ── Prescription dialog ────────────────────────────────────────────────────

@Composable
fun PrescriptionDialog(
    doctors: List<Doctor>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PrescriptionRegisterRequest) -> Unit,
) {
    var medicine by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    val pastDates = rememberPastOrTodaySelectableDates()
    var prescribedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedDoctor by remember(doctors) { mutableStateOf(doctors.firstOrNull()) }

    val canSubmit =
        medicine.isNotBlank() && dosage.isNotBlank() && frequency.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Nueva receta",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppTextField(
                value = medicine,
                onValueChange = { medicine = it },
                label = "Medicamento",
                placeholder = "Amoxicilina 500 mg",
                enabled = !isSaving,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                AppTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = "Dosis",
                    placeholder = "1 cápsula",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = "Frecuencia",
                    placeholder = "Cada 8 horas",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
            AppTextArea(
                value = instructions,
                onValueChange = { instructions = it },
                label = "Indicaciones (opcional)",
                placeholder = "Tomar durante 7 días con comida.",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            AppDatePickerField(
                label = "Fecha de prescripción",
                value = prescribedDate,
                onValueChange = { prescribedDate = it },
                enabled = !isSaving,
                selectableDates = pastDates,
                shortcuts = null,
            )
            AppDropdownField(
                label = "Doctor (opcional)",
                options = doctors,
                selected = selectedDoctor,
                onSelected = { selectedDoctor = it },
                enabled = !isSaving && doctors.isNotEmpty(),
                optionLabel = { it.fullName },
                placeholder = "Selecciona doctor…",
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
                    text = if (isSaving) "Guardando..." else "Registrar receta",
                    onClick = {
                        onSubmit(
                            PrescriptionRegisterRequest(
                                medicine = medicine.trim(),
                                dosage = dosage.trim(),
                                frequency = frequency.trim(),
                                instructions = instructions.trim().takeIf { it.isNotEmpty() },
                                prescribedAt = prescribedDate.toString(),
                                doctorId = selectedDoctor?.id,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

// ── Follow-up dialog ───────────────────────────────────────────────────────

@Composable
fun FollowUpDialog(
    appointmentLinkOptions: List<TreatmentAppointmentLinkOption>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (FollowUpRegisterRequest) -> Unit,
) {
    var dueDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var notes by remember { mutableStateOf("") }
    val statusOptions = remember { followUpStatusOptions() }
    var selectedStatus by remember { mutableStateOf(FollowUpStatus.PENDING) }
    val linkOptions =
        remember(appointmentLinkOptions) {
            if (appointmentLinkOptions.isEmpty()) {
                listOf(TreatmentAppointmentLinkOption.createNewAtTreatmentDate())
            } else {
                appointmentLinkOptions
            }
        }
    var selectedLink by remember(linkOptions) {
        mutableStateOf(linkOptions.firstOrNull() ?: TreatmentAppointmentLinkOption.createNewAtTreatmentDate())
    }

    val canSubmit = !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Nuevo seguimiento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppDatePickerField(
                label = "Fecha de vencimiento",
                value = dueDate,
                onValueChange = { dueDate = it },
                enabled = !isSaving,
                shortcuts = defaultAppointmentDateShortcuts(),
            )
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas (opcional)",
                placeholder = "Revisar evolución del tratamiento…",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            AppDropdownField(
                label = "Estado",
                options = statusOptions,
                selected = selectedStatus,
                onSelected = { selectedStatus = it },
                enabled = !isSaving,
                optionLabel = { it.labelEs },
                placeholder = "Estado…",
            )
            AppDropdownField(
                label = "Vincular a cita (opcional)",
                options = linkOptions,
                selected = selectedLink,
                onSelected = { selectedLink = it },
                enabled = !isSaving,
                optionLabel = { it.label },
                placeholder = "Selecciona cita…",
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
                    text = if (isSaving) "Guardando..." else "Registrar seguimiento",
                    onClick = {
                        onSubmit(
                            FollowUpRegisterRequest(
                                dueDate = dueDate.toString(),
                                notes = notes.trim().takeIf { it.isNotEmpty() },
                                status = selectedStatus,
                                appointmentId = selectedLink.appointmentId,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
