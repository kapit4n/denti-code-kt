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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.MedicalServices
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

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
