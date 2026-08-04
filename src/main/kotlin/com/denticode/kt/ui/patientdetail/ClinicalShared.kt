@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppButton
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

// ── Record actions menu ────────────────────────────────────────────────────

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
