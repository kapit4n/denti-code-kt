@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

// ── Overview summary panel ─────────────────────────────────────────────────

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
internal fun ClinicalSummaryChip(
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
