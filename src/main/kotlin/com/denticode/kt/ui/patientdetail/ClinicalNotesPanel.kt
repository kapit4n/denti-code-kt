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
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

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
