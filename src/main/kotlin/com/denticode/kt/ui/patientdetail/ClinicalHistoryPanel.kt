@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

// ── Clinical history panel ─────────────────────────────────────────────────

@Composable
fun ClinicalHistoryPanel(
    medicalRecords: List<PatientMedicalRecordUi>,
    dentalRecords: List<PatientDentalRecordUi>,
    summary: ClinicalSummaryCounts,
    onAddMedical: () -> Unit,
    onEditMedical: (PatientMedicalRecordUi) -> Unit,
    onDeleteMedical: (PatientMedicalRecordUi) -> Unit,
    onAddDental: () -> Unit,
    onEditDental: (PatientDentalRecordUi) -> Unit,
    onDeleteDental: (PatientDentalRecordUi) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            color = PatientsPremiumPalette.card,
            shadowElevation = AppElevations.cardRest,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text(
                    "Resumen clínico",
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientsPremiumPalette.textPrimary,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    ClinicalSummaryChip("Planificados", summary.planned, PatientsPremiumPalette.primary, Modifier.weight(1f))
                    ClinicalSummaryChip("En progreso", summary.inProgress, PatientsPremiumPalette.warning, Modifier.weight(1f))
                    ClinicalSummaryChip("Completados", summary.completed, PatientsPremiumPalette.success, Modifier.weight(1f))
                    ClinicalSummaryChip("Cancelados", summary.cancelled, PatientsPremiumPalette.error, Modifier.weight(1f))
                }
            }
        }

        MedicalHistorySection(
            medicalRecords = medicalRecords,
            onAdd = onAddMedical,
            onEdit = onEditMedical,
            onDelete = onDeleteMedical,
            enabled = enabled,
        )

        DentalHistorySection(
            dentalRecords = dentalRecords,
            onAdd = onAddDental,
            onEdit = onEditDental,
            onDelete = onDeleteDental,
            enabled = enabled,
        )
    }
}

@Composable
private fun MedicalHistorySection(
    medicalRecords: List<PatientMedicalRecordUi>,
    onAdd: () -> Unit,
    onEdit: (PatientMedicalRecordUi) -> Unit,
    onDelete: (PatientMedicalRecordUi) -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Historial médico (${medicalRecords.size})",
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientsPremiumPalette.textPrimary,
                )
                AppButton(
                    text = "Agregar",
                    onClick = onAdd,
                    enabled = enabled,
                    minHeight = 36.dp,
                    leadingIcon = {
                        Icon(Icons.Default.HealthAndSafety, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    },
                )
            }
            if (medicalRecords.isEmpty()) {
                Text(
                    "No hay registros médicos. Agrega alergias, condiciones, cirugías o medicación.",
                    style = AppTypography.Body,
                    color = PatientsPremiumPalette.textSecondary,
                    modifier = Modifier.padding(vertical = AppSpacing.sm),
                )
            } else {
                medicalRecords.forEachIndexed { index, record ->
                    MedicalRecordCard(record, onEdit = { onEdit(record) }, onDelete = { onDelete(record) })
                    if (index < medicalRecords.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalRecordCard(
    record: PatientMedicalRecordUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(record.color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.HealthAndSafety, null, tint = record.color, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    record.recordTypeLabel,
                    style = AppTypography.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = record.color,
                )
                if (!record.isActive) {
                    DetailStatusBadge("Inactivo", PatientsPremiumPalette.textSecondary)
                }
            }
            Text(record.description, style = AppTypography.Body, fontWeight = FontWeight.Medium, color = PatientsPremiumPalette.textPrimary)
            Text(
                listOfNotNull(record.recordedAtLabel, record.doctorName).joinToString(" · "),
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
            )
            record.notes?.let {
                Text(it, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        RecordActionsMenu(
            onEdit = onEdit,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun DentalHistorySection(
    dentalRecords: List<PatientDentalRecordUi>,
    onAdd: () -> Unit,
    onEdit: (PatientDentalRecordUi) -> Unit,
    onDelete: (PatientDentalRecordUi) -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Historial dental (${dentalRecords.size})",
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientsPremiumPalette.textPrimary,
                )
                AppButton(
                    text = "Agregar",
                    onClick = onAdd,
                    enabled = enabled,
                    minHeight = 36.dp,
                    leadingIcon = {
                        Icon(Icons.Default.Healing, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    },
                )
            }
            if (dentalRecords.isEmpty()) {
                Text(
                    "No hay registros dentales. Registra diagnósticos y tratamientos por pieza.",
                    style = AppTypography.Body,
                    color = PatientsPremiumPalette.textSecondary,
                    modifier = Modifier.padding(vertical = AppSpacing.sm),
                )
            } else {
                dentalRecords.forEachIndexed { index, record ->
                    DentalRecordCard(record, onEdit = { onEdit(record) }, onDelete = { onDelete(record) })
                    if (index < dentalRecords.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DentalRecordCard(
    record: PatientDentalRecordUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(PatientsPremiumPalette.warning.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Healing, null, tint = PatientsPremiumPalette.warning, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                record.toothLabel,
                style = AppTypography.BodySmall,
                fontWeight = FontWeight.SemiBold,
                color = PatientsPremiumPalette.primary,
            )
            Text(record.diagnosis, style = AppTypography.Body, fontWeight = FontWeight.Medium, color = PatientsPremiumPalette.textPrimary)
            record.treatmentPerformed?.let {
                Text("Tratamiento: $it", style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary)
            }
            Text(
                listOfNotNull(record.recordedAtLabel, record.doctorName, record.procedureTypeName).joinToString(" · "),
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        RecordActionsMenu(
            onEdit = onEdit,
            onDelete = onDelete,
        )
    }
}
