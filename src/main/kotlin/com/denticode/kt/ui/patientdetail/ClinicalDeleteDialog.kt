@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

enum class ClinicalDeleteKind {
    MEDICAL,
    DENTAL,
    NOTE,
    PRESCRIPTION,
    FOLLOW_UP,
    DOCUMENT,
    TREATMENT_PLAN,
    TREATMENT_PLAN_PHASE,
}

data class DeleteClinicalTarget(
    val kind: ClinicalDeleteKind,
    val id: Int,
    val title: String,
    val message: String,
    val filePath: String? = null,
)

@Composable
fun DeleteClinicalConfirmDialog(
    target: DeleteClinicalTarget,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                target.title,
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                target.message,
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Eliminando..." else "Eliminar",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}
