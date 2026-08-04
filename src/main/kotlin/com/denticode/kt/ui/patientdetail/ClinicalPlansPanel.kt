@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.TreatmentPlanPhaseStatus
import com.denticode.kt.data.TreatmentPlanStatus
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

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
