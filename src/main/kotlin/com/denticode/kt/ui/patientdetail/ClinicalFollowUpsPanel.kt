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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.FollowUpStatus
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

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
