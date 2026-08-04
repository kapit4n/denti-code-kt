@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Checkbox
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
import com.denticode.kt.data.DentalRecordRegisterRequest
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.MedicalRecordRegisterRequest
import com.denticode.kt.data.MedicalRecordType
import com.denticode.kt.data.PatientDentalRecord
import com.denticode.kt.data.PatientMedicalRecord
import com.denticode.kt.data.PatientNoteRegisterRequest
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.medicalRecordTypeOptions
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate

// ── Medical history dialog ──────────────────────────────────────────────────

@Composable
fun MedicalRecordDialog(
    initial: PatientMedicalRecord?,
    doctors: List<Doctor>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (MedicalRecordRegisterRequest) -> Unit,
) {
    val recordTypeOptions = remember { medicalRecordTypeOptions() }
    var recordType by remember(initial) { mutableStateOf(initial?.recordType ?: MedicalRecordType.CONDITION) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    val pastDates = rememberPastOrTodaySelectableDates()
    var recordedDate by remember(initial) {
        mutableStateOf(
            initial?.recordedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now(),
        )
    }
    var selectedDoctor by remember(initial, doctors) {
        mutableStateOf(doctors.find { it.id == initial?.doctorId } ?: doctors.firstOrNull())
    }
    var isActive by remember(initial) { mutableStateOf(initial?.isActive ?: true) }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }

    val canSubmit = description.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                if (initial == null) "Registrar historial médico" else "Editar historial médico",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppDropdownField(
                label = "Tipo",
                options = recordTypeOptions,
                selected = recordType,
                onSelected = { recordType = it },
                enabled = !isSaving,
                optionLabel = { it.labelEs },
                placeholder = "Tipo de registro…",
            )
            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descripción",
                placeholder = "Alergia a la penicilina, hipertensión, etc.",
                enabled = !isSaving,
            )
            AppDatePickerField(
                label = "Fecha del registro",
                value = recordedDate,
                onValueChange = { recordedDate = it },
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                Text("Registro activo", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
            }
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas (opcional)",
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
                    text = if (isSaving) "Guardando..." else if (initial == null) "Registrar" else "Guardar",
                    onClick = {
                        onSubmit(
                            MedicalRecordRegisterRequest(
                                recordType = recordType,
                                description = description.trim(),
                                recordedAt = recordedDate.toString(),
                                doctorId = selectedDoctor?.id,
                                isActive = isActive,
                                notes = notes.trim().takeIf { it.isNotEmpty() },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

// ── Dental history dialog ──────────────────────────────────────────────────

@Composable
fun DentalRecordDialog(
    initial: PatientDentalRecord?,
    doctors: List<Doctor>,
    procedureTypes: List<ProcedureTypeRow>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (DentalRecordRegisterRequest) -> Unit,
) {
    var toothNumber by remember(initial) { mutableStateOf(initial?.toothNumber.orEmpty()) }
    var toothQuadrant by remember(initial) { mutableStateOf(initial?.toothQuadrant.orEmpty()) }
    var diagnosis by remember(initial) { mutableStateOf(initial?.diagnosis.orEmpty()) }
    var treatmentPerformed by remember(initial) { mutableStateOf(initial?.treatmentPerformed.orEmpty()) }
    val pastDates = rememberPastOrTodaySelectableDates()
    var recordedDate by remember(initial) {
        mutableStateOf(
            initial?.recordedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now(),
        )
    }
    var selectedDoctor by remember(initial, doctors) {
        mutableStateOf(doctors.find { it.id == initial?.doctorId } ?: doctors.firstOrNull())
    }
    var selectedProcedure by remember(initial, procedureTypes) {
        mutableStateOf(procedureTypes.find { it.id == initial?.procedureTypeId })
    }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }

    val canSubmit = diagnosis.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                if (initial == null) "Registrar historial dental" else "Editar historial dental",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                AppTextField(
                    value = toothNumber,
                    onValueChange = { toothNumber = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = "Pieza (opcional)",
                    placeholder = "24",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = toothQuadrant,
                    onValueChange = { toothQuadrant = it },
                    label = "Cuadrante (opcional)",
                    placeholder = "Sup. derecha",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
            AppTextField(
                value = diagnosis,
                onValueChange = { diagnosis = it },
                label = "Diagnóstico",
                placeholder = "Caries profunda, periodontitis, etc.",
                enabled = !isSaving,
            )
            AppTextField(
                value = treatmentPerformed,
                onValueChange = { treatmentPerformed = it },
                label = "Tratamiento realizado (opcional)",
                placeholder = "Obturación compuesta",
                enabled = !isSaving,
            )
            AppDropdownField(
                label = "Tratamiento del catálogo (opcional)",
                options = procedureTypes,
                selected = selectedProcedure,
                onSelected = { selectedProcedure = it },
                enabled = !isSaving && procedureTypes.isNotEmpty(),
                optionLabel = { it.name },
                placeholder = "Tratamiento…",
                searchable = true,
                searchPlaceholder = "Buscar tratamiento…",
            )
            AppDatePickerField(
                label = "Fecha del registro",
                value = recordedDate,
                onValueChange = { recordedDate = it },
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
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas (opcional)",
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
                    text = if (isSaving) "Guardando..." else if (initial == null) "Registrar" else "Guardar",
                    onClick = {
                        onSubmit(
                            DentalRecordRegisterRequest(
                                toothNumber = toothNumber.trim().takeIf { it.isNotEmpty() },
                                toothQuadrant = toothQuadrant.trim().takeIf { it.isNotEmpty() },
                                diagnosis = diagnosis.trim(),
                                treatmentPerformed = treatmentPerformed.trim().takeIf { it.isNotEmpty() },
                                procedureTypeId = selectedProcedure?.id,
                                recordedAt = recordedDate.toString(),
                                doctorId = selectedDoctor?.id,
                                notes = notes.trim().takeIf { it.isNotEmpty() },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

// ── Note dialog ─────────────────────────────────────────────────────────────

@Composable
fun PatientNoteDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientNoteRegisterRequest) -> Unit,
) {
    var body by remember { mutableStateOf("") }
    var authorLabel by remember { mutableStateOf("Recepción") }
    var isPinned by remember { mutableStateOf(false) }

    val canSubmit = body.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 480.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Nueva nota",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppTextArea(
                value = body,
                onValueChange = { body = it },
                label = "Contenido",
                placeholder = "Observaciones de la atención…",
                enabled = !isSaving,
                minLines = 3,
                maxLines = 6,
            )
            AppTextField(
                value = authorLabel,
                onValueChange = { authorLabel = it },
                label = "Autor",
                placeholder = "Recepción",
                enabled = !isSaving,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPinned, onCheckedChange = { isPinned = it }, enabled = !isSaving)
                Text("Fijar nota", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
            }
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Guardar nota",
                    onClick = {
                        onSubmit(
                            PatientNoteRegisterRequest(
                                body = body.trim(),
                                authorLabel = authorLabel.trim().ifBlank { "Recepción" },
                                isPinned = isPinned,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
