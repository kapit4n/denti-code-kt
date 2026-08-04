@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.data.PrescriptionStatus
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.export.DocumentStore
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

// ── Workspace tab bar ──────────────────────────────────────────────────────

data class WorkspaceTabBadge(val tab: PatientWorkspaceTab, val count: Int?)

@Composable
fun WorkspaceTabBar(
    tabs: List<WorkspaceTabBadge>,
    selected: PatientWorkspaceTab,
    onSelect: (PatientWorkspaceTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        tabs.forEach { badge ->
            val tab = badge.tab
            val isSelected = tab == selected
            val color = if (isSelected) PatientsPremiumPalette.primary else PatientsPremiumPalette.textSecondary
            Surface(
                onClick = { onSelect(tab) },
                shape = AppShapes.small,
                color =
                    if (isSelected) PatientsPremiumPalette.primary.copy(alpha = 0.1f)
                    else Color.Transparent,
                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) PatientsPremiumPalette.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        tab.label,
                        style = AppTypography.BodySmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = color,
                    )
                    badge.count?.let { count ->
                        Surface(shape = CircleShape, color = color.copy(alpha = 0.14f)) {
                            Text(
                                count.toString(),
                                style = AppTypography.Caption,
                                color = color,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable section shell ─────────────────────────────────────────────────

@Composable
fun ClinicalSection(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    actionIcon: ImageVector = Icons.Default.Event,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                    title,
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientsPremiumPalette.textPrimary,
                )
                AppButton(
                    text = actionLabel,
                    onClick = onAction,
                    enabled = enabled,
                    minHeight = 36.dp,
                    leadingIcon = {
                        Icon(actionIcon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    },
                )
            }
            content()
        }
    }
}

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
fun ClinicalSummaryPanel(
    summary: ClinicalSummaryCounts,
    activePrescriptions: Int,
    pendingFollowUps: Int,
    activePlans: Int,
    pendingBalance: Double,
    onExportFicha: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Resumen clínico",
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientsPremiumPalette.textPrimary,
                )
                AppOutlinedButton(
                    text = "Exportar ficha",
                    onClick = onExportFicha,
                    minHeight = 36.dp,
                    leadingIcon = {
                        Icon(Icons.Default.SaveAlt, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                ClinicalSummaryChip("Planificados", summary.planned, PatientsPremiumPalette.primary, Modifier.weight(1f))
                ClinicalSummaryChip("En progreso", summary.inProgress, PatientsPremiumPalette.warning, Modifier.weight(1f))
                ClinicalSummaryChip("Completados", summary.completed, PatientsPremiumPalette.success, Modifier.weight(1f))
                ClinicalSummaryChip("Cancelados", summary.cancelled, PatientsPremiumPalette.error, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                ClinicalSummaryChip("Recetas activas", activePrescriptions, Color(0xFF8B5CF6), Modifier.weight(1f))
                ClinicalSummaryChip("Seguimientos pendientes", pendingFollowUps, PatientsPremiumPalette.warning, Modifier.weight(1f))
                ClinicalSummaryChip("Planes activos", activePlans, PatientsPremiumPalette.primary, Modifier.weight(1f))
                ClinicalSummaryMoneyChip("Saldo pendiente", pendingBalance, PatientsPremiumPalette.error, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ClinicalSummaryChip(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.small,
        color = color.copy(alpha = 0.1f),
    ) {
        Column(Modifier.padding(AppSpacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), style = AppTypography.MetricMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
        }
    }
}

@Composable
private fun ClinicalSummaryMoneyChip(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.small,
        color = color.copy(alpha = 0.1f),
    ) {
        Column(Modifier.padding(AppSpacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bs %.2f".format(value), style = AppTypography.MetricMedium, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
            Text(label, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
        }
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

@Composable
fun RecordActionsMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Opciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = PatientsPremiumPalette.error) },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = PatientsPremiumPalette.error) },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

// ── Timeline panel ─────────────────────────────────────────────────────────

@Composable
fun TimelinePanel(
    timeline: List<PatientTimelineEntry>,
    onJumpToSection: (PatientWorkspaceTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Cronología (${timeline.size})",
                style = AppTypography.SectionTitle,
                fontWeight = FontWeight.SemiBold,
                color = PatientsPremiumPalette.textPrimary,
            )
            if (timeline.isEmpty()) {
                Text(
                    "Sin eventos registrados.",
                    style = AppTypography.Body,
                    color = PatientsPremiumPalette.textSecondary,
                    modifier = Modifier.padding(vertical = AppSpacing.sm),
                )
            } else {
                timeline.forEachIndexed { index, entry ->
                    TimelineEntryRow(entry, onJumpToSection)
                    if (index < timeline.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEntryRow(
    entry: PatientTimelineEntry,
    onJumpToSection: (PatientWorkspaceTab) -> Unit,
) {
    val icon = timelineEntryIcon(entry.kind)
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(entry.color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = entry.color, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(entry.title, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
                entry.amountLabel?.let {
                    Text(it, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.success)
                }
            }
            entry.subtitle?.let {
                Text(it, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(
                formatWorkspaceEpoch(entry.timestamp),
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        AppOutlinedButton(
            text = "Ver",
            onClick = { onJumpToSection(timelineEntryTab(entry.kind)) },
            minHeight = 32.dp,
            leadingIcon = {
                Icon(Icons.Default.OpenInNew, null, Modifier.size(14.dp), tint = PatientsPremiumPalette.primary)
            },
        )
    }
}

@Composable
private fun timelineEntryIcon(kind: TimelineEntryKind): ImageVector =
    when (kind) {
        TimelineEntryKind.APPOINTMENT -> Icons.Default.CalendarMonth
        TimelineEntryKind.TREATMENT -> Icons.Outlined.MedicalServices
        TimelineEntryKind.PAYMENT -> Icons.Default.Payments
        TimelineEntryKind.NOTE -> Icons.Default.Notes
        TimelineEntryKind.PRESCRIPTION -> Icons.Default.Medication
        TimelineEntryKind.FOLLOW_UP -> Icons.Default.EventRepeat
        TimelineEntryKind.DOCUMENT -> Icons.Default.Description
        TimelineEntryKind.MEDICAL_RECORD -> Icons.Default.HealthAndSafety
        TimelineEntryKind.DENTAL_RECORD -> Icons.Default.Healing
        TimelineEntryKind.TREATMENT_PLAN -> Icons.Default.Healing
    }

private fun timelineEntryTab(kind: TimelineEntryKind): PatientWorkspaceTab =
    when (kind) {
        TimelineEntryKind.APPOINTMENT -> PatientWorkspaceTab.APPOINTMENTS
        TimelineEntryKind.TREATMENT -> PatientWorkspaceTab.CLINICAL_HISTORY
        TimelineEntryKind.PAYMENT -> PatientWorkspaceTab.PAYMENTS
        TimelineEntryKind.NOTE -> PatientWorkspaceTab.NOTES
        TimelineEntryKind.PRESCRIPTION -> PatientWorkspaceTab.PRESCRIPTIONS
        TimelineEntryKind.FOLLOW_UP -> PatientWorkspaceTab.FOLLOW_UPS
        TimelineEntryKind.DOCUMENT -> PatientWorkspaceTab.DOCUMENTS
        TimelineEntryKind.MEDICAL_RECORD -> PatientWorkspaceTab.CLINICAL_HISTORY
        TimelineEntryKind.DENTAL_RECORD -> PatientWorkspaceTab.CLINICAL_HISTORY
        TimelineEntryKind.TREATMENT_PLAN -> PatientWorkspaceTab.TREATMENT_PLAN
    }

// ── Documents panel ────────────────────────────────────────────────────────

@Composable
fun DocumentsPanel(
    documents: List<PatientDocumentUi>,
    onUpload: () -> Unit,
    onOpen: (PatientDocumentUi) -> Unit,
    onDelete: (PatientDocumentUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClinicalSection(
        title = "Documentos (${documents.size})",
        actionLabel = "Subir documento",
        onAction = onUpload,
        modifier = modifier,
        actionIcon = Icons.Default.Description,
    ) {
        if (documents.isEmpty()) {
            Text(
                "Sin documentos. Sube radiografías, fotos, consentimientos o archivos PDF.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.padding(vertical = AppSpacing.sm),
            )
        } else {
            documents.forEachIndexed { index, doc ->
                DocumentCard(doc, onOpen = { onOpen(doc) }, onDelete = { onDelete(doc) })
                if (index < documents.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: PatientDocumentUi,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(document.categoryColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Description, null, tint = document.categoryColor, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(document.title, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DetailStatusBadge(document.categoryLabel, document.categoryColor)
                DetailStatusBadge(DocumentStore.extensionLabel(document.fileName), PatientsPremiumPalette.textSecondary)
                if (document.filePath == null) {
                    DetailStatusBadge("Solo metadatos", PatientsPremiumPalette.textSecondary)
                }
                Text(document.fileSizeLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
            }
            Text(
                listOfNotNull(document.uploadedAtLabel, document.fileName).joinToString(" · "),
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            document.notes?.let {
                Text(it, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        AppOutlinedButton(
            text = "Abrir",
            onClick = onOpen,
            minHeight = 32.dp,
            enabled = document.filePath != null,
            leadingIcon = {
                Icon(Icons.Default.OpenInNew, null, Modifier.size(14.dp), tint = PatientsPremiumPalette.primary)
            },
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Eliminar documento", tint = PatientsPremiumPalette.error)
        }
    }
}

// ── Notes panel ────────────────────────────────────────────────────────────

@Composable
fun NotesPanel(
    notes: List<PatientNoteUi>,
    onAdd: () -> Unit,
    onTogglePin: (PatientNoteUi) -> Unit,
    onDelete: (PatientNoteUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(notes) {
        notes.sortedWith(compareByDescending<PatientNoteUi> { it.isPinned }.thenByDescending { it.id })
    }
    ClinicalSection(
        title = "Notas (${notes.size})",
        actionLabel = "Nueva nota",
        onAction = onAdd,
        modifier = modifier,
        actionIcon = Icons.Default.Notes,
    ) {
        if (sorted.isEmpty()) {
            Text(
                "Sin notas. Registra observaciones de la atención.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.padding(vertical = AppSpacing.sm),
            )
        } else {
            sorted.forEachIndexed { index, note ->
                NoteCard(note, onTogglePin = { onTogglePin(note) }, onDelete = { onDelete(note) })
                if (index < sorted.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: PatientNoteUi,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(PatientsPremiumPalette.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Notes,
                null,
                tint = PatientsPremiumPalette.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                note.body,
                style = AppTypography.Body,
                fontWeight = if (note.isPinned) FontWeight.Medium else FontWeight.Normal,
                color = PatientsPremiumPalette.textPrimary,
            )
            Text(
                "${note.authorLabel} · ${note.createdAtLabel}",
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        IconButton(onClick = onTogglePin) {
            Icon(
                Icons.Default.PushPin,
                if (note.isPinned) "Desfijar nota" else "Fijar nota",
                tint = if (note.isPinned) PatientsPremiumPalette.warning else PatientsPremiumPalette.textSecondary,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Eliminar nota", tint = PatientsPremiumPalette.error)
        }
    }
}

// ── Prescriptions panel ────────────────────────────────────────────────────

@Composable
fun PrescriptionsPanel(
    prescriptions: List<PatientPrescriptionUi>,
    onAdd: () -> Unit,
    onStatusChange: (PatientPrescriptionUi, PrescriptionStatus) -> Unit,
    onDelete: (PatientPrescriptionUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClinicalSection(
        title = "Recetas (${prescriptions.size})",
        actionLabel = "Nueva receta",
        onAction = onAdd,
        modifier = modifier,
        actionIcon = Icons.Default.Medication,
    ) {
        if (prescriptions.isEmpty()) {
            Text(
                "Sin recetas. Emite prescripciones de medicamentos.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.padding(vertical = AppSpacing.sm),
            )
        } else {
            prescriptions.forEachIndexed { index, rx ->
                PrescriptionCard(rx, onStatusChange = { s -> onStatusChange(rx, s) }, onDelete = { onDelete(rx) })
                if (index < prescriptions.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun PrescriptionCard(
    prescription: PatientPrescriptionUi,
    onStatusChange: (PrescriptionStatus) -> Unit,
    onDelete: () -> Unit,
) {
    var statusMenu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(Color(0xFF8B5CF6).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Medication, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(prescription.medicine, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
            Text(
                "${prescription.dosage} · ${prescription.frequency}",
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
            )
            prescription.instructions?.let {
                Text(it, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(
                listOfNotNull(prescription.prescribedAtLabel, prescription.doctorName).joinToString(" · "),
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Box {
                DetailStatusBadge(
                    label = prescription.statusLabel,
                    color = prescription.statusColor,
                )
                DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                    PrescriptionStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.labelEs) },
                            onClick = {
                                statusMenu = false
                                onStatusChange(status)
                            },
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = { statusMenu = true }) {
                    Icon(Icons.Default.Edit, "Cambiar estado", tint = PatientsPremiumPalette.textSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar receta", tint = PatientsPremiumPalette.error)
                }
            }
        }
    }
}

// ── Follow-ups panel ───────────────────────────────────────────────────────

@Composable
fun FollowUpsPanel(
    followUps: List<PatientFollowUpUi>,
    onAdd: () -> Unit,
    onStatusChange: (PatientFollowUpUi, FollowUpStatus) -> Unit,
    onDelete: (PatientFollowUpUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(followUps) {
        followUps.sortedWith(
            compareBy<PatientFollowUpUi> { it.status == FollowUpStatus.COMPLETED }
                .thenBy { it.status == FollowUpStatus.CANCELLED }
                .thenBy { it.dueDate },
        )
    }
    ClinicalSection(
        title = "Seguimientos (${followUps.size})",
        actionLabel = "Nuevo seguimiento",
        onAction = onAdd,
        modifier = modifier,
        actionIcon = Icons.Default.EventRepeat,
    ) {
        if (sorted.isEmpty()) {
            Text(
                "Sin seguimientos programados.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.padding(vertical = AppSpacing.sm),
            )
        } else {
            sorted.forEachIndexed { index, followUp ->
                FollowUpCard(followUp, onStatusChange = { s -> onStatusChange(followUp, s) }, onDelete = { onDelete(followUp) })
                if (index < sorted.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun FollowUpCard(
    followUp: PatientFollowUpUi,
    onStatusChange: (FollowUpStatus) -> Unit,
    onDelete: () -> Unit,
) {
    var statusMenu by remember { mutableStateOf(false) }
    val accent =
        when (followUp.dueState) {
            FollowUpDueState.OVERDUE -> PatientsPremiumPalette.error
            FollowUpDueState.TODAY -> PatientsPremiumPalette.warning
            FollowUpDueState.UPCOMING -> PatientsPremiumPalette.primary
            FollowUpDueState.DONE -> PatientsPremiumPalette.textSecondary
        }
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(40.dp).clip(AppShapes.small).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.EventRepeat, null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(followUp.dueDateLabel, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
                if (followUp.status == FollowUpStatus.PENDING) {
                    DetailStatusBadge(followUp.dueState.labelEs, accent)
                }
            }
            followUp.notes?.let {
                Text(it, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary)
            }
            DetailStatusBadge(followUp.statusLabel, followUp.statusColor)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            IconButton(onClick = { statusMenu = true }) {
                Icon(Icons.Default.Edit, "Cambiar estado", tint = PatientsPremiumPalette.textSecondary)
            }
            DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                FollowUpStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.labelEs) },
                        onClick = {
                            statusMenu = false
                            onStatusChange(status)
                        },
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar seguimiento", tint = PatientsPremiumPalette.error)
            }
        }
    }
}

// ── Treatment plans panel ───────────────────────────────────────────────────

@Composable
fun TreatmentPlanPanel(
    plans: List<PatientTreatmentPlanUi>,
    onAddPlan: () -> Unit,
    onEditPlan: (PatientTreatmentPlanUi) -> Unit,
    onDeletePlan: (PatientTreatmentPlanUi) -> Unit,
    onPlanStatusChange: (PatientTreatmentPlanUi, TreatmentPlanStatus) -> Unit,
    onAddPhase: (PatientTreatmentPlanUi) -> Unit,
    onEditPhase: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi) -> Unit,
    onDeletePhase: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi) -> Unit,
    onPhaseStatusChange: (PatientTreatmentPlanUi, PatientTreatmentPlanPhaseUi, TreatmentPlanPhaseStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClinicalSection(
        title = "Planes de tratamiento (${plans.size})",
        actionLabel = "Nuevo plan",
        onAction = onAddPlan,
        modifier = modifier,
        actionIcon = Icons.Default.Healing,
    ) {
        if (plans.isEmpty()) {
            Text(
                "Sin planes de tratamiento. Crea un plan con fases, cronograma y presupuesto estimado.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.padding(vertical = AppSpacing.sm),
            )
        } else {
            plans.forEachIndexed { index, plan ->
                TreatmentPlanCard(
                    plan = plan,
                    onEditPlan = { onEditPlan(plan) },
                    onDeletePlan = { onDeletePlan(plan) },
                    onPlanStatusChange = { status -> onPlanStatusChange(plan, status) },
                    onAddPhase = { onAddPhase(plan) },
                    onEditPhase = { phase -> onEditPhase(plan, phase) },
                    onDeletePhase = { phase -> onDeletePhase(plan, phase) },
                    onPhaseStatusChange = { phase, status -> onPhaseStatusChange(plan, phase, status) },
                )
                if (index < plans.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun TreatmentPlanCard(
    plan: PatientTreatmentPlanUi,
    onEditPlan: () -> Unit,
    onDeletePlan: () -> Unit,
    onPlanStatusChange: (TreatmentPlanStatus) -> Unit,
    onAddPhase: () -> Unit,
    onEditPhase: (PatientTreatmentPlanPhaseUi) -> Unit,
    onDeletePhase: (PatientTreatmentPlanPhaseUi) -> Unit,
    onPhaseStatusChange: (PatientTreatmentPlanPhaseUi, TreatmentPlanPhaseStatus) -> Unit,
) {
    var statusMenu by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                Modifier.size(40.dp).clip(AppShapes.small).background(plan.statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Healing, null, tint = plan.statusColor, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(plan.title, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
                plan.description?.let {
                    Text(
                        it,
                        style = AppTypography.BodySmall,
                        color = PatientsPremiumPalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    "${plan.createdAtLabel} · Presupuesto: Bs ${"%.2f".format(plan.estimatedCost)}",
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textSecondary,
                )
                if (plan.phases.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { plan.phaseProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = plan.statusColor,
                        trackColor = plan.statusColor.copy(alpha = 0.15f),
                    )
                    Text(
                        "${plan.completedPhases}/${plan.phases.size} fases completadas",
                        style = AppTypography.Caption,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Box {
                    DetailStatusBadge(plan.statusLabel, plan.statusColor)
                    DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                        TreatmentPlanStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.labelEs) },
                                onClick = {
                                    statusMenu = false
                                    onPlanStatusChange(status)
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = { statusMenu = true }) {
                    Icon(Icons.Default.Edit, "Cambiar estado del plan", tint = PatientsPremiumPalette.textSecondary)
                }
            }
        }

        if (plan.phases.isEmpty()) {
            Text(
                "Sin fases todavía. Agrega la primera fase.",
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.padding(start = 52.dp),
            )
        } else {
            Column(Modifier.padding(start = 52.dp), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                plan.phases.forEach { phase ->
                    TreatmentPlanPhaseRow(
                        phase = phase,
                        onEdit = { onEditPhase(phase) },
                        onDelete = { onDeletePhase(phase) },
                        onStatusChange = { status -> onPhaseStatusChange(phase, status) },
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            AppOutlinedButton(
                text = "Agregar fase",
                onClick = onAddPhase,
                minHeight = 32.dp,
                leadingIcon = {
                    Icon(Icons.Default.Add, null, Modifier.size(14.dp), tint = PatientsPremiumPalette.primary)
                },
            )
            AppOutlinedButton(
                text = "Editar plan",
                onClick = onEditPlan,
                minHeight = 32.dp,
                leadingIcon = {
                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = PatientsPremiumPalette.primary)
                },
            )
            IconButton(onClick = onDeletePlan) {
                Icon(Icons.Default.Delete, "Eliminar plan", tint = PatientsPremiumPalette.error)
            }
        }
    }
}

@Composable
private fun TreatmentPlanPhaseRow(
    phase: PatientTreatmentPlanPhaseUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (TreatmentPlanPhaseStatus) -> Unit,
) {
    var statusMenu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            Modifier.size(28.dp).clip(AppShapes.small).background(phase.statusColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Event, null, tint = phase.statusColor, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(phase.name, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
            phase.description?.let {
                Text(
                    it,
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("Bs ${"%.2f".format(phase.estimatedCost)}", style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
        }
        Box {
            DetailStatusBadge(phase.statusLabel, phase.statusColor)
            DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                TreatmentPlanPhaseStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.labelEs) },
                        onClick = {
                            statusMenu = false
                            onStatusChange(status)
                        },
                    )
                }
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Editar fase", tint = PatientsPremiumPalette.textSecondary)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Eliminar fase", tint = PatientsPremiumPalette.error)
        }
    }
}
