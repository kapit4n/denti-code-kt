@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.DentalRecordRegisterRequest
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.FollowUpRegisterRequest
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.MedicalRecordRegisterRequest
import com.denticode.kt.data.MedicalRecordType
import com.denticode.kt.data.PatientDentalRecord
import com.denticode.kt.data.PatientDocumentRegisterRequest
import com.denticode.kt.data.PatientMedicalRecord
import com.denticode.kt.data.PatientNoteRegisterRequest
import com.denticode.kt.data.PrescriptionRegisterRequest
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentPlan
import com.denticode.kt.data.TreatmentPlanPhase
import com.denticode.kt.data.TreatmentPlanPhaseRegisterRequest
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanRegisterRequest
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.data.documentCategoryOptions
import com.denticode.kt.data.followUpStatusOptions
import com.denticode.kt.data.medicalRecordTypeOptions
import com.denticode.kt.data.treatmentPlanPhaseStatusOptions
import com.denticode.kt.data.treatmentPlanStatusOptions
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
import com.denticode.kt.ui.parseMoneyAmount
import java.time.LocalDate

enum class ClinicalDeleteKind {
    MEDICAL,
    DENTAL,
    NOTE,
    PRESCRIPTION,
    FOLLOW_UP,
    DOCUMENT,
    TREATMENT_PLAN,
    TREATMENT_PLAN_PHASE,
}

data class DeleteClinicalTarget(
    val kind: ClinicalDeleteKind,
    val id: Int,
    val title: String,
    val message: String,
)

@Composable
fun DeleteClinicalConfirmDialog(
    target: DeleteClinicalTarget,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                target.title,
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                target.message,
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Eliminando..." else "Eliminar",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}

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

@Composable
fun PatientDocumentDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientDocumentRegisterRequest) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    val categoryOptions = remember { documentCategoryOptions() }
    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var fileName by remember { mutableStateOf("") }
    var fileSizeText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val canSubmit = title.isNotBlank() && fileName.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Registrar documento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Por ahora se registran los metadatos del documento. La carga física del archivo llegará en una fase posterior.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título",
                placeholder = "Radiografía panorámica",
                enabled = !isSaving,
            )
            AppDropdownField(
                label = "Categoría",
                options = categoryOptions,
                selected = selectedCategory,
                onSelected = { selectedCategory = it },
                enabled = !isSaving,
                optionLabel = { it.labelEs },
                placeholder = "Categoría…",
            )
            AppTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = "Nombre del archivo",
                placeholder = "panoramica_2024.pdf",
                enabled = !isSaving,
            )
            AppTextField(
                value = fileSizeText,
                onValueChange = { fileSizeText = it.filter { ch -> ch.isDigit() }.take(12) },
                label = "Tamaño en bytes (opcional)",
                placeholder = "0",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    text = if (isSaving) "Guardando..." else "Registrar documento",
                    onClick = {
                        onSubmit(
                            PatientDocumentRegisterRequest(
                                title = title.trim(),
                                category = selectedCategory,
                                fileName = fileName.trim(),
                                fileSize = fileSizeText.toLongOrNull() ?: 0,
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

@Composable
fun TreatmentPlanDialog(
    initial: TreatmentPlan?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (TreatmentPlanRegisterRequest) -> Unit,
) {
    val statusOptions = remember { treatmentPlanStatusOptions() }
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var selectedStatus by remember(initial) {
        mutableStateOf(initial?.status ?: TreatmentPlanStatus.DRAFT)
    }

    val canSubmit = title.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                if (initial == null) "Nuevo plan de tratamiento" else "Editar plan de tratamiento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título",
                placeholder = "Rehabilitación oral completa",
                enabled = !isSaving,
            )
            AppTextArea(
                value = description,
                onValueChange = { description = it },
                label = "Descripción (opcional)",
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
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else if (initial == null) "Crear plan" else "Guardar cambios",
                    onClick = {
                        onSubmit(
                            TreatmentPlanRegisterRequest(
                                title = title.trim(),
                                description = description.trim().takeIf { it.isNotEmpty() },
                                status = selectedStatus,
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
fun TreatmentPlanPhaseDialog(
    initial: TreatmentPlanPhase?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (TreatmentPlanPhaseRegisterRequest) -> Unit,
) {
    val statusOptions = remember { treatmentPlanPhaseStatusOptions() }
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var costText by remember(initial) {
        mutableStateOf(if (initial == null) "" else "%.2f".format(initial.estimatedCost))
    }
    var selectedStatus by remember(initial) {
        mutableStateOf(initial?.status ?: TreatmentPlanPhaseStatus.PENDING)
    }

    val canSubmit = name.isNotBlank() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                if (initial == null) "Nueva fase" else "Editar fase",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre de la fase",
                placeholder = "Endodoncia",
                enabled = !isSaving,
            )
            AppTextArea(
                value = description,
                onValueChange = { description = it },
                label = "Descripción (opcional)",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            AppTextField(
                value = costText,
                onValueChange = { costText = it },
                label = "Costo estimado (Bs)",
                placeholder = "0.00",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else if (initial == null) "Agregar fase" else "Guardar cambios",
                    onClick = {
                        onSubmit(
                            TreatmentPlanPhaseRegisterRequest(
                                name = name.trim(),
                                description = description.trim().takeIf { it.isNotEmpty() },
                                estimatedCost = parseMoneyAmount(costText) ?: 0.0,
                                status = selectedStatus,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
