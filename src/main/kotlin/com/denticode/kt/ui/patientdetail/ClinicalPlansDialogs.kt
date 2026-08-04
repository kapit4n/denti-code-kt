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
import com.denticode.kt.data.TreatmentPlan
import com.denticode.kt.data.TreatmentPlanPhase
import com.denticode.kt.data.TreatmentPlanPhaseRegisterRequest
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanRegisterRequest
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.data.treatmentPlanPhaseStatusOptions
import com.denticode.kt.data.treatmentPlanStatusOptions
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.parseMoneyAmount
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

// ── Treatment plan dialog ──────────────────────────────────────────────────

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

// ── Treatment plan phase dialog ────────────────────────────────────────────

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
