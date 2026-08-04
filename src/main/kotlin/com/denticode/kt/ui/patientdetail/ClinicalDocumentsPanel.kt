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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.export.DocumentStore
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

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
