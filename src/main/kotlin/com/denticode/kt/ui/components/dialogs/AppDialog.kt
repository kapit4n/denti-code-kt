package com.denticode.kt.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun AppBasicDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    shape: Shape = AppShapes.large,
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                if (dismissText != null) {
                    TextButton(onClick = onDismiss ?: onDismissRequest) { Text(dismissText) }
                }
                if (confirmText != null && onConfirm != null) {
                    TextButton(onClick = onConfirm) { Text(confirmText) }
                }
            }
        },
        modifier = modifier,
        title = { Text(text = title, style = AppTypography.SectionTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                message?.let {
                    Text(
                        text = it,
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                content?.invoke()
            }
        },
        shape = shape,
        properties = properties,
    )
}

@Composable
fun AppSurfaceDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.large,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.md),
        ) {
            val verticalBudget = (maxHeight - AppSpacing.md * 2).coerceAtLeast(120.dp)
            val scrollCap = verticalBudget.coerceAtMost(720.dp)
            Surface(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .heightIn(max = scrollCap),
                shape = shape,
                tonalElevation = tonalElevation,
                color = MaterialTheme.colorScheme.surface,
            ) {
                val scroll = rememberScrollState()
                Column(
                    modifier =
                        Modifier
                            .padding(AppSpacing.lg)
                            .verticalScroll(scroll),
                ) {
                    content()
                }
            }
        }
    }
}
