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
import androidx.compose.material.icons.filled.Medication
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.PrescriptionStatus
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

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
