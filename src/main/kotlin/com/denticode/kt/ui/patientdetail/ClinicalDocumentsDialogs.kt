@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.PatientDocumentRegisterRequest
import com.denticode.kt.data.documentCategoryOptions
import com.denticode.kt.export.DocumentStore
import com.denticode.kt.export.ExportService
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Document dialog ────────────────────────────────────────────────────────

@Composable
fun PatientDocumentDialog(
    patientId: Int,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientDocumentRegisterRequest) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    val categoryOptions = remember { documentCategoryOptions() }
    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var fileName by remember { mutableStateOf("") }
    var fileSizeText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var mimeType by remember { mutableStateOf<String?>(null) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf(false) }
    var committed by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val canSubmit = title.isNotBlank() && fileName.isNotBlank() && !isSaving && !picking

    fun cleanupPendingFile() {
        if (!committed) {
            selectedFilePath?.let { DocumentStore.delete(it) }
        }
    }

    fun dismiss() {
        cleanupPendingFile()
        onDismiss()
    }

    fun pickFile() {
        picking = true
        localError = null
        scope.launch {
            val picked = runCatching { ExportService.pickOpenFile() }.getOrNull()
            if (picked != null) {
                val result =
                    runCatching {
                        withContext(Dispatchers.IO) { DocumentStore.save(patientId, picked) }
                    }
                val stored = result.getOrNull()
                if (stored != null) {
                    selectedFilePath = stored.absolutePath
                    fileName = stored.name
                    fileSizeText = stored.length().toString()
                    mimeType = DocumentStore.guessMimeType(picked)
                    if (title.isBlank()) {
                        title =
                            picked.nameWithoutExtension
                                .replace('_', ' ')
                                .replaceFirstChar { it.uppercase() }
                    }
                } else {
                    localError = "No se pudo copiar el archivo: ${result.exceptionOrNull()?.message ?: "error desconocido"}"
                }
            }
            picking = false
        }
    }

    AppSurfaceDialog(
        onDismissRequest = { if (!isSaving && !picking) dismiss() },
        modifier = Modifier.widthIn(max = 540.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Registrar documento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Selecciona el archivo físico: se copiará al almacén local de la clínica y podrá abrirse desde la ficha.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título",
                placeholder = "Radiografía panorámica",
                enabled = !isSaving,
            )
            AppDropdownField(
                label = "Categoría",
                options = categoryOptions,
                selected = selectedCategory,
                onSelected = { selectedCategory = it },
                enabled = !isSaving,
                optionLabel = { it.labelEs },
                placeholder = "Categoría…",
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = "Nombre del archivo",
                    placeholder = "panoramica_2024.pdf",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = if (picking) "Seleccionando..." else "Seleccionar archivo",
                    onClick = { pickFile() },
                    enabled = !isSaving && !picking,
                    leadingIcon = {
                        Icon(Icons.Default.AttachFile, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    },
                )
            }
            val attachedPath = selectedFilePath
            if (attachedPath != null) {
                Text(
                    "Archivo adjunto: ${fileName ?: "—"} (${fileSizeText.toLongOrNull()?.let(::formatDocBytes) ?: "0 B"})",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "Sin archivo adjunto (solo se registrarán los metadatos).",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppTextField(
                value = fileSizeText,
                onValueChange = { fileSizeText = it.filter { ch -> ch.isDigit() }.take(15) },
                label = "Tamaño en bytes (opcional)",
                placeholder = "0",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            localError?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas (opcional)",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = { dismiss() }, enabled = !isSaving && !picking)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Registrar documento",
                    onClick = {
                        committed = true
                        onSubmit(
                            PatientDocumentRegisterRequest(
                                title = title.trim(),
                                category = selectedCategory,
                                fileName = fileName.trim(),
                                filePath = selectedFilePath,
                                mimeType = mimeType,
                                fileSize = fileSizeText.toLongOrNull() ?: 0,
                                notes = notes.trim().takeIf { it.isNotEmpty() },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

private fun formatDocBytes(bytes: Long): String =
    when {
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024 -> "%.0f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
